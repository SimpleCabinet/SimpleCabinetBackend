package pro.gravit.launchermodules.simplecabinet.event;

import pro.gravit.launcher.modules.LauncherModule;
import pro.gravit.launchermodules.simplecabinet.model.User;

public class UserPasswordResetEvent extends LauncherModule.Event {
    public User user;
    public String ip;

    public UserPasswordResetEvent(User user, String ip) {
        this.user = user;
        this.ip = ip;
    }
}
