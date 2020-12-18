package pro.gravit.launchermodules.simplecabinet.event;

import pro.gravit.launcher.modules.LauncherModule;
import pro.gravit.launchermodules.simplecabinet.model.User;

public class UserChangedUsernameEvent extends LauncherModule.Event {
    public User user;

    public UserChangedUsernameEvent(User user) {
        this.user = user;
    }
}
