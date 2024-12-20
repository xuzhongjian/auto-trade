package com.gantenx.controller;

import com.gantenx.constant.CryptoCurrency;
import com.gantenx.constant.CryptoSymbol;
import com.gantenx.model.Kline;
import com.gantenx.service.BinanceService;
import com.gantenx.utils.DateUtils;
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
    private BinanceService binanceService;

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/kline")
    public List<Kline> kline(@RequestParam("base") CryptoCurrency base,
                             @RequestParam("quote") CryptoCurrency quote,
                             @RequestParam("begin") String beginStr,
                             @RequestParam("end") String endStr,
                             @RequestParam(value = "limit", required = false, defaultValue = "500") int limit) {
        CryptoSymbol symbol = CryptoSymbol.toSymbol(base, quote);
        return binanceService.getKline(symbol, DateUtils.getTimestamp(beginStr), DateUtils.getTimestamp(endStr));
    }
}