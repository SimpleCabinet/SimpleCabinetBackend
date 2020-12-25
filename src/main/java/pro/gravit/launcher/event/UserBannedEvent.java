package pro.gravit.launcher.event;

import pro.gravit.launcher.request.WebSocketEvent;

public class UserBannedEvent implements WebSocketEvent {
    public String username;
    public boolean hardware;
    public String adminUsername;

    @Override
    public String getType() {
        return "lkUserBanned";
    }
}
