package com.leyou.order.config;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(PayConfig.class)
public class WXPayConfiguration {

    @Autowired
    private PayConfig payConfig;

    @Bean
    public WXPay wxPay(){
        return new WXPay(payConfig, WXPayConstants.SignType.HMACSHA256);
    }
}
