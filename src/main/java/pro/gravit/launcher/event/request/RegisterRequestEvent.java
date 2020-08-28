package pro.gravit.launcher.event.request;

import pro.gravit.launcher.events.RequestEvent;

import java.util.UUID;

public class RegisterRequestEvent extends RequestEvent {
    public String username;
    public UUID uuid;

    public RegisterRequestEvent(String username, UUID uuid) {
        this.username = username;
        this.uuid = uuid;
    }

    @Override
    public String getType() {
        return "lkRegister";
    }
}
