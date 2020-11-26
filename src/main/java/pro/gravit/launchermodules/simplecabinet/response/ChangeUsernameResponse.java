package pro.gravit.launchermodules.simplecabinet.response;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.event.request.ChangeUsernameRequestEvent;
import pro.gravit.launcher.events.request.ExitRequestEvent;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.model.AuditEntity;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.socket.Client;
import pro.gravit.launchserver.socket.handlers.WebSocketFrameHandler;
import pro.gravit.launchserver.socket.response.auth.ExitResponse;

public class ChangeUsernameResponse extends AbstractUserResponse {
    public String newUsername;
    @Override
    public void executeByUser(ChannelHandlerContext channelHandlerContext, User user, boolean self, Client client) {
        if(self)
        {
            sendError("Change username for yourself not allow");
            return;
        }
        if(server.config.dao.userDAO.findByUsername(newUsername) != null)
        {
            sendError("Username busy");
            return;
        }
        service.forEachActiveChannels(((channel, webSocketFrameHandler) -> {
            Client client1 = webSocketFrameHandler.getClient();
            if (!client1.isAuth || !newUsername.equals(client1.username)) return;
            ExitResponse.exit(server, webSocketFrameHandler, channel, ExitRequestEvent.ExitReason.SERVER);
        }));
        user.setUsername(newUsername);
        server.config.dao.userDAO.update(user);
        server.modulesManager.getModule(SimpleCabinetModule.class).auditService.pushBaseAudit(AuditEntity.AuditType.CHANGE_USERNAME, (User) client.daoObject, ip, user);
        sendResult(new ChangeUsernameRequestEvent());
    }

    @Override
    public String getType() {
        return "lkChangeUsername";
    }
}
