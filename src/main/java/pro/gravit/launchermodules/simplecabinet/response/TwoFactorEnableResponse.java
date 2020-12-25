package pro.gravit.launchermodules.simplecabinet.response;

import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.ClientPermissions;
import pro.gravit.launcher.event.request.TwoFactorEnableRequestEvent;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.model.AuditEntity;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchermodules.simplecabinet.providers.CabinetAuthProvider;
import pro.gravit.launchserver.socket.Client;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

public class TwoFactorEnableResponse extends AbstractUserResponse {
    public byte[] data;
    public int code;

    @Override
    public String getType() {
        return "lkTwoFactorEnable";
    }

    @Override
    public void executeByUser(ChannelHandlerContext channelHandlerContext, User user, boolean self, Client client) throws NoSuchAlgorithmException, InvalidKeyException {
        if (data == null) {
            if (user.getTotpSecretKey() == null) {
                sendError("TwoFactor already disabled");
                return;
            }
            int result = CabinetAuthProvider.generateTotp(user.getTotpSecretKey(), Instant.now());
            if (!self && user.getPermissions().isPermission(ClientPermissions.PermissionConsts.ADMIN)) {
                sendError("Disable 2FA in admin accounts not allowed");
                return;
            }
            if (self && code != result) {
                sendError("Invalid code");
                return;
            }
            user.setTotpSecretKey(null);
            server.config.dao.userDAO.update(user);
            if (!self) {
                server.modulesManager.getModule(SimpleCabinetModule.class).auditService.pushBaseAudit(AuditEntity.AuditType.DISABLE_2FA, (User) client.daoObject, ip, user);
            }
            sendResult(new TwoFactorEnableRequestEvent());
        } else if (data.length == 16) {
            if (user.getTotpSecretKey() != null) {
                sendError("TwoFactor already enabled");
                return;
            }
            int result = CabinetAuthProvider.generateTotp(data, Instant.now());
            if (!self) {
                sendError("This request not allowed");
                return;
            }
            if (code != result) {
                sendError("Invalid code");
                return;
            }
            user.setTotpSecretKey(data);
            server.config.dao.userDAO.update(user);
            sendResult(new TwoFactorEnableRequestEvent());
        } else {
            sendError("Invalid request");
        }
    }
}
