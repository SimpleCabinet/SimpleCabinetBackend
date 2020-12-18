package pro.gravit.launchermodules.simplecabinet.response;

import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.event.request.RegisterRequestEvent;
import pro.gravit.launchermodules.simplecabinet.dao.SimpleCabinetUserDAO;
import pro.gravit.launchermodules.simplecabinet.event.UserRegisteredEvent;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.manangers.hook.AuthHookManager;
import pro.gravit.launchserver.socket.Client;
import pro.gravit.launchserver.socket.response.SimpleResponse;
import pro.gravit.utils.HookException;
import pro.gravit.utils.helper.VerifyHelper;

import java.time.LocalDateTime;
import java.util.UUID;

public class RegisterResponse extends SimpleResponse {
    public String username;
    public String email;
    public String password;
    public User.Gender gender;
    @Override
    public String getType() {
        return "lkRegister";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) throws Exception {
        if(username == null || password == null || !VerifyHelper.isValidUsername(username) || !User.isCorrectEmail(email))
        {
            sendError("Incorrect request");
            return;
        }
        if(password.length() < 4 || password.length() > 32)
        {
            sendError("Password length invalid");
            return;
        }
        AuthHookManager.RegContext context = new AuthHookManager.RegContext(username, password, ip, false);
        try {
            server.authHookManager.registraion.hook(context);
        } catch (HookException e)
        {
            sendError(e.getMessage());
            return;
        }
        SimpleCabinetUserDAO dao = (SimpleCabinetUserDAO) server.config.dao.userDAO;
        if(dao.findByUsername(username) != null || dao.findByEmail(email) != null)
        {
            sendError("User already registered");
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setUuid(UUID.randomUUID());
        user.setEmail(email);
        user.setGender(gender);
        user.setRegistrationDate(LocalDateTime.now());
        dao.save(user);
        server.modulesManager.invokeEvent(new UserRegisteredEvent(user, ip));
        sendResult(new RegisterRequestEvent(username, user.getUuid()));
    }
}
