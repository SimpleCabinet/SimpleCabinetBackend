package pro.gravit.launchermodules.simplecabinet.commands.cabinet;

import pro.gravit.launchermodules.simplecabinet.SimpleCabinetConfig;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.launchserver.command.Command;
import pro.gravit.utils.helper.LogHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.UUID;

public class MigratorCommand extends Command {
    public MigratorCommand(LaunchServer server) {
        super(server);
    }

    @Override
    public String getArgsDescription() {
        return "[passwordType] [sql]";
    }

    @Override
    public String getUsageDescription() {
        return "";
    }

    @Override
    public void invoke(String... strings) throws Exception {
        verifyArgs(strings, 2);
        SimpleCabinetModule module = server.modulesManager.getModule(SimpleCabinetModule.class);
        SimpleCabinetConfig config = module.configurable.getConfig();
        if (config.migratorSource == null)
            throw new IllegalArgumentException("migratorSource not configured");
        User.HashType type = User.HashType.valueOf(strings[0]);
        try (Connection c = config.migratorSource.getConnection()) {
            PreparedStatement s = c.prepareStatement(strings[1]);
            ResultSet set = s.executeQuery();
            int counter = 0;
            while (set.next()) {
                String username = set.getString("username");
                UUID uuid = UUID.fromString(set.getString("uuid"));
                String encryptedPassword = set.getString("password");
                //long economyMoney = set.getLong("economyMoney");
                //long donateMoney = set.getLong("donateMoney");
                User user = new User();
                user.setUuid(uuid);
                user.setUsername(username);
                user.setRawPassword(encryptedPassword);
                user.setRawPasswordType(type);
                if (set.findColumn("economy_money") > 0)
                    user.setEconomyMoney(set.getLong("economy_money"));
                if (set.findColumn("donate_money") > 0)
                    user.setDonateMoney(set.getDouble("donate_money"));
                if (set.findColumn("extended_money") > 0)
                    user.setExtendedMoney(set.getLong("extended_money"));
                if (set.findColumn("registration_date") > 0)
                    user.setRegistrationDate(set.getTimestamp("registration_date").toLocalDateTime());
                else
                    user.setRegistrationDate(LocalDateTime.now());
                //user.economyMoney = economyMoney;
                //user.donateMoney = donateMoney;
                if (LogHelper.isDebugEnabled()) {
                    LogHelper.debug("User %s uuid %s add to database", user.getUsername(), user.getUuid());
                }
                server.config.dao.userDAO.save(user);
                counter++;
            }
            LogHelper.info("Processed %d users", counter);
        }
    }
}
