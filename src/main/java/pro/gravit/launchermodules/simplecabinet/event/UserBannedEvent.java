package pro.gravit.launchermodules.simplecabinet.event;

import pro.gravit.launcher.modules.LauncherModule;
import pro.gravit.launchermodules.simplecabinet.model.User;

public class UserBannedEvent extends LauncherModule.Event {
    public User user;
    public boolean isHardware;

    public UserBannedEvent(User user, boolean isHardware) {
        this.user = user;
        this.isHardware = isHardware;
    }
}
