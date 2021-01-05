package pro.gravit.launchermodules.simplecabinet.commands.cabinet;

import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.launchserver.command.Command;

public class MoneyCommand extends Command {
    public MoneyCommand(LaunchServer server) {
        super(server);
    }

    @Override
    public String getArgsDescription() {
        return "[set/add] [economy/donate/extended] [username] [value]";
    }

    @Override
    public String getUsageDescription() {
        return "add or get user money";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 4);
        String action = args[0];
        String moneyType = args[1];
        String username = args[2];
        double value = Double.parseDouble(args[3]);
        User user = (User) server.config.dao.userDAO.findByUsername(username);
        if(action.equals("add")) {
            switch (moneyType) {
                case "economy":
                    user.setEconomyMoney(user.getEconomyMoney() + (long) value);
                    break;
                case "donate":
                    user.setDonateMoney(user.getDonateMoney() + value);
                    break;
                case "extended":
                    user.setExtendedMoney(user.getExtendedMoney() + value);
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Unknown money type: %s", action));
            }
        }
        else if(action.equals("set")) {
            switch (moneyType) {
                case "economy":
                    user.setEconomyMoney((long) value);
                    break;
                case "donate":
                    user.setDonateMoney(value);
                    break;
                case "extended":
                    user.setExtendedMoney(value);
                    break;
                default:
                    throw new IllegalArgumentException(String.format("Unknown money type: %s", action));
            }
        }
        else {
            throw new IllegalArgumentException(String.format("Action %s not found", action));
        }
        SimpleCabinetModule module = server.modulesManager.getModule(SimpleCabinetModule.class);
        module.syncService.updateUser(user, false, true);
        server.config.dao.userDAO.update(user);
    }
}
