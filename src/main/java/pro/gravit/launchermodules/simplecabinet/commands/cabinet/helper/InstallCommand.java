package pro.gravit.launchermodules.simplecabinet.commands.cabinet.helper;

import pro.gravit.launcher.Launcher;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.providers.CabinetAuthProvider;
import pro.gravit.launchermodules.simplecabinet.providers.CabinetHWIDProvider;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.launchserver.auth.AuthProviderPair;
import pro.gravit.launchserver.auth.handler.HibernateAuthHandler;
import pro.gravit.launchserver.auth.protect.AdvancedProtectHandler;
import pro.gravit.launchserver.command.Command;
import pro.gravit.utils.helper.LogHelper;

public class InstallCommand extends Command {
    public InstallCommand(LaunchServer server) {
        super(server);
    }

    @Override
    public String getArgsDescription() {
        return "[]";
    }

    @Override
    public String getUsageDescription() {
        return "interactive install script";
    }

    public SimpleCabinetDAOProvider configureDatabase() throws Exception
    {
        LogHelper.info("Aluriel: Configure database");
        System.out.print("Print your database engine(mysql/postgresql): ");
        String dbType = server.commandHandler.readLine();
        String driverName;
        String jdbcUrlName;
        boolean stringUUID;
        String dialect = null;
        switch (dbType.toLowerCase())
        {
            case "mysql": {
                driverName = "com.mysql.cj.jdbc.Driver";
                jdbcUrlName = "mysql";
                stringUUID = true;
                LogHelper.info("Aluriel: MySQL database require \"dialect\"");
                LogHelper.info("Aluriel: MariaDB 10.3 or higher is recommended");
                System.out.print("Are you using MariaDB 10.3 or higher?(Y/N) ");
                String reply = server.commandHandler.readLine();
                if(reply.toLowerCase().equals("y"))
                {
                    dialect = "org.hibernate.dialect.MariaDB103Dialect";
                }
                else {
                    System.out.print("Press your dialect(org.hibernate.dialect.MariaDB103Dialect, org.hibernate.dialect.MySQL8Dialect or other): ");
                    dialect = server.commandHandler.readLine();
                    Class.forName(dialect);
                }
                break;
            }
            case "postgresql": {
                driverName = "org.postgresql.Driver";
                jdbcUrlName = "postgresql";
                stringUUID = false;
                break;
            }
            default: {
                LogHelper.error("Aluriel: Unknown db name %s", dbType);
                LogHelper.error("Aluriel: If you used other database please configure manually");
                throw new RuntimeException("Failed configure database: db name invalid");
            }
        }
        System.out.print("Print database address(localhost, localhost:7070, db.yourserver.ru, 10.0.0.2): ");
        String address = server.commandHandler.readLine();
        System.out.print("Print database username: ");
        String username = server.commandHandler.readLine();
        System.out.print("Print database password: ");
        String password = server.commandHandler.readLine();
        System.out.print("Print database name: ");
        String database = server.commandHandler.readLine();
        LogHelper.info("Configuration:");
        String url = String.format("jdbc:%s://%s/%s", jdbcUrlName, address, database);

        SimpleCabinetDAOProvider daoProvider = new SimpleCabinetDAOProvider();
        daoProvider.driver = driverName;
        daoProvider.username = username;
        daoProvider.password = password;
        daoProvider.url = url;
        daoProvider.dialect = dialect;
        daoProvider.stringUUID = stringUUID;
        daoProvider.pool_size = "4";
        String serializedDaoProvider = Launcher.gsonManager.configGson.toJson(daoProvider);
        LogHelper.debug("\"dao\": %s", serializedDaoProvider);
        return daoProvider;

    }

    @Override
    public void invoke(String... args) throws Exception {
        LogHelper.subInfo("Aluriel: Hi, I'm a bot girl from the GravitLauncher discord server");
        LogHelper.subInfo("Aluriel: I will help you setup SimpleCabinet");
        if(!(server.config.dao instanceof SimpleCabinetDAOProvider))
        {
            SimpleCabinetDAOProvider daoProvider = configureDatabase();
            System.out.print("Aluriel: Do you want to re-create tables?(ALL DATA WILL BE DELETED)(Y/N): ");
            String reply = server.commandHandler.readLine();
            if(reply.toLowerCase().equals("y"))
            {
                System.getProperties().setProperty("hibernate.hbm2ddl.auto", "create");
                LogHelper.info("Aluriel: Okay, you want to re-create tables");
            }
            else {
                System.getProperties().setProperty("hibernate.hbm2ddl.auto", "update");
                LogHelper.info("Aluriel: Okay, if possible, the data will be saved");
            }
            //daoProvider.init(server);
            LogHelper.info("Wait 2 seconds...");
            Thread.sleep(2000);
            if(daoProvider.isOpen()) LogHelper.info("Successful initialization!");
            else {
                LogHelper.error("Failed initialization (!)");
                daoProvider.close();
                return;
            }
            if(server.config.dao instanceof AutoCloseable) ((AutoCloseable) server.config.dao).close();
            server.unregisterObject("dao", server.config.dao);
            server.config.dao = daoProvider;
            server.registerObject("dao", server.config.dao);
        }
        if(!(server.config.protectHandler instanceof AdvancedProtectHandler) || !((AdvancedProtectHandler) server.config.protectHandler).enableHardwareFeature)
        {
            LogHelper.warning("Aluriel: HWIDProvider not configured. Skip");
        }
        else if(!(((AdvancedProtectHandler) server.config.protectHandler).provider instanceof CabinetHWIDProvider))
        {
            AdvancedProtectHandler protectHandler = ((AdvancedProtectHandler) server.config.protectHandler);
            if(protectHandler.provider != null) protectHandler.provider.close();
            CabinetHWIDProvider hwidProvider = new CabinetHWIDProvider();
            hwidProvider.criticalCompareLevel = 1.0;
            protectHandler.provider = hwidProvider;
        }
        AuthProviderPair pair = server.config.getAuthProviderPair("std");
        if(pair == null)
            pair = server.config.getAuthProviderPair();
        if(pair == null) {
            LogHelper.error("Aluriel: Not found correct auth id. Critical error");
            return;
        }
        LogHelper.info("Aluriel: Selected auth id %s", pair.name);
        if(!(pair.handler instanceof HibernateAuthHandler))
        {
            pair.handler = new HibernateAuthHandler();
        }
        if(!(pair.provider instanceof CabinetAuthProvider))
        {
            pair.provider = new CabinetAuthProvider();
        }
        LogHelper.info("Alurial: Save LaunchServer config");
        server.launchServerConfigManager.writeConfig(server.config);
        LogHelper.subInfo("Aluriel: Completed! Thank you for using my help");
        LogHelper.subInfo("Aluriel: I recommend restarting the launchserver and checking the installation with the \"cabinet check\" command");
        LogHelper.subInfo("Aluriel: If you have any problems, contact me in the support of the GravitLauncher discord server.");
    }
}
