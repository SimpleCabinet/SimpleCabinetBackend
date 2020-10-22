package pro.gravit.launchermodules.simplecabinet.response;

import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.event.request.PasswordResetApplyRequestEvent;
import pro.gravit.launchermodules.simplecabinet.dao.SimpleCabinetUserDAO;
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
        sendResult(new PasswordResetApplyRequestEvent());
    }
}
