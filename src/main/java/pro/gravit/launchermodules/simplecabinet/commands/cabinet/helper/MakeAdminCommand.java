package pro.gravit.launchermodules.simplecabinet.commands.cabinet.helper;

import pro.gravit.launcher.ClientPermissions;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.dao.SimpleCabinetUserDAO;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.launchserver.command.Command;
import pro.gravit.utils.helper.LogHelper;

public class MakeAdminCommand extends Command {
    public MakeAdminCommand(LaunchServer server) {
        super(server);
    }

    @Override
    public String getArgsDescription() {
        return "[username]";
    }

    @Override
    public String getUsageDescription() {
        return "give admin permission to user";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 1);
        SimpleCabinetDAOProvider dao = (SimpleCabinetDAOProvider) server.config.dao;
        SimpleCabinetUserDAO userDAO = (SimpleCabinetUserDAO) dao.userDAO;
        SimpleCabinetModule module = server.modulesManager.getModule(SimpleCabinetModule.class);
        User user = userDAO.findByUsername(args[0]);
        if(user == null) {
            LogHelper.error("User %s not found", args[0]);
            return;
        }
        ClientPermissions permissions = user.getPermissions();
        permissions.setPermission(ClientPermissions.PermissionConsts.ADMIN, true);
        user.setPermissions(permissions);
        userDAO.update(user);
        module.syncService.updateUser(user, true, true);
        LogHelper.info("Give admin permission to user %s(UUID %s)", user.getUsername(), user.getUuid());
    }
}
