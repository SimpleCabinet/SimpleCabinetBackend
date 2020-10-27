package pro.gravit.launchermodules.simplecabinet.response;

import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.event.request.PasswordResetRequestEvent;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.dao.SimpleCabinetUserDAO;
import pro.gravit.launchermodules.simplecabinet.model.PasswordResetEntity;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.socket.Client;
import pro.gravit.launchserver.socket.response.SimpleResponse;
import pro.gravit.utils.helper.IOHelper;
import pro.gravit.utils.helper.LogHelper;

import java.util.UUID;

public class PasswordResetResponse extends SimpleResponse {
    public String email;

    @Override
    public String getType() {
        return "lkPasswordReset";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) throws Exception {
        if(email == null || email.isBlank()) {
            sendError("Invalid request");
            return;
        }
        SimpleCabinetModule module = server.modulesManager.getModule(SimpleCabinetModule.class);
        SimpleCabinetDAOProvider dao = (SimpleCabinetDAOProvider) server.config.dao;
        SimpleCabinetUserDAO userDAO = (SimpleCabinetUserDAO) dao.userDAO;
        User user = userDAO.findByEmail(email);
        if(user == null) {
            //Skip
            sendError("User not found");
            return;
        } else {
            module.workers.submit(() -> {
            try {
                LogHelper.debug("User %s request password reset (start)", user.getEmail());
                PasswordResetEntity passwordResetEntity = new PasswordResetEntity();
                passwordResetEntity.setUser(user);
                passwordResetEntity.setUuid(UUID.randomUUID());
                userDAO.save(passwordResetEntity);
                long id = passwordResetEntity.getId();
                UUID uuid = passwordResetEntity.getUuid();
                try {
                    String url = String.format("%s/cb/passwordreset/%d/%s", module.config.urls.frontendUrl, id, uuid.toString());
                    String template = new String(IOHelper.read(module.baseConfigPath.resolve("emailPasswordReset.html")));
                    String content = String.format(template, user.getUsername(), url);
                    module.mail.simpleSendEmail(user.getEmail(), "Account Password Reset", content);
                    LogHelper.debug("User %s request password reset", user.getEmail());
                } catch (Throwable e) {
                    LogHelper.error(e);
                    userDAO.delete(passwordResetEntity);
                }
                } catch(Throwable ex) {
                LogHelper.error(ex);
                }
            });
        }
        sendResult(new PasswordResetRequestEvent());
    }
}
