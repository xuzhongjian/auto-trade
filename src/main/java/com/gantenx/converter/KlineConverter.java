package com.gantenx.converter;

import com.gantenx.model.Kline;
import com.gantenx.util.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.gantenx.util.DateUtils.MS_OF_ONE_DAY;

public class KlineConverter {

    // 返回转换后的 A/B 的 Kline 列表
    public static List<Kline> getKline(Map<Long, Kline> baseMap, Map<Long, Kline> quoteMap, String beginStr, String endStr) {
        long begin = DateUtils.getTimestamp(beginStr);
        long end = DateUtils.getTimestamp(endStr);
        List<Kline> resultList = new ArrayList<>();

        // 遍历时间戳区间
        for (long ts = begin; ts <= end; ts += MS_OF_ONE_DAY) {
            Kline baseKline = baseMap.get(ts);  // A/USD Kline
            Kline quoteKline = quoteMap.get(ts); // B/USD Kline

            // 如果基准数据和目标数据都存在，则进行转换
            if (Objects.nonNull(baseKline) && Objects.nonNull(quoteKline)) {
                resultList.add(converter(baseKline, quoteKline));
            }
        }

        return resultList;
    }

    // 转换 A/USD 和 B/USD Kline 为 A/B Kline
    private static Kline converter(Kline base, Kline quote) {
        // 1. 时间戳保持一致
        Kline kline = new Kline(base.getTimestamp());

        // 2. 开盘价：A/USD 的开盘价 / B/USD 的开盘价
        double baseOpen = base.getOpen();
        double quoteOpen = quote.getOpen();
        kline.setOpen(baseOpen / quoteOpen);

        // 3. 最高价：A/USD 的最高价 / B/USD 的最高价
        double baseHigh = base.getHigh();
        double quoteHigh = quote.getHigh();
        kline.setHigh(baseHigh / quoteHigh);

        // 4. 最低价：A/USD 的最低价 / B/USD 的最低价
        double baseLow = base.getLow();
        double quoteLow = quote.getLow();
        kline.setLow(baseLow / quoteLow);

        // 5. 收盘价：A/USD 的收盘价 / B/USD 的收盘价
        double baseClose = base.getClose();
        double quoteClose = quote.getClose();
        kline.setClose(baseClose / quoteClose);

        // 6. 成交量：可以选择使用 base 或 quote 的成交量，这里我们选择 base 的成交量
        kline.setVolume(base.getVolume());

        return kline;
    }
}