package pro.gravit.launchermodules.simplecabinet.event;

import pro.gravit.launcher.modules.LauncherModule;
import pro.gravit.launchermodules.simplecabinet.model.User;

public class UserPasswordResetApplyEvent extends LauncherModule.Event {
    public User user;
    public String ip;

    public UserPasswordResetApplyEvent(User user, String ip) {
        this.user = user;
        this.ip = ip;
    }
}
