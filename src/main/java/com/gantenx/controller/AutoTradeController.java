package com.gantenx.controller;

import com.gantenx.model.RSI;
import com.gantenx.model.response.KlineModel;
import com.gantenx.service.QuoteService;
import com.gantenx.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.gantenx.constant.Constants.ONE_DAY;

@Slf4j
@RestController
@RequestMapping("/auto-trade")
public class AutoTradeController {

    @Autowired
    private QuoteService quoteService;

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/kline")
    public List<KlineModel> kline(@RequestParam("symbol") String symbol,
                                    @RequestParam("begin") String beginStr,
                                    @RequestParam("end") String endStr,
                                    @RequestParam(value = "limit", required = false, defaultValue = "500") int limit) {
        return quoteService.getKline(symbol.toUpperCase(), ONE_DAY, DateUtils.getTimestamp(beginStr), DateUtils.getTimestamp(endStr), limit);
    }

    @GetMapping("/rsi")
    public List<RSI> rsi(@RequestParam("symbol") String symbol,
                         @RequestParam("begin") String beginStr,
                         @RequestParam("end") String endStr,
                         @RequestParam(value = "limit", required = false, defaultValue = "500") int limit) {
        return quoteService.getRsi(symbol.toUpperCase(), ONE_DAY, DateUtils.getTimestamp(beginStr), DateUtils.getTimestamp(endStr), limit);
    }
}