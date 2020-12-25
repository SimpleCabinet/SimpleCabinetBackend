package pro.gravit.launchermodules.simplecabinet.commands.cabinet;

import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.providers.CabinetAuthProvider;
import pro.gravit.launchermodules.simplecabinet.providers.CabinetHWIDProvider;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.launchserver.auth.AuthProviderPair;
import pro.gravit.launchserver.auth.handler.HibernateAuthHandler;
import pro.gravit.launchserver.auth.protect.AdvancedProtectHandler;
import pro.gravit.launchserver.command.Command;
import pro.gravit.utils.helper.LogHelper;

public class CheckCommand extends Command {
    public CheckCommand(LaunchServer server) {
        super(server);
    }

    @Override
    public String getArgsDescription() {
        return "[]";
    }

    @Override
    public String getUsageDescription() {
        return "Check requirements";
    }

    @Override
    public void invoke(String... args) throws Exception {
        if (!(server.config.dao instanceof SimpleCabinetDAOProvider)) {
            LogHelper.error("[Check] DAO: FAIL");
        } else {
            LogHelper.info("[Check] DAO: OK");
        }
        if (!(server.config.protectHandler instanceof AdvancedProtectHandler) || !((AdvancedProtectHandler) server.config.protectHandler).enableHardwareFeature) {
            LogHelper.warning("[Check] HardwareId: NOT CONFIGURED");
        } else if (!(((AdvancedProtectHandler) server.config.protectHandler).provider instanceof CabinetHWIDProvider)) {
            LogHelper.error("[Check] hardwareId: FAIL");
        } else {
            LogHelper.info("[Check] hardwareId: OK");
        }
        if (server.config.netty.disableWebApiInterface) {
            LogHelper.error("[Check] WebSeverlet: FAIL");
        } else {
            LogHelper.info("[Check] WebSeverlet: OK");
        }
        AuthProviderPair pair = server.config.getAuthProviderPair("std");
        if (pair == null)
            pair = server.config.getAuthProviderPair();
        if (pair == null) {
            LogHelper.error("[Check] Not found correct auth id. Critical error");
            return;
        }
        if (!pair.name.equals("std")) {
            LogHelper.warning("[Check] You use not default auth id name: %s. Recommended name: std", pair.name);
        }
        LogHelper.info("[Check] Selected auth id %s", pair.name);
        if (!(pair.handler instanceof HibernateAuthHandler)) {
            LogHelper.error("[Check] AuthHandler: FAIL");
        } else {
            LogHelper.info("[Check] AuthHandler: OK");
        }
        if (!(pair.provider instanceof CabinetAuthProvider)) {
            LogHelper.error("[Check] AuthProvider: FAIL");
        } else {
            LogHelper.info("[Check] AuthProvider: OK");
        }
        if (server.config.netty.address.startsWith("ws://")) {
            LogHelper.warning("[Check] Your websocket address not used ssl( wss:// )");
            LogHelper.warning("SimpleCabinet Frontend may be not worked from HTTPS");
            LogHelper.warning("Browsers not allowed HTTPS to HTTP requests");
        }
        //Check libraries
        try {
            Class.forName("org.mindrot.jbcrypt.BCrypt");
            LogHelper.info("[Check][Libraries] BCrypt: OK");
        } catch (ClassNotFoundException e) {
            LogHelper.error("[Check][Libraries] BCrypt: FAIL");
        }

        try {
            Class.forName("org.apache.commons.codec.binary.Base32");
            LogHelper.info("[Check][Libraries] Apache Commons Codec: OK");
        } catch (ClassNotFoundException e) {
            LogHelper.error("[Check][Libraries] Apache Commons Codec: FAIL");
        }

        try {
            Class.forName("com.eatthepath.otp.TimeBasedOneTimePasswordGenerator");
            LogHelper.info("[Check][Libraries] Eatthepath otp: OK");
        } catch (ClassNotFoundException e) {
            LogHelper.error("[Check][Libraries] Eatthepath otp: FAIL");
        }
    }
}
