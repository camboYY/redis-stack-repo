package com.crypto.ranking.crypto.ranking;

import com.crypto.ranking.crypto.ranking.service.CoinDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartup implements ApplicationListener<ApplicationEvent> {

    @Autowired
    private CoinDataService coinDataService;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {

        coinDataService.fetchCoins();
        coinDataService.fetChCoinHistory();
    }
}
