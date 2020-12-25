package pro.gravit.launchermodules.simplecabinet.commands.cabinet.helper;

import pro.gravit.launcher.ClientPermissions;
import pro.gravit.launchermodules.simplecabinet.dao.SimpleCabinetUserDAO;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.launchserver.command.Command;
import pro.gravit.utils.helper.LogHelper;
import pro.gravit.utils.helper.SecurityHelper;

import java.util.UUID;

public class AddServerCommand extends Command {
    public AddServerCommand(LaunchServer server) {
        super(server);
    }

    @Override
    public String getArgsDescription() {
        return "[username]";
    }

    @Override
    public String getUsageDescription() {
        return "register new user and give server permissions";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 1);
        SimpleCabinetUserDAO dao = (SimpleCabinetUserDAO) server.config.dao.userDAO;
        User user = new User();
        String username = args[0];
        if (!username.endsWith("Bot") && !username.endsWith("bot")) {
            username = username.concat("Bot");
        }
        user.setUsername(username);
        user.setStatus("Beep. Boop. I'm a Minecraft server bot");
        user.setGender(SecurityHelper.newRandom().nextBoolean() ? User.Gender.FEMALE : User.Gender.MALE);
        user.setEmail(String.format("%s@%s.robot.world", username.substring(0, username.length() - 2).toLowerCase(), server.config.projectName.toLowerCase()));
        user.setUuid(UUID.randomUUID());
        String password = SecurityHelper.randomStringToken();
        user.setPassword(password);
        ClientPermissions permissions = user.getPermissions();
        permissions.setPermission(ClientPermissions.PermissionConsts.MANAGEMENT, true);
        permissions.setFlag(ClientPermissions.FlagConsts.SYSTEM, true);
        user.setPermissions(permissions);
        dao.save(user);
        LogHelper.info("Your login %s | password: %s", user.getUsername(), password);
        LogHelper.info("Using this params in ServerWrapper");
    }
}
