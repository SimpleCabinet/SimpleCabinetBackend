package pro.gravit.launchermodules.simplecabinet.event;

import pro.gravit.launcher.modules.LauncherModule;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchermodules.simplecabinet.response.UploadSkinResponse;

import java.nio.file.Path;

public class UploadedSkinEvent extends LauncherModule.Event {
    public User user;
    public UploadSkinResponse.SkinType type;
    public Path path;

    public UploadedSkinEvent(User user, UploadSkinResponse.SkinType type, Path path) {
        this.user = user;
        this.type = type;
        this.path = path;
    }
}
