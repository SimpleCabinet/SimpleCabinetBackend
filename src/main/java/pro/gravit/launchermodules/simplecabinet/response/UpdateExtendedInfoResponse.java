package pro.gravit.launchermodules.simplecabinet.response;

import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.event.request.UpdateExtendedInfoRequestEvent;
import pro.gravit.launchermodules.simplecabinet.event.UserUpdatedExtendedInfoEvent;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.socket.Client;

public class UpdateExtendedInfoResponse extends AbstractUserResponse {
    public User.Gender gender;
    public String status;

    @Override
    public String getType() {
        return "lkUpdateExtendedInfo";
    }

    @Override
    public void executeByUser(ChannelHandlerContext channelHandlerContext, User user, boolean self, Client client) {
        user.setGender(gender);
        user.setStatus(status);
        server.config.dao.userDAO.update(user);
        server.modulesManager.invokeEvent(new UserUpdatedExtendedInfoEvent(user));
        sendResult(new UpdateExtendedInfoRequestEvent());
    }
}
