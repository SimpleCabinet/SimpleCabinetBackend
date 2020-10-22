package pro.gravit.launcher.event.request;

import pro.gravit.launcher.events.RequestEvent;

public class PasswordResetApplyRequestEvent extends RequestEvent {
    @Override
    public String getType() {
        return "lkPasswordResetApply";
    }
}
