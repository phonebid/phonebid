package com.phonebid.app.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "portone.v2")
public class PortOneV2Properties {

    private String storeId;
    private String apiSecret;
    private String channelKey;
    private String webhookSecret;

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public void setApiSecret(String apiSecret) {
        this.apiSecret = apiSecret;
    }

    public String getChannelKey() {
        return channelKey;
    }

    public void setChannelKey(String channelKey) {
        this.channelKey = channelKey;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public void setWebhookSecret(String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }
}

