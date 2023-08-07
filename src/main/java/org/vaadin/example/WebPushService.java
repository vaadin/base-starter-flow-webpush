package org.vaadin.example;

import nl.martijndwars.webpush.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.server.webpush.WebPush;
import com.vaadin.flow.server.webpush.WebPushMessage;

@Service
public class WebPushService {

    @Value("${public.key}")
    private String publicKey;
    @Value("${private.key}")
    private String privateKey;
    @Value("${subject}")
    private String subject;

    private final Map<String, Subscription> endpointToSubscription = new HashMap<>();

    WebPush webPush;

    public WebPush getWebPush() {
        if(webPush == null) {
            webPush = new WebPush(publicKey, privateKey, subject);
        }
        return webPush;
    }

    /**
     * Send a notification to all subscriptions.
     *
     * @param title message title
     * @param body message body
     */
    public void notifyAll(String title, String body) {
        endpointToSubscription.values().forEach(subscription -> {
            webPush.sendNotification(subscription, new WebPushMessage(title, body));
        });
    }

    private Logger getLogger() {
        return LoggerFactory.getLogger(WebPushService.class);
    }

    public void store(Subscription subscription) {
        getLogger().info("Subscribed to {}", subscription.endpoint);
        /*
         * Note, in a real world app you'll want to persist these
         * in the backend. Also, you probably want to know which
         * subscription belongs to which user to send custom messages
         * for different users. In this demo, we'll just use
         * endpoint URL as key to store subscriptions in memory.
         */
        endpointToSubscription.put(subscription.endpoint, subscription);
    }


    public void remove(Subscription subscription) {
        getLogger().info("Unsubscribed {}", subscription.endpoint);
        endpointToSubscription.remove(subscription.endpoint);
    }

    public boolean isEmpty() {
        return endpointToSubscription.isEmpty();
    }

}
