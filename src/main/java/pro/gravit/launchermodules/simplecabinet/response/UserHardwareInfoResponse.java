package pro.gravit.launchermodules.simplecabinet.response;

import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.event.request.UserHardwareInfoRequestEvent;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.dao.SimpleCabinetUserDAO;
import pro.gravit.launchermodules.simplecabinet.model.HardwareId;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.socket.Client;

public class UserHardwareInfoResponse extends AbstractUserResponse {
    @Override
    public String getType() {
        return "lkUserHardwareInfo";
    }

    @Override
    public void executeByUser(ChannelHandlerContext channelHandlerContext, User user, boolean self, Client client) throws Exception {
        if (self) {
            sendError("Show self hardware not allowed");
            return;
        }
        SimpleCabinetDAOProvider dao = (SimpleCabinetDAOProvider) server.config.dao;
        HardwareId id = ((SimpleCabinetUserDAO) dao.userDAO).fetchHardwareId(user);
        sendResult(new UserHardwareInfoRequestEvent(id == null ? null : id.toHardwareInfo(), id == null ? null : id.getPublicKey()));
    }
}
