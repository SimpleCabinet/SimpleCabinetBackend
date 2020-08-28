package pro.gravit.launchermodules.simplecabinet.response;

import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.ClientPermissions;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.socket.Client;
import pro.gravit.launchserver.socket.response.SimpleResponse;

import java.util.UUID;

public abstract class AbstractUserResponse extends SimpleResponse {
    public String userUsername;
    public UUID userUuid;
    public int userId;
    @Override
    public void execute(ChannelHandlerContext channelHandlerContext, Client client) throws Exception {
        if(!client.isAuth || client.username == null)
        {
            sendError("Permissions denied");
            return;
        }
        if(userUsername == null && userUuid == null && userId == 0)
        {
            if(client.daoObject == null)
            {
                sendError("Your account not connected to lk");
                return;
            }
            User user = (User) client.daoObject;
            executeByUser(channelHandlerContext, user, true, client);
        }
        else
        {
            if(!checkPermissionForNonSelf(client))
            {
                sendError("Permissions denied");
                return;
            }
            User user;
            if(userUuid != null)
            {
                user = (User) server.config.dao.userDAO.findByUUID(userUuid);
            }
            else if(userUsername != null)
            {
                user = (User) server.config.dao.userDAO.findByUsername(userUsername);
            }
            else
            {
                user = (User) server.config.dao.userDAO.findById(userId);
            }
            if(user == null)
            {
                sendError("User not found");
                return;
            }
            executeByUser(channelHandlerContext, user, false, client);
        }
    }
    public boolean checkPermissionForNonSelf(Client client)
    {
        return client.permissions != null && client.permissions.isPermission(ClientPermissions.PermissionConsts.ADMIN);
    }
    public abstract void executeByUser(ChannelHandlerContext channelHandlerContext,  User user, boolean self, Client client);
}
