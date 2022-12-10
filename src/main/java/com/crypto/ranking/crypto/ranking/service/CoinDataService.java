package com.crypto.ranking.crypto.ranking.service;

import com.crypto.ranking.crypto.ranking.model.*;
import com.crypto.ranking.crypto.ranking.utils.HttpConfig;
import io.github.dengliming.redismodule.redisjson.RedisJSON;
import io.github.dengliming.redismodule.redisjson.args.GetArgs;
import io.github.dengliming.redismodule.redisjson.args.SetArgs;
import io.github.dengliming.redismodule.redisjson.utils.GsonUtils;
import io.github.dengliming.redismodule.redistimeseries.DuplicatePolicy;
import io.github.dengliming.redismodule.redistimeseries.RedisTimeSeries;
import io.github.dengliming.redismodule.redistimeseries.Sample;
import io.github.dengliming.redismodule.redistimeseries.TimeSeriesOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class CoinDataService {
    private static final String GET_COINS_HISTORY_API = "https://coinranking1.p.rapidapi.com/coin/";
    private static final String COIN_HISTORY_PEROID_PARAMS = "/history?referenceCurrencyUuid=yhjMzLPhuIDl&timePeriod=";
    private static final String GET_COINS_API = "https://coinranking1.p.rapidapi.com/coins?referenceCurrencyUuid=yhjMzLPhuIDl&timePeriod=24h&tiers%5B0%5D=1&orderBy=marketCap&orderDirection=desc&limit=50&offset=0";
    private static final List<String> timePeroids = List.of("24h", "7d", "30d", "3m", "1y", "3y", "5y");
    private static final String REDIS_KEY_COINS = "REDIS_KEY_COINS";


    @Autowired
    private RestTemplate restTemplate;


    @Autowired
    private RedisTimeSeries redisTimeSeries;

    @Autowired
    private RedisJSON redisJSON;

    public void fetchCoins () {
        log.info("Inside FetchCoins()");
      ResponseEntity<Coins> coinsResponseEntity = restTemplate.exchange( GET_COINS_API, HttpMethod.GET, HttpConfig.getHttpEntity(), Coins.class);

      storeCoinsToRedisJson(coinsResponseEntity.getBody());

    }

    public void fetChCoinHistory () {
        log.info("Inside fetChCoinHistory");

        List<CoinInfo> allCoins = getAllCoinsFromRedisJson();
        allCoins.forEach(coinInfo -> {
            timePeroids.forEach(timePeriod->{
                fetChCoinHistoryForTimerPeriod(coinInfo,timePeriod);
            });
        });
    }

    private void fetChCoinHistoryForTimerPeriod(CoinInfo coinInfo, String timePeriod) {
        log.info("insider fetChCoinHistoryForTimerPeriod");
        String url = GET_COINS_HISTORY_API+coinInfo.getUuid()+COIN_HISTORY_PEROID_PARAMS+timePeriod;
        ResponseEntity<CoinPriceHistory> coinPriceHistory = restTemplate.exchange(url, HttpMethod.GET,HttpConfig.getHttpEntity(), CoinPriceHistory.class);
        log.info("Data fetched from api for coin history of {} for time period {}", coinInfo.getName(),timePeriod);
        storeCoinHisotryToRedisTS(coinPriceHistory.getBody(), coinInfo.getSymbol(), timePeriod);
    }

    private void storeCoinHisotryToRedisTS(CoinPriceHistory coinPriceHistory, String symbol, String timePeriod) {
        log.info("Storing Coin history of {} for time period {} into redis TS", symbol, timePeriod);
        List<CoinPriceHistoryExchangeRate> coinPriceHistoryExchangeRates = coinPriceHistory.getData().getHistory();
        coinPriceHistoryExchangeRates.stream().filter(ch-> ch.getPrice() != null && ch.getTimestamp() != null)
                .forEach(ch ->{
                    redisTimeSeries.add(
                            new Sample(symbol+":"+timePeriod, Sample.Value.of(Long.valueOf(ch.getTimestamp()), Double.valueOf(ch.getPrice()))),
                            new TimeSeriesOptions()
                            .unCompressed()
                            .duplicatePolicy(DuplicatePolicy.LAST));
                });
        log.info("Completed Storing Coin history of {} for time period {} into redis TS", symbol, timePeriod);

    }

    private List<CoinInfo> getAllCoinsFromRedisJson () {
      CoinData coinData = redisJSON.get(GET_COINS_API, CoinData.class, new GetArgs().path(".data").indent("\t").newLine("\n").space(" "));
      return  coinData.getCoins();
    }

    private void storeCoinsToRedisJson(Coins body) {
        redisJSON.set(REDIS_KEY_COINS, SetArgs.Builder.create(".", GsonUtils.toJson(body)));
    }
}
