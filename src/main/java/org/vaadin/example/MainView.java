package org.vaadin.example;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.webpush.WebPush;

@Route
public class MainView extends VerticalLayout {

    private TextArea message;

    private Button broadcast;
    private Button subscribe;
    private Button unsubscribe;

    WebPushService webPushService;

    WebPush pushApi;

    public MainView(WebPushService webPushService) {
        this.webPushService = webPushService;
        message = new TextArea("Message");

        broadcast = new Button("Broadcast message");
        subscribe = new Button("Subscribe");
        unsubscribe = new Button("UnSubscribe");

        broadcast.addClickListener(e ->
                webPushService.notifyAll("Message from administration", message.getValue())
        );
        broadcast.addClickShortcut(Key.ENTER);

        pushApi = webPushService.getWebPush();

        subscribe.setEnabled(false);
        subscribe.addClickListener(e -> {
            pushApi.subscribe(subscribe.getUI().get(), subscription -> {
                webPushService.store(subscription);
                subscribe.setEnabled(false);
                unsubscribe.setEnabled(true);
            });
        });

        unsubscribe.setEnabled(false);
        unsubscribe.addClickListener(e -> {
            pushApi.unsubscribe(subscribe.getUI().get(), subscription -> {
                webPushService.remove(subscription);
                subscribe.setEnabled(true);
                unsubscribe.setEnabled(false);
            });
        });

        setMargin(true);

        HorizontalLayout subscription = new HorizontalLayout(subscribe,
                unsubscribe);
        add(subscription, message, broadcast);
        setHorizontalComponentAlignment(Alignment.CENTER, message, broadcast, subscription);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        UI ui = attachEvent.getUI();
        pushApi.subscriptionExists(ui, registered -> {
            subscribe.setEnabled(!registered);
            unsubscribe.setEnabled(registered);
            if(registered && webPushService.isEmpty()) {
                pushApi.fetchExistingSubscription(ui, webPushService::store);
            }
        });
    }
}
