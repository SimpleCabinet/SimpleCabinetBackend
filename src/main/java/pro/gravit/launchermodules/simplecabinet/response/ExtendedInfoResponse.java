package pro.gravit.launchermodules.simplecabinet.response;

import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.ClientPermissions;
import pro.gravit.launcher.event.request.ExtendedInfoRequestEvent;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetConfig;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.dao.SimpleCabinetUserDAO;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchermodules.simplecabinet.model.UserGroup;
import pro.gravit.launchserver.socket.Client;
import pro.gravit.launchserver.socket.response.SimpleResponse;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

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
        dao.fetchGroups(user);
        ClientPermissions permissions = user.getPermissions();
        if(permissions.isFlag(ClientPermissions.FlagConsts.HIDDEN) && !(client.permissions != null && client.permissions.isPermission(ClientPermissions.PermissionConsts.ADMIN)))
        {
            sendError("User not found");
            return;
        }
        ExtendedInfoRequestEvent event;
        SimpleCabinetModule module = server.modulesManager.getModule(SimpleCabinetModule.class);
        event = fetchExtendedInfo(module, user, client.isAuth && client.username.equals(user.getUsername()), ( client.permissions != null && client.permissions.isPermission(ClientPermissions.PermissionConsts.ADMIN)));
        sendResult(event);
    }

    public static ExtendedInfoRequestEvent fetchExtendedInfo(SimpleCabinetModule module, User user, boolean self, boolean admin) {
        ClientPermissions permissions = user.getPermissions();
        ExtendedInfoRequestEvent event = new ExtendedInfoRequestEvent();
        if(self || admin) {
            event.email = user.getEmail();
            event.donateMoney = user.getDonateMoney();
            event.economyMoney = user.getEconomyMoney();
            event.extendedMoney = user.getExtendedMoney();
            event.privateUserZone = fillPrivateUserZone(user);
        }
        event.gender = user.getGender();
        event.status = user.getStatus();
        event.registrationDate = user.getRegistrationDate() == null ? null : ExtendedInfoRequestEvent.formatDate(user.getRegistrationDate().toLocalDate());
        event.isBanned = permissions.isFlag(ClientPermissions.FlagConsts.BANNED);
        event.groups = ExtendedInfoRequestEvent.ExtendedGroup.getGroupsByClientPermissions(user.getPermissions());
        event.groups.addAll(user.getGroups().stream().map(e -> getUserGroupPublicInfo(module, e)).collect(Collectors.toList()));
        return event;
    }

    public static ExtendedInfoRequestEvent.ExtendedGroup getUserGroupPublicInfo(SimpleCabinetModule module, UserGroup group)
    {
        SimpleCabinetConfig.GroupEntity entity = module.config.findGroupByName(group.getGroupName());
        ExtendedInfoRequestEvent.ExtendedGroup result = new ExtendedInfoRequestEvent.ExtendedGroup(entity.name, entity.displayName);
        if(group.getEndDate() != null) {
            result.days = (int) Duration.between(LocalDateTime.now(), group.getEndDate()).toDays();
        }
        return result;
    }

    public static ExtendedInfoRequestEvent.PrivateUserZone fillPrivateUserZone(User user)
    {
        ExtendedInfoRequestEvent.PrivateUserZone privateUserZone = new ExtendedInfoRequestEvent.PrivateUserZone();
        privateUserZone.isEnabled2FA = (user.getTotpSecretKey() != null);
        return privateUserZone;
    }
}
