package pro.gravit.launchermodules.simplecabinet.commands.cabinet;

import pro.gravit.launchermodules.simplecabinet.commands.cabinet.helper.*;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.launchserver.command.Command;

public class HelperCommand extends Command {
    public HelperCommand(LaunchServer server) {
        super(server);
        childCommands.put("addserver", new AddServerCommand(server));
        childCommands.put("install", new InstallCommand(server));
        childCommands.put("sendemail", new SendEmailCommand(server));
        childCommands.put("enums", new EnumsCommand(server));
        childCommands.put("makeadmin", new MakeAdminCommand(server));
    }

    @Override
    public String getArgsDescription() {
        return "[subcommand] [args]";
    }

    @Override
    public String getUsageDescription() {
        return "Useful utils";
    }

    @Override
    public void invoke(String... args) throws Exception {
        invokeSubcommands(args);
    }
}
