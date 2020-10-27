package pro.gravit.launchermodules.simplecabinet;

import pro.gravit.launcher.config.JsonConfigurable;
import pro.gravit.launcher.modules.LauncherInitContext;
import pro.gravit.launcher.modules.LauncherModule;
import pro.gravit.launcher.modules.LauncherModuleInfo;
import pro.gravit.launcher.modules.events.ClosePhase;
import pro.gravit.launcher.modules.events.PreConfigPhase;
import pro.gravit.launchermodules.simplecabinet.commands.CabinetCommand;
import pro.gravit.launchermodules.simplecabinet.providers.CabinetAuthProvider;
import pro.gravit.launchermodules.simplecabinet.providers.CabinetHWIDProvider;
import pro.gravit.launchermodules.simplecabinet.response.*;
import pro.gravit.launchermodules.simplecabinet.services.PaymentService;
import pro.gravit.launchermodules.simplecabinet.services.SyncService;
import pro.gravit.launchermodules.simplecabinet.severlet.RobokassaSeverlet;
import pro.gravit.launchermodules.simplecabinet.severlet.UnitPaySeverlet;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.launchserver.auth.protect.hwid.HWIDProvider;
import pro.gravit.launchserver.auth.provider.AuthProvider;
import pro.gravit.launchserver.dao.provider.DaoProvider;
import pro.gravit.launchserver.modules.events.LaunchServerInitPhase;
import pro.gravit.launchserver.modules.events.NewLaunchServerInstanceEvent;
import pro.gravit.launchserver.socket.WebSocketService;
import pro.gravit.launchserver.socket.handlers.NettyWebAPIHandler;
import pro.gravit.utils.Version;
import pro.gravit.utils.helper.IOHelper;
import pro.gravit.utils.helper.JVMHelper;
import pro.gravit.utils.helper.LogHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SimpleCabinetModule extends LauncherModule {
    public JsonConfigurable<SimpleCabinetConfig> configurable;
    public SimpleCabinetConfig config;
    public SimpleCabinetMailSender mail;
    public PaymentService paymentService;
    public SyncService syncService;
    public ScheduledExecutorService scheduler;
    public ExecutorService workers;
    public Path baseConfigPath;
    private LaunchServer server;

    public SimpleCabinetModule() {
        super(new LauncherModuleInfo("SimpleCabinet", new Version(1,0,0)));
    }

    @Override
    public void init(LauncherInitContext initContext) {
        registerEvent(this::preConfigPhase, PreConfigPhase.class);
        registerEvent(this::initPhase, LaunchServerInitPhase.class);
        registerEvent(this::getLaunchServerEvent, NewLaunchServerInstanceEvent.class);
        registerEvent(this::closePhase, ClosePhase.class);
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
        WebSocketService.providers.register("lkTwoFactorEnable", TwoFactorEnableResponse.class);
        WebSocketService.providers.register("lkInitPayment", InitPaymentResponse.class);
        WebSocketService.providers.register("lkPasswordReset", PasswordResetResponse.class);
        WebSocketService.providers.register("lkPasswordResetApply", PasswordResetApplyResponse.class);
        NettyWebAPIHandler.addNewSeverlet("lk/unitpay", new UnitPaySeverlet(this));
        NettyWebAPIHandler.addNewSeverlet("lk/robokassa", new RobokassaSeverlet(this));
    }

    public void getLaunchServerEvent(NewLaunchServerInstanceEvent event)
    {
        server = event.launchServer;
    }

    public void initPhase(LaunchServerInitPhase initPhase)
    {
        SimpleCabinetModule module = this;
        baseConfigPath = modulesConfigManager.getModuleConfigDir(moduleInfo.name);
        configurable = new JsonConfigurable<SimpleCabinetConfig>(SimpleCabinetConfig.class, modulesConfigManager.getModuleConfig(moduleInfo.name)) {
            @Override
            public SimpleCabinetConfig getConfig() {
                return module.config;
            }

            @Override
            public void setConfig(SimpleCabinetConfig config) {
                module.config = config;
                module.config.init();
            }

            @Override
            public SimpleCabinetConfig getDefaultConfig() {
                return SimpleCabinetConfig.getDefault();
            }
        };
        try {
            configurable.loadConfig();
        } catch (IOException e) {
            if(!(e instanceof FileNotFoundException)) LogHelper.error(e);
            configurable.setConfig(configurable.getDefaultConfig());
        }
        try {
            Path emailPasswordResetHtml = exportFile(baseConfigPath.resolve("emailPasswordReset.html"), "emailPasswordReset.html");
        } catch (IOException e) {
            LogHelper.error(e);
        }
        if(config.workersCorePoolSize <= 0) config.workersCorePoolSize = 3;
        if(config.schedulerCorePoolSize <= 0) config.schedulerCorePoolSize = 2;
        this.scheduler = Executors.newScheduledThreadPool(config.schedulerCorePoolSize);
        this.workers = Executors.newWorkStealingPool(config.workersCorePoolSize);
        this.mail = new SimpleCabinetMailSender(this);
        this.paymentService = new PaymentService(this, server);
        this.syncService = new SyncService(this, server);
        server.commandHandler.registerCommand("cabinet", new CabinetCommand(server));
    }
    public void closePhase(ClosePhase closePhase)
    {
        if(this.workers != null)
            this.workers.shutdownNow();
        if(this.scheduler != null)
            this.scheduler.shutdownNow();
    }

    private Path exportFile(Path target, String name) throws IOException
    {
        if(Files.exists(target)) return target;
        try(InputStream input = IOHelper.newInput(Objects.requireNonNull(SimpleCabinetModule.class.getClassLoader().getResource("pro/gravit/launchermodules/simplecabinet/".concat(name)))))
        {
            IOHelper.transfer(input, target);
        }
        return target;
    }
}
