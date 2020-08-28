package pro.gravit.launchermodules.simplecabinet;

import org.hibernate.cfg.Configuration;
import pro.gravit.launchermodules.simplecabinet.dao.SimpleCabinetHwidDAO;
import pro.gravit.launchermodules.simplecabinet.dao.SimpleCabinetUserDAO;
import pro.gravit.launchermodules.simplecabinet.model.HardwareId;
import pro.gravit.launchermodules.simplecabinet.model.HardwareIdLogEntity;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchermodules.simplecabinet.model.converter.UUIDConverter;
import pro.gravit.launchserver.dao.UserDAO;
import pro.gravit.launchserver.dao.provider.HibernateDaoProvider;
import pro.gravit.utils.command.Command;
import pro.gravit.utils.command.SubCommand;
import pro.gravit.utils.helper.LogHelper;

import java.util.Map;
import java.util.UUID;

public class SimpleCabinetDAOProvider extends HibernateDaoProvider {
    public transient SimpleCabinetHwidDAO hwidDAO;
    public boolean stringUUID = false;
    @Override
    protected void onConfigure(Configuration configuration) {
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(HardwareId.class);
        configuration.addAnnotatedClass(HardwareIdLogEntity.class);
        if(stringUUID)
        {
            configuration.addAnnotatedClass(UUIDConverter.class);
        }
        try {
            Thread.currentThread().getContextClassLoader().loadClass(User.class.getName());
        } catch (ClassNotFoundException e) {
            LogHelper.warning("ClassLoading bug detected. Your LaunchServer <5.1.7");
            Thread.currentThread().setContextClassLoader(SimpleCabinetUserDAO.class.getClassLoader());
        }
    }

    @Override
    protected UserDAO newUserDAO() {
        hwidDAO = new SimpleCabinetHwidDAO(sessionFactory);
        return new SimpleCabinetUserDAO(sessionFactory);
    }

    @Override
    public Map<String, Command> getCommands() {
        Map<String, Command> commands = super.getCommands();
        commands.put("register", new SubCommand() {
            @Override
            public void invoke(String... args) throws Exception {
                verifyArgs(args, 3);
                String login = args[0];
                String password = args[1];
                String email = args[2];
                if(!User.isCorrectEmail(email))
                {
                    LogHelper.error("Invalid email");
                    return;
                }
                SimpleCabinetUserDAO dao = (SimpleCabinetUserDAO) userDAO;
                User user = new User();
                user.setUsername(login);
                user.setEmail(email);
                user.setPassword(password);
                //Generate
                user.setUuid(UUID.randomUUID());
                //Save
                dao.save(user);
            }
        });
        return commands;
    }
}
