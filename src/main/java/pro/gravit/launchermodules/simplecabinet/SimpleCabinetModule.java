package pro.gravit.launchermodules.simplecabinet;

import pro.gravit.launcher.config.JsonConfigurable;
import pro.gravit.launcher.config.SimpleConfigurable;
import pro.gravit.launcher.event.request.ExtendedInfoRequestEvent;
import pro.gravit.launcher.modules.LauncherInitContext;
import pro.gravit.launcher.modules.LauncherModule;
import pro.gravit.launcher.modules.LauncherModuleInfo;
import pro.gravit.launcher.modules.events.PreConfigPhase;
import pro.gravit.launchermodules.simplecabinet.commands.CabinetCommand;
import pro.gravit.launchermodules.simplecabinet.providers.CabinetAuthProvider;
import pro.gravit.launchermodules.simplecabinet.providers.CabinetHWIDProvider;
import pro.gravit.launchermodules.simplecabinet.response.*;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.launchserver.auth.protect.hwid.HWIDProvider;
import pro.gravit.launchserver.auth.provider.AuthProvider;
import pro.gravit.launchserver.dao.provider.DaoProvider;
import pro.gravit.launchserver.modules.events.LaunchServerInitPhase;
import pro.gravit.launchserver.modules.events.NewLaunchServerInstanceEvent;
import pro.gravit.launchserver.socket.WebSocketService;
import pro.gravit.utils.Version;
import pro.gravit.utils.helper.LogHelper;

import java.io.IOException;

public class SimpleCabinetModule extends LauncherModule {
    public JsonConfigurable<SimpleCabinetConfig> configurable;
    private LaunchServer server;

    public SimpleCabinetModule() {
        super(new LauncherModuleInfo("SimpleCabinet", new Version(1,0,0)));
    }

    @Override
    public void init(LauncherInitContext initContext) {
        registerEvent(this::preConfigPhase, PreConfigPhase.class);
        registerEvent(this::initPhase, LaunchServerInitPhase.class);
        registerEvent(this::getLaunchServerEvent, NewLaunchServerInstanceEvent.class);
    }

    public void preConfigPhase(PreConfigPhase preConfigPhase)
    {
        DaoProvider.providers.register("simplecabinet", SimpleCabinetDAOProvider.class);
        HWIDProvider.providers.register("cabinet", CabinetHWIDProvider.class);
        AuthProvider.providers.register("cabinet", CabinetAuthProvider.class);
        WebSocketService.providers.register("lkExtendedInfo", ExtendedInfoResponse.class);
        WebSocketService.providers.register("lkUploadSkin", UploadSkinResponse.class);
        WebSocketService.providers.register("lkChangePassword", ChangePasswordResponse.class);
        WebSocketService.providers.register("lkUpdateExtendedInfo", UpdateExtendedInfoResponse.class);
        WebSocketService.providers.register("lkChangeUsername", ChangeUsernameResponse.class);
        WebSocketService.providers.register("lkRegister", RegisterResponse.class);
    }

    public void getLaunchServerEvent(NewLaunchServerInstanceEvent event)
    {
        server = event.launchServer;
    }

    public void initPhase(LaunchServerInitPhase initPhase)
    {
        configurable = modulesConfigManager.getConfigurable(SimpleCabinetConfig.class, moduleInfo.name);
        try {
            configurable.loadConfig();
        } catch (IOException e) {
            LogHelper.error(e);
            configurable.setConfig(configurable.getDefaultConfig());
        }
        server.commandHandler.registerCommand("cabinet", new CabinetCommand(server));
    }
}
