package org.chzz.market.domain.payment.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;


public class TossAuthorizationHeaderProvider extends PaymentAuthorizationHeaderProvider {
    private static final String PREFIX = "Basic ";
    public TossAuthorizationHeaderProvider(String secretKey, String clientKey) {
        super(secretKey, clientKey);
    }


    @Override
    public String getAuthorizationHeader() {
        String encodedSecretKey = Base64.getEncoder().encodeToString((secretKey + ":")
                        .getBytes(StandardCharsets.UTF_8));
        return PREFIX.concat(encodedSecretKey);
    }
}