package pro.gravit.launchermodules.simplecabinet.response;

import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.ClientPermissions;
import pro.gravit.launcher.event.request.ChangePasswordRequestEvent;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.event.UserChangedPasswordEvent;
import pro.gravit.launchermodules.simplecabinet.model.AuditEntity;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.socket.Client;
import pro.gravit.launchserver.socket.response.SimpleResponse;

public class ChangePasswordResponse extends AbstractUserResponse {
    public String username;
    public String oldPassword;
    public String newPassword;
    @Override
    public String getType() {
        return "lkChangePassword";
    }

    @Override
    public void executeByUser(ChannelHandlerContext channelHandlerContext, User user, boolean self, Client client) {
        if(self && (oldPassword == null || !user.verifyPassword(oldPassword)) )
        {
            sendError("oldPassword wrong");
            return;
        }
        if(newPassword == null || newPassword.length() < 4 || newPassword.length() > 32)
        {
            sendError("Password length invalid");
            return;
        }
        user.setPassword(newPassword);
        server.config.dao.userDAO.update(user);
        server.modulesManager.getModule(SimpleCabinetModule.class).auditService.pushBaseAudit(AuditEntity.AuditType.CHANGE_PASSWORD, (User) client.daoObject, ip, user);
        server.modulesManager.invokeEvent(new UserChangedPasswordEvent(user));
        sendResult(new ChangePasswordRequestEvent());
    }
}
