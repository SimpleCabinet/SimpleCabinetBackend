package pro.gravit.launchermodules.simplecabinet.commands.cabinet;

import pro.gravit.launchermodules.simplecabinet.SimpleCabinetConfig;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.dao.SimpleCabinetUserDAO;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchermodules.simplecabinet.model.UserGroup;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.launchserver.command.Command;
import pro.gravit.utils.helper.LogHelper;

import java.time.LocalDateTime;

public class GroupCommand extends Command {
    public GroupCommand(LaunchServer server) {
        super(server);
    }

    @Override
    public String getArgsDescription() {
        return "[add/remove] [groupName] [username] (days)";
    }

    @Override
    public String getUsageDescription() {
        return "";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 3);
        SimpleCabinetModule module = server.modulesManager.getModule(SimpleCabinetModule.class);
        SimpleCabinetConfig config = module.config;
        SimpleCabinetConfig.GroupEntity group = config.findGroupByName(args[1]);
        if (group == null) {
            throw new IllegalArgumentException(String.format("Group %s not found", args[1]));
        }
        User user = (User) server.config.dao.userDAO.findByUsername(args[2]);
        ((SimpleCabinetUserDAO) server.config.dao.userDAO).fetchGroups(user);
        if (user == null) {
            throw new IllegalArgumentException(String.format("User %s not found", args[2]));
        }
        if (args[0].equals("add")) {
            UserGroup entity = null;
            for (UserGroup e : user.getGroups()) {
                if (e.getGroupName().equals(group.name)) {
                    entity = e;
                    break;
                }
            }
            if (entity != null) {
                LogHelper.error("User %s already contains group %s (end time: %s )", user.getUsername(), group.name, entity.getEndDate().toString());
            } else {
                LocalDateTime date = LocalDateTime.now();
                LocalDateTime endTime;
                if (args.length > 3) {
                    endTime = date.plusDays(Long.parseLong(args[3]));
                } else endTime = null;
                UserGroup userGroup = new UserGroup();
                userGroup.setUser(user);
                userGroup.setEndDate(endTime);
                userGroup.setStartDate(date);
                userGroup.setGroupName(group.name);
                //((SimpleCabinetUserDAO) server.config.dao.userDAO).save(userGroup);
                user.getGroups().add(userGroup);
                server.config.dao.userDAO.update(user);
                module.syncService.updateUser(user, false, true);
                LogHelper.info("Successful added group %s to %s (end time %s)", group.name, user.getUsername(), endTime == null ? "no" : endTime.toString());
            }
        } else if (args[0].equals("remove")) {
            UserGroup entity = null;
            for (UserGroup e : user.getGroups()) {
                if (e.getGroupName().equals(group.name)) {
                    entity = e;
                    break;
                }
            }
            if (entity == null) {
                LogHelper.error("User %s no contains group %s", user.getUsername(), group.name);
                return;
            }
            ((SimpleCabinetUserDAO) server.config.dao.userDAO).delete(entity);
            user.getGroups().remove(entity);
            server.config.dao.userDAO.update(user);
            module.syncService.updateUser(user, false, true);
            LogHelper.info("Successful removed group %s from %s", group.name, user.getUsername());
        } else {
            LogHelper.error("Action %s not found", args[0]);
        }
    }
}
