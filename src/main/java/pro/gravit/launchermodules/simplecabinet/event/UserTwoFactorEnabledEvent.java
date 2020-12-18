package pro.gravit.launchermodules.simplecabinet.event;

import pro.gravit.launchermodules.simplecabinet.model.User;

public class UserTwoFactorEnabledEvent {
    public User user;

    public UserTwoFactorEnabledEvent(User user) {
        this.user = user;
    }
}
