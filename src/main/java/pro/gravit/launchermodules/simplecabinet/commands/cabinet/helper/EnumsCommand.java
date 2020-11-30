package pro.gravit.launchermodules.simplecabinet.commands.cabinet.helper;

import pro.gravit.launchermodules.simplecabinet.model.*;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.launchserver.command.Command;
import pro.gravit.launchserver.command.service.SecurityCheckCommand;
import pro.gravit.utils.helper.LogHelper;

import java.util.HashSet;
import java.util.Set;

public class EnumsCommand extends Command {
    public Set<Class<? extends Enum<?>>> enums = new HashSet<>();
    public EnumsCommand(LaunchServer server) {
        super(server);
        enums.add(AuditEntity.AuditType.class);
        enums.add(OrderEntity.OrderStatus.class);
        enums.add(PaymentId.PaymentStatus.class);
        enums.add(ProductEntity.ProductType.class);
        enums.add(User.HashType.class);
        enums.add(User.Gender.class);
    }

    @Override
    public String getArgsDescription() {
        return "[]";
    }

    @Override
    public String getUsageDescription() {
        return "show all enums element";
    }

    @Override
    public void invoke(String... args) throws Exception {
        for(Class<? extends Enum<?>> clazz : enums)
        {
            printEnum(clazz);
        }
    }

    public static void printEnum(Class<? extends Enum<?>> clazz)
    {
        try {
            SecurityCheckCommand.printCheckResult(LogHelper.Level.INFO, clazz.getSimpleName(), String.format(">>> %s <<<", clazz.getSimpleName()), true);
            Enum<?>[] values = clazz.getEnumConstants();
            for(Enum<?> element : values) {
                SecurityCheckCommand.printCheckResult(LogHelper.Level.INFO, String.valueOf(element.ordinal()), element.name(), true);
            }
        } catch (Throwable throwable) {
            LogHelper.error(throwable);
        }
    }
}
