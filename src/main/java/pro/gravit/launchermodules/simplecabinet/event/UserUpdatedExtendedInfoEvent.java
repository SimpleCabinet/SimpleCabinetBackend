package pro.gravit.launchermodules.simplecabinet.event;

import pro.gravit.launcher.modules.LauncherModule;
import pro.gravit.launchermodules.simplecabinet.model.User;

public class UserUpdatedExtendedInfoEvent extends LauncherModule.Event {
    public User user;

    public UserUpdatedExtendedInfoEvent(User user) {
        this.user = user;
    }
}
