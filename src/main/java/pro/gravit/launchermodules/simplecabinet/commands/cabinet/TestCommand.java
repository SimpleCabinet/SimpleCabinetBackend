package pro.gravit.launchermodules.simplecabinet.commands.cabinet;

import com.google.gson.JsonElement;
import pro.gravit.launcher.HTTPRequest;
import pro.gravit.launcher.Launcher;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.launchserver.command.Command;
import pro.gravit.utils.helper.LogHelper;
import pro.gravit.utils.helper.SecurityHelper;

import java.net.URL;

public class TestCommand extends Command {
    public TestCommand(LaunchServer server) {
        super(server);
    }

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return null;
    }

    @Override
    public void invoke(String... args) throws Exception {
        SimpleCabinetModule module = server.modulesManager.getModule(SimpleCabinetModule.class);
        StringBuilder builder = new StringBuilder(module.config.payments.unitPay.url+"?method=initPayment");
        builder.append(formatParam("paymentType", args[0]));
        builder.append(formatParam("account", args[1]));
        builder.append(formatParam("sum", args[2]));
        builder.append(formatParam("projectId", String.valueOf(module.config.payments.unitPay.projectId)));
        builder.append(formatParam("resultUrl", module.config.payments.unitPay.resultUrl));
        builder.append(formatParam("desc", args[4]));
        builder.append(formatParam("ip", "8.8.8.8"));
        builder.append(formatParam("test", module.config.payments.unitPay.testMode ? "1" : "0"));
        if(module.config.payments.unitPay.testMode)
            builder.append(formatParam("login", module.config.payments.unitPay.login));
        builder.append(formatParam("secretKey", module.config.payments.unitPay.secretKey));
        String signatureString = String.format("%s{up}%s{up}%s{up}%s", args[1], args[4], args[2], module.config.payments.unitPay.secretKey);
        String hex = SecurityHelper.toHex(SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256, signatureString));
        LogHelper.dev("Digest %s", hex);
        builder.append(formatParam("signature", hex));
        URL url = new URL(builder.toString());
        LogHelper.info(builder.toString());
        JsonElement result = HTTPRequest.jsonRequest(null, "GET", url);
        LogHelper.info("Output: %s", Launcher.gsonManager.configGson.toJson(result));
    }

    public String formatParam(String key, String value) {
        return String.format("&params[%s]=%s", key, value);
    }
}
