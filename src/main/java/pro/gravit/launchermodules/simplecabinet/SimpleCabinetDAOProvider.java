package pro.gravit.launchermodules.simplecabinet;

import com.eatthepath.otp.HmacOneTimePasswordGenerator;
import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import org.apache.commons.codec.binary.Base32;
import org.hibernate.cfg.Configuration;
import pro.gravit.launchermodules.simplecabinet.dao.*;
import pro.gravit.launchermodules.simplecabinet.model.*;
import pro.gravit.launchermodules.simplecabinet.model.converter.UUIDConverter;
import pro.gravit.launchserver.dao.UserDAO;
import pro.gravit.launchserver.dao.provider.HibernateDaoProvider;
import pro.gravit.utils.command.Command;
import pro.gravit.utils.command.SubCommand;
import pro.gravit.utils.helper.LogHelper;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class SimpleCabinetDAOProvider extends HibernateDaoProvider {
    public transient SimpleCabinetHwidDAO hwidDAO;
    public transient SimpleCabinetPaymentDAO paymentDAO;
    public transient SimpleCabinetAuditDAO auditDAO;
    public transient SimpleCabinetProductDAO productDAO;
    public transient SimpleCabinetOrderDAO orderDAO;
    public boolean stringUUID = false;
    @Override
    protected void onConfigure(Configuration configuration) {
        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(HardwareId.class);
        configuration.addAnnotatedClass(HardwareIdLogEntity.class);
        configuration.addAnnotatedClass(UserGroup.class);
        configuration.addAnnotatedClass(PaymentId.class);
        configuration.addAnnotatedClass(PasswordResetEntity.class);
        configuration.addAnnotatedClass(AuditEntity.class);
        configuration.addAnnotatedClass(ProductEntity.class);
        configuration.addAnnotatedClass(OrderEntity.class);
        if(stringUUID)
        {
            configuration.addAnnotatedClass(UUIDConverter.class);
        }
        try {
            Thread.currentThread().getContextClassLoader().loadClass(User.class.getName());
        } catch (ClassNotFoundException e) {
            LogHelper.warning("ClassLoading bug detected. Your LaunchServer <5.1.8");
            Thread.currentThread().setContextClassLoader(SimpleCabinetUserDAO.class.getClassLoader());
        }
    }

    @Override
    protected UserDAO newUserDAO() {
        hwidDAO = new SimpleCabinetHwidDAO(sessionFactory);
        paymentDAO = new SimpleCabinetPaymentDAO(sessionFactory);
        auditDAO = new SimpleCabinetAuditDAO(sessionFactory);
        productDAO = new SimpleCabinetProductDAO(sessionFactory);
        orderDAO = new SimpleCabinetOrderDAO(sessionFactory);
        return new SimpleCabinetUserDAO(sessionFactory);
    }

    public boolean isOpen() {
        return sessionFactory != null && sessionFactory.isOpen();
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
        commands.put("enable2fa", new SubCommand() {
            @Override
            public void invoke(String... args) throws Exception {
                verifyArgs(args, 1);
                User user = (User) userDAO.findByUsername(args[0]);
                if(user == null) {
                    throw new IllegalArgumentException("User not found");
                }
                final Key key;
                {
                    final KeyGenerator keyGenerator = KeyGenerator.getInstance(HmacOneTimePasswordGenerator.HOTP_HMAC_ALGORITHM);

                    // SHA-1 and SHA-256 prefer 64-byte (512-bit) keys; SHA512 prefers 128-byte (1024-bit) keys
                    keyGenerator.init(128);

                    key = keyGenerator.generateKey();
                }
                byte[] secretKey = key.getEncoded();
                user.setTotpSecretKey(secretKey);
                userDAO.update(user);
                LogHelper.info("User 2FA Key now: %s", new Base32().encodeAsString(secretKey));
            }
        });
        commands.put("disable2fa", new SubCommand() {
            @Override
            public void invoke(String... args) throws Exception {
                verifyArgs(args, 1);
                User user = (User) userDAO.findByUsername(args[0]);
                if(user == null) {
                    throw new IllegalArgumentException("User not found");
                }
                user.setTotpSecretKey(null);
                userDAO.update(user);
                LogHelper.info("User 2FA disabled");
            }
        });
        commands.put("check2fa", new SubCommand() {
            @Override
            public void invoke(String... args) throws Exception {
                verifyArgs(args, 1);
                User user = (User) userDAO.findByUsername(args[0]);
                if(user == null) {
                    throw new IllegalArgumentException("User not found");
                }
                TimeBasedOneTimePasswordGenerator totp = new TimeBasedOneTimePasswordGenerator();
                SecretKeySpec signingKey = new SecretKeySpec(user.getTotpSecretKey(), totp.getAlgorithm());
                int result = totp.generateOneTimePassword(signingKey, Instant.now());
                LogHelper.info("Generated 2FA key: %d", result);
            }
        });
        return commands;
    }
}
