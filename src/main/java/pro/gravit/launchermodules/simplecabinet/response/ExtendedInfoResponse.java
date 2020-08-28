package pro.gravit.launchermodules.simplecabinet.response;

import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.ClientPermissions;
import pro.gravit.launcher.event.request.ExtendedInfoRequestEvent;
import pro.gravit.launchermodules.simplecabinet.dao.SimpleCabinetUserDAO;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.socket.Client;
import pro.gravit.launchserver.socket.response.SimpleResponse;

import java.util.UUID;

public class ExtendedInfoResponse extends SimpleResponse {
    public String username;
    public UUID uuid;
    @Override
    public String getType() {
        return "lkExtendedInfo";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) throws Exception {
        User user;
        SimpleCabinetUserDAO dao = (SimpleCabinetUserDAO) server.config.dao.userDAO;
        if(uuid != null)
        {
            user = dao.findByUUID(uuid);
        }
        else if(username != null)
        {
            user = dao.findByUsername(username);
        }
        else if(client.daoObject != null)
        {
            user = (User) client.daoObject;
        }
        else user = null;
        if(user == null)
        {
            sendError("User not found");
            return;
        }
        ClientPermissions permissions = user.getPermissions();
        if(permissions.isFlag(ClientPermissions.FlagConsts.HIDDEN) && !(client.permissions != null && client.permissions.isPermission(ClientPermissions.PermissionConsts.ADMIN)))
        {
            sendError("User not found");
            return;
        }
        ExtendedInfoRequestEvent event = new ExtendedInfoRequestEvent();
        if(client.isAuth && client.username.equals(user.getUsername()) || ( client.permissions != null && client.permissions.isPermission(ClientPermissions.PermissionConsts.ADMIN)))
        {
            event.email = user.getEmail();
            event.donateMoney = user.getDonateMoney();
            event.economyMoney = user.getEconomyMoney();
            event.extendedMoney = user.getExtendedMoney();
        }
        event.gender = user.getGender();
        event.status = user.getStatus();
        event.isBanned = permissions.isFlag(ClientPermissions.FlagConsts.BANNED);
        event.groups = ExtendedInfoRequestEvent.ExtendedGroup.getGroupsByClientPermissions(user.getPermissions());
        sendResult(event);
    }
}
