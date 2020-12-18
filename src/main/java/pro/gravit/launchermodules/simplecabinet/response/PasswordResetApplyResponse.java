package pro.gravit.launchermodules.simplecabinet.response;

import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.event.request.PasswordResetApplyRequestEvent;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.dao.SimpleCabinetUserDAO;
import pro.gravit.launchermodules.simplecabinet.event.UserPasswordResetApplyEvent;
import pro.gravit.launchermodules.simplecabinet.model.AuditEntity;
import pro.gravit.launchermodules.simplecabinet.model.PasswordResetEntity;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.socket.Client;
import pro.gravit.launchserver.socket.response.SimpleResponse;

import java.util.UUID;

public class PasswordResetApplyResponse extends SimpleResponse {
    public long id;
    public UUID uuid;
    public String newPassword;
    @Override
    public String getType() {
        return "lkPasswordResetApply";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) throws Exception {
        if(id == 0 || uuid == null || newPassword == null) {
            sendError("Invalid request");
            return;
        }
        if(newPassword.length() < 4 || newPassword.length() > 32)
        {
            sendError("Password length invalid");
            return;
        }
        SimpleCabinetUserDAO dao = (SimpleCabinetUserDAO) server.config.dao.userDAO;
        PasswordResetEntity entity = dao.findPasswordResetById(id);
        if(entity == null || !entity.getUuid().equals(uuid)) {
            sendError("Invalid password reset token");
            return;
        }
        User user = dao.fetchUserInPasswordResetEntity(entity);
        user.setPassword(newPassword);
        dao.update(user);
        dao.delete(entity);
        server.modulesManager.getModule(SimpleCabinetModule.class).auditService.pushBaseAudit(AuditEntity.AuditType.PASSWORD_RESET, user, ip, user);
        server.modulesManager.invokeEvent(new UserPasswordResetApplyEvent(user, ip));
        sendResult(new PasswordResetApplyRequestEvent());
    }
}
