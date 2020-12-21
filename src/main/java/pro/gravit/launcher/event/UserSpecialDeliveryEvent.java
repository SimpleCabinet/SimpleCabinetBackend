package pro.gravit.launcher.event;

import pro.gravit.launcher.request.WebSocketEvent;

public class UserSpecialDeliveryEvent implements WebSocketEvent {
    @Override
    public String getType() {
        return "lkUserSpecialDelivery";
    }
}
