package pro.gravit.launcher.event.request;

import pro.gravit.launcher.events.RequestEvent;

public class BanUserRequestEvent extends RequestEvent {
    @Override
    public String getType() {
        return "lkBanUser";
    }
}
