package pro.gravit.launcher.event.request;

import pro.gravit.launcher.events.RequestEvent;

import java.util.Map;

public class InitPaymentRequestEvent extends RequestEvent {
    public enum PaymentVariant {
        ROBOKASSA
    }
    public String redirectUri;
    public String method;
    public Map<String, String> params;

    public InitPaymentRequestEvent() {
    }

    public InitPaymentRequestEvent(String redirectUri, String method, Map<String, String> params) {
        this.redirectUri = redirectUri;
        this.method = method;
        this.params = params;
    }

    @Override
    public String getType() {
        return "lkInitPayment";
    }
}
