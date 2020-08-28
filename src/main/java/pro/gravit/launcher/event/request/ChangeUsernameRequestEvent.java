package pro.gravit.launcher.event.request;

import pro.gravit.launcher.events.RequestEvent;

public class ChangeUsernameRequestEvent extends RequestEvent {
    @Override
    public String getType() {
        return "lkChangeUsername";
    }
}
