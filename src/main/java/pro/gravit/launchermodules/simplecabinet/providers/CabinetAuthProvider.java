package pro.gravit.launchermodules.simplecabinet.providers;

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator;
import pro.gravit.launcher.ClientPermissions;
import pro.gravit.launcher.events.request.AuthRequestEvent;
import pro.gravit.launcher.request.auth.AuthRequest;
import pro.gravit.launcher.request.auth.password.Auth2FAPassword;
import pro.gravit.launcher.request.auth.password.AuthECPassword;
import pro.gravit.launcher.request.auth.password.AuthPlainPassword;
import pro.gravit.launcher.request.auth.password.AuthTOTPPassword;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.launchserver.auth.AuthException;
import pro.gravit.launchserver.auth.provider.AuthProvider;
import pro.gravit.launchserver.auth.provider.AuthProviderDAOResult;
import pro.gravit.launchserver.auth.provider.AuthProviderResult;
import pro.gravit.utils.helper.IOHelper;
import pro.gravit.utils.helper.SecurityHelper;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

public class CabinetAuthProvider extends AuthProvider {
    private transient LaunchServer server;
    private transient SimpleCabinetModule module;

    public static int generateTotp(byte[] keyBytes, Instant time) throws InvalidKeyException, NoSuchAlgorithmException {

        TimeBasedOneTimePasswordGenerator totp = new TimeBasedOneTimePasswordGenerator();
        SecretKeySpec signingKey = new SecretKeySpec(keyBytes, totp.getAlgorithm());
        return totp.generateOneTimePassword(signingKey, time);
    }

    @Override
    public void init(LaunchServer srv) {
        this.server = srv;
        module = server.modulesManager.getModule(SimpleCabinetModule.class);
    }

    @Override
    public AuthProviderResult auth(String login, AuthRequest.AuthPasswordInterface password, String ip) throws Exception {
        User user = (User) server.config.dao.userDAO.findByUsername(login);
        if (user == null) {
            throw new AuthException("User or password incorrect");
        }
        if (user.getPermissions().isFlag(ClientPermissions.FlagConsts.BANNED)) {
            throw new AuthException("User banned");
        }
        String firstPassword = null;
        String totpPassword = null;
        if(password instanceof AuthPlainPassword) {
            firstPassword = ((AuthPlainPassword) password).password;
        }
        else if(password instanceof Auth2FAPassword) {
            if (((Auth2FAPassword) password).firstPassword instanceof AuthECPassword) {
                firstPassword = IOHelper.decode(SecurityHelper.decrypt(server.runtime.passwordEncryptKey
                        , ((AuthECPassword) ((Auth2FAPassword) password).firstPassword).password));
            }
            else if(((Auth2FAPassword) password).firstPassword instanceof AuthPlainPassword) {
                firstPassword = ((AuthPlainPassword) ((Auth2FAPassword) password).firstPassword).password;
            }
            if(((Auth2FAPassword) password).secondPassword instanceof AuthTOTPPassword) {
                totpPassword = ((AuthTOTPPassword) ((Auth2FAPassword) password).secondPassword).totp;
            }
        }
        if(firstPassword == null) {
            throw new AuthException("Password type wrong");
        }
        if (!user.verifyPassword(firstPassword)) {
            throw new AuthException("User or password incorrect");
        }
        byte[] keyBytes = user.getTotpSecretKey();
        if(keyBytes != null) {
            if (totpPassword == null) {
                throw new AuthException(AuthRequestEvent.TWO_FACTOR_NEED_ERROR_MESSAGE);
            }
            int result = generateTotp(keyBytes, Instant.now());
            try {
                int totp = Integer.parseInt(totpPassword);
                if (totp != result) {
                    throw new AuthException("TOTP password wrong");
                }
            } catch (NumberFormatException e) {
                throw new AuthException("TOTP password not number");
            }
        }
        return new AuthProviderDAOResult(user.getUsername(), SecurityHelper.randomStringToken(), user.getPermissions(), user);
    }

    @Override
    public void close() throws IOException {

    }
}
