package pro.gravit.launchermodules.simplecabinet.commands;

import pro.gravit.launchermodules.simplecabinet.commands.cabinet.*;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.launchserver.command.Command;

import java.util.HashMap;

public class CabinetCommand extends Command {
    public CabinetCommand(LaunchServer server) {
        super(new HashMap<>(), server);
        childCommands.put("migrator", new MigratorCommand(server));
        childCommands.put("group", new GroupCommand(server));
        childCommands.put("test", new TestCommand(server));
        childCommands.put("check", new CheckCommand(server));
        childCommands.put("helper", new HelperCommand(server));
        childCommands.put("money", new MoneyCommand(server));
    }


    @Override
    public String getArgsDescription() {
        return "[subcomand] [args]";
    }

    @Override
    public String getUsageDescription() {
        return "main cabinet command";
    }

    @Override
    public void invoke(String... strings) throws Exception {
        invokeSubcommands(strings);
    }
}
