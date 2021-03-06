package pro.gravit.launchermodules.simplecabinet.response;

import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.ClientPermissions;
import pro.gravit.launcher.event.UserBannedEvent;
import pro.gravit.launcher.event.request.BanUserRequestEvent;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.dao.SimpleCabinetUserDAO;
import pro.gravit.launchermodules.simplecabinet.model.HardwareId;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.socket.Client;
import pro.gravit.launchserver.socket.response.SimpleResponse;

import java.util.UUID;

public class BanUserResponse extends SimpleResponse {
    public String username;
    public UUID uuid;
    public boolean hardware;

    @Override
    public String getType() {
        return "lkBanUser";
    }

    @Override
    public void execute(ChannelHandlerContext channelHandlerContext, Client client) throws Exception {
        if (client.daoObject == null) {
            sendError("Your account not connected to lk");
            return;
        }
        if (client.permissions == null || !client.permissions.isPermission(ClientPermissions.PermissionConsts.ADMIN)) {
            sendError("Permissions denied");
            return;
        }
        SimpleCabinetDAOProvider dao = (SimpleCabinetDAOProvider) server.config.dao;
        User user = null;
        if (uuid != null) {
            user = ((SimpleCabinetUserDAO) dao.userDAO).findByUUID(uuid);
        }
        if (user == null && username != null) {
            user = ((SimpleCabinetUserDAO) dao.userDAO).findByUsername(username);
        }
        if (user == null) {
            sendError("User not found");
            return;
        }
        HardwareId id = ((SimpleCabinetUserDAO) dao.userDAO).fetchHardwareId(user);
        if (hardware) {
            if (id == null) {
                sendError("User not contains HardwareInfo");
                return;
            }
            id.setBanned(true);
            dao.hwidDAO.update(id);
        }
        ClientPermissions permissions = user.getPermissions();
        permissions.setFlag(ClientPermissions.FlagConsts.BANNED, true);
        user.setPermissions(permissions);
        dao.userDAO.update(user);
        service.kickByUserUUID(user.getUuid(), true);
        server.modulesManager.invokeEvent(new pro.gravit.launchermodules.simplecabinet.event.UserBannedEvent(user, hardware));
        sendResult(new BanUserRequestEvent());
        UserBannedEvent event = new UserBannedEvent();
        event.username = user.getUsername();
        event.hardware = hardware;
        event.adminUsername = client.daoObject.getUsername();
        service.forEachActiveChannels((ch, wsHandler) -> {
            Client cClient = wsHandler.getClient();
            if (cClient != null && cClient.permissions != null && cClient.permissions.isPermission(ClientPermissions.PermissionConsts.MANAGEMENT)) {
                service.sendObject(ch, event);
            }
        });
    }
}
