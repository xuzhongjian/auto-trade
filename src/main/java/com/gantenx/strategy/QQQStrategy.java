package com.gantenx.strategy;

import com.gantenx.model.Kline;
import com.gantenx.model.Order;
import com.gantenx.model.TradeDetail;
import com.gantenx.util.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.gantenx.util.DateUtils.MS_OF_ONE_DAY;

@Slf4j
public class QQQStrategy {

    /**
     * 开始到结束，长期持有 QQQ 的收益
     *
     * @param startStr 开始时间
     * @param endStr   结束时间
     * @param kline    QQQ 的 K 线
     * @return 策略执行过后的订单列表，盈利信息等
     */
    private static TradeDetail longTermHolding(String startStr,
                                               String endStr,
                                               Map<Long, Kline> kline) {
        long start = DateUtils.getTimestamp(startStr);
        long end = DateUtils.getTimestamp(endStr);
        Kline qqqLastCandle = null;
        long lastTs = 0;

        TradeMocker tradeMocker = new TradeMocker(10000.0, 0.001);
        for (long ts = start; ts <= end; ts += MS_OF_ONE_DAY) {
            Kline qqqCandle = kline.get(ts);
            qqqLastCandle = qqqCandle;
            if (Objects.isNull(qqqCandle)) {
                // 说明今日美股不开市，或者数据异常
                continue;
            }
            lastTs = ts;
            double qqqPrice = Double.parseDouble(qqqCandle.getClose());
            // 没有仓位的时候，持有QQQ
            if (!tradeMocker.hasPosition()) {
                tradeMocker.buyAll("X", qqqPrice, ts);
            }
        }
        HashMap<String, Double> priceMap = new HashMap<>();
        priceMap.put("X", Double.parseDouble(qqqLastCandle.getClose()));
        return tradeMocker.exit(priceMap, lastTs);
    }

    /**
     * @param startStr     开始时间
     * @param endStr       结束时间
     * @param tqqqKlineMap TQQQ 的 K 线
     * @param qqqKlineMap  QQQ 的 K 线
     * @param qqqRsiMap    QQQ 的 RSI 线
     * @return 策略执行过后的订单列表，盈利信息等
     */
    private static TradeDetail process(String startStr,
                                       String endStr,
                                       Map<Long, Kline> tqqqKlineMap,
                                       Map<Long, Kline> qqqKlineMap,
                                       Map<Long, Double> qqqRsiMap) {
        long start = DateUtils.getTimestamp(startStr);
        long end = DateUtils.getTimestamp(endStr);
        Kline tqqqLastCandle = null;
        Kline qqqLastCandle = null;
        long lastTs = 0;

        TradeMocker tradeMocker = new TradeMocker(10000.0, 0.001);
        for (long ts = start; ts <= end; ts += MS_OF_ONE_DAY) {
            Double rsi = qqqRsiMap.get(ts);
            Kline tqqqCandle = tqqqKlineMap.get(ts);
            tqqqLastCandle = tqqqCandle;
            Kline qqqCandle = qqqKlineMap.get(ts);
            qqqLastCandle = qqqCandle;
            if (Objects.isNull(rsi) || Objects.isNull(qqqCandle) || Objects.isNull(tqqqCandle)) {
                // 说明今日美股不开市，或者数据异常
                continue;
            }
            lastTs = ts;
            double tqqqPrice = Double.parseDouble(tqqqCandle.getClose());
            double qqqPrice = Double.parseDouble(qqqCandle.getClose());
            // 没有仓位的时候，持有QQQ
            if (!tradeMocker.hasPosition()) {
                tradeMocker.buyAll("QQQ", qqqPrice, ts);
            }
            //认为 QQQ 原始标的价格到达高点，抛售 TQQQ，进行长期持有 QQQ
            if (rsi > 70) {
                tradeMocker.sellAll("TQQQ", tqqqPrice, ts);
                tradeMocker.buyAll("QQQ", qqqPrice, ts);
            }
            //认为 QQQ 原始标的价格到达低点，抛售 QQQ，进行短期期持有 TQQQ
            if (rsi < 30) {
                tradeMocker.sellAll("QQQ", qqqPrice, ts);
                tradeMocker.buyAll("TQQQ", tqqqPrice, ts);
            }
        }
        HashMap<String, Double> priceMap = new HashMap<>();
        priceMap.put("QQQ", Double.parseDouble(qqqLastCandle.getClose()));
        priceMap.put("TQQQ", Double.parseDouble(tqqqLastCandle.getClose()));
        return tradeMocker.exit(priceMap, lastTs);
    }

    public static void replay(String start, String end) {
        // 从 CSV 文件中获取历史数据
        List<Kline> qqqKlineList = CsvUtils.getKLineFromCsv("data/QQQ.csv", start, end);
        List<Kline> tqqqKlineList = CsvUtils.getKLineFromCsv("data/TQQQ.csv", start, end);
        // 转换成 map 格式
        Map<Long, Double> rsiMap = RsiCalculator.calculateAndAttachRSI(qqqKlineList, 6);
        Map<Long, Kline> tqqqKlineMap = CollectionUtils.toTimeMap(tqqqKlineList);
        Map<Long, Kline> qqqKlineMap = CollectionUtils.toTimeMap(qqqKlineList);
        // 执行对应的策略，输出交易的结果
        log.info("------------------------------------策略------------------------------------------------------------");
        TradeDetail td = process(start, end, tqqqKlineMap, qqqKlineMap, rsiMap);
        printTradeDetail(td);
        log.info("-----------------------------------长期持有QQQ-------------------------------------------------------");
        TradeDetail longQQQ = longTermHolding(start, end, qqqKlineMap);
        printTradeDetail(longQQQ);
        log.info("-----------------------------------长期持有TQQQ------------------------------------------------------");
        TradeDetail longTQQQ = longTermHolding(start, end, tqqqKlineMap);
        printTradeDetail(longTQQQ);

        TradingChart.show(qqqKlineList, tqqqKlineList, rsiMap, td.getOrders());
    }

    private static void printTradeDetail(TradeDetail td) {
        List<Order> orders = td.getOrders();
        for (Order order : orders) {
            long timestamp = order.getTimestamp();
            String date = DateUtils.getDate(timestamp);
            double price = order.getPrice();
            double quantity = order.getQuantity();
            String symbol = order.getSymbol();
            String type = order.getType();
            log.info("{}: {} {}, {} * {} = {}", date, type, symbol, price, quantity, price * quantity);
        }

        Map<String, OrderCalculator.Result> results = OrderCalculator.calculateProfitAndHoldingDays(orders);
        for (Map.Entry<String, OrderCalculator.Result> entry : results.entrySet()) {
            OrderCalculator.Result result = entry.getValue();
            log.info("{}: holding days:{}, profit:{}", entry.getKey(), result.getTotalHoldingDays(), result.getProfit());
        }
        log.info("init balance:{}, finish balance:{}", td.getInitialBalance(), td.getBalance());
        log.info("fee:{}", td.getFeeCount());
    }
}