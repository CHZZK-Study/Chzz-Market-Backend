package org.chzz.market.domain.payment.service;

import java.lang.reflect.InvocationTargetException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public abstract class PaymentAuthorizationHeaderProvider {
    private static final String PREFIX = "Basic ";

    protected final String secretKey;
    protected final String clientKey;

    public PaymentAuthorizationHeaderProvider(
            @Value("${payment.toss.security-key}") String secretKey,
            @Value("${payment.toss.client-key}") String clientKey) {
        this.secretKey = secretKey;
        this.clientKey = clientKey;
    }

    public abstract String getAuthorizationHeader();

    @Component
    public static class Factory {
        private final String secretKey;
        private final String clientKey;

        public Factory(@Value("${payment.toss.security-key}") String secretKey,
                       @Value("${payment.toss.client-key}") String clientKey) {
            this.secretKey = secretKey;
            this.clientKey = clientKey;
        }

        public <T extends PaymentAuthorizationHeaderProvider> T create(Class<T> clazz)
                throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
            return clazz.getDeclaredConstructor(String.class, String.class)
                    .newInstance(secretKey, clientKey);
        }
    }
}