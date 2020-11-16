package pro.gravit.launchermodules.simplecabinet.providers;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import pro.gravit.launcher.events.request.AuthRequestEvent;
import pro.gravit.launcher.request.auth.AuthRequest;
import pro.gravit.launcher.request.auth.password.Auth2FAPassword;
import pro.gravit.launcher.request.auth.password.AuthECPassword;
import pro.gravit.launcher.request.auth.password.AuthPlainPassword;
import pro.gravit.launcher.request.auth.password.AuthTOTPPassword;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.dao.SimpleCabinetUserDAO;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.launchserver.auth.AuthException;
import pro.gravit.launchserver.auth.provider.AuthProvider;
import pro.gravit.launchserver.auth.provider.AuthProviderDAOResult;
import pro.gravit.launchserver.auth.provider.AuthProviderResult;
import pro.gravit.utils.helper.IOHelper;
import pro.gravit.utils.helper.SecurityHelper;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

public class CabinetAuthProvider extends AuthProvider {
    private transient LaunchServer server;
    private transient SimpleCabinetModule module;
    @Override
    public void init(LaunchServer srv) {
        this.server = srv;
        module = server.modulesManager.getModule(SimpleCabinetModule.class);
    }

    @Override
    public AuthProviderResult auth(String login, AuthRequest.AuthPasswordInterface password, String ip) throws Exception {
        User user = (User) server.config.dao.userDAO.findByUsername(login);
        if(user == null)
        {
            throw new AuthException("User or password incorrect");
        }
        byte[] keyBytes = user.getTotpSecretKey();
        if(keyBytes != null)
        {
            if(password instanceof Auth2FAPassword && ((Auth2FAPassword) password).firstPassword instanceof AuthECPassword)
            {
                ((Auth2FAPassword) password).firstPassword = new AuthPlainPassword(IOHelper.decode(SecurityHelper.decrypt(server.runtime.passwordEncryptKey
                        , ((AuthECPassword) ((Auth2FAPassword) password).firstPassword).password)));
            }
            if(!(password instanceof Auth2FAPassword) || !( ((Auth2FAPassword)password).secondPassword instanceof AuthTOTPPassword ) || !( ((Auth2FAPassword)password).firstPassword instanceof AuthPlainPassword ) )
            {
                throw new AuthException(AuthRequestEvent.TWO_FACTOR_NEED_ERROR_MESSAGE);
            }
            Auth2FAPassword auth2FAPassword = (Auth2FAPassword) password;
            AuthPlainPassword firstPassword = (AuthPlainPassword) auth2FAPassword.firstPassword;
            AuthTOTPPassword secondPassword = (AuthTOTPPassword) auth2FAPassword.secondPassword;
            if(!user.verifyPassword(firstPassword.password))
            {
                throw new AuthException("User or password incorrect");
            }
            int result = generateTotp(keyBytes, Instant.now());
            try {
                int totpPassword = Integer.parseInt(secondPassword.totp);
                if(totpPassword != result)
                {
                    throw new AuthException("TOTP password wrong");
                }
            } catch (NumberFormatException e)
            {
                throw new AuthException("TOTP password not number");
            }
        }
        else
        {
            if(!(password instanceof AuthPlainPassword))
            {
                throw new AuthException("Password type not supported");
            }
            if(!user.verifyPassword(((AuthPlainPassword) password).password))
            {
                throw new AuthException("User or password incorrect");
            }
        }
        return new AuthProviderDAOResult(user.getUsername(), SecurityHelper.randomStringToken(), user.getPermissions(), user);
    }

    public static int generateTotp(byte[] keyBytes, Instant time) throws InvalidKeyException, NoSuchAlgorithmException {

        TimeBasedOneTimePasswordGenerator totp = new TimeBasedOneTimePasswordGenerator();
        SecretKeySpec signingKey = new SecretKeySpec(keyBytes, totp.getAlgorithm());
        return totp.generateOneTimePassword(signingKey, time);
    }

    @Override
    public void close() throws IOException {

    }
}
