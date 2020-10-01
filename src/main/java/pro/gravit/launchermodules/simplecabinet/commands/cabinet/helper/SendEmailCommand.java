package pro.gravit.launchermodules.simplecabinet.commands.cabinet.helper;

import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.launchserver.command.Command;
import pro.gravit.utils.helper.LogHelper;

public class SendEmailCommand extends Command {
    public SendEmailCommand(LaunchServer server) {
        super(server);
    }

    @Override
    public String getArgsDescription() {
        return "[to] [title] [content]";
    }

    @Override
    public String getUsageDescription() {
        return "send raw email";
    }

    @Override
    public void invoke(String... args) throws Exception {
        verifyArgs(args, 3);
        String to = args[0];
        String title = args[1];
        String content = args[2];
        SimpleCabinetModule module = server.modulesManager.getModule(SimpleCabinetModule.class);
        module.mail.simpleSendEmail(to, title, content);
        LogHelper.info("Mail sended to %s", to);
    }
}
