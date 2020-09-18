package pro.gravit.launchermodules.simplecabinet.commands;

import pro.gravit.launchermodules.simplecabinet.commands.cabinet.GroupCommand;
import pro.gravit.launchermodules.simplecabinet.commands.cabinet.MigratorCommand;
import pro.gravit.launchermodules.simplecabinet.commands.cabinet.TestCommand;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.launchserver.command.Command;
import pro.gravit.utils.helper.LogHelper;

import java.util.HashMap;
import java.util.Map;

public class CabinetCommand extends Command {
    public CabinetCommand(LaunchServer server) {
        super(new HashMap<>(), server);
        childCommands.put("migrator", new MigratorCommand(server));
        childCommands.put("group", new GroupCommand(server));
        childCommands.put("test", new TestCommand(server));
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
