package pro.gravit.launchermodules.simplecabinet.services;

import pro.gravit.launcher.event.request.ExtendedInfoRequestEvent;
import pro.gravit.launcher.events.request.CurrentUserRequestEvent;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.dao.SimpleCabinetUserDAO;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchermodules.simplecabinet.response.ExtendedInfoResponse;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.launchserver.socket.Client;
import pro.gravit.launchserver.socket.response.profile.ProfileByUUIDResponse;

public class SyncService {
    private transient final SimpleCabinetModule module;
    private transient final LaunchServer server;

    public SyncService(SimpleCabinetModule module, LaunchServer server) {
        this.module = module;
        this.server = server;
    }

    public void updateUser(User user, boolean isUserInfo, boolean isExtInfo) {
        SimpleCabinetUserDAO userDAO = (SimpleCabinetUserDAO) server.config.dao.userDAO;
        userDAO.fetchGroups(user);
        String username = user.getUsername();
        ExtendedInfoRequestEvent extendedInfoRequestEvent;
        CurrentUserRequestEvent currentUserRequestEvent;
        if (isExtInfo) {
            extendedInfoRequestEvent = ExtendedInfoResponse.fetchExtendedInfo(module, user, true, false);
        } else extendedInfoRequestEvent = null;
        if (isUserInfo) {
            CurrentUserRequestEvent.UserInfo userInfo = new CurrentUserRequestEvent.UserInfo();
            userInfo.permissions = user.getPermissions();
            userInfo.playerProfile = ProfileByUUIDResponse.getProfile(user.getUuid(), user.getUsername(), "", server.config.getAuthProviderPair().textureProvider);
            currentUserRequestEvent = new CurrentUserRequestEvent(userInfo);
        } else currentUserRequestEvent = null;
        server.nettyServerSocketHandler.nettyServer.service.forEachActiveChannels(((channel, webSocketFrameHandler) -> {
            Client client = webSocketFrameHandler.getClient();
            if (client.isAuth && username.equals(client.username) && client.daoObject != null) {
                client.daoObject = user;
                client.permissions = user.getPermissions();
                if (isExtInfo)
                    webSocketFrameHandler.service.sendObject(channel, extendedInfoRequestEvent);
                if (isUserInfo)
                    webSocketFrameHandler.service.sendObject(channel, currentUserRequestEvent);
            }
        }));
    }

    public boolean deleteOlderUserGroups() {
        if (server.config.dao != null && ((SimpleCabinetDAOProvider) server.config.dao).isOpen()) {
            ((SimpleCabinetUserDAO) server.config.dao.userDAO).deleteOrderUserGroups();
            return true;
        } else {
            return false;
        }
    }
}
