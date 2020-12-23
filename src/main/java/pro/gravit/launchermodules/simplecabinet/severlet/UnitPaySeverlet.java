package pro.gravit.launchermodules.simplecabinet.severlet;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import net.bytebuddy.implementation.bytecode.Throw;
import org.apache.commons.codec.Charsets;
import pro.gravit.launcher.Launcher;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.services.PaymentService;
import pro.gravit.launchserver.socket.NettyConnectContext;
import pro.gravit.launchserver.socket.handlers.NettyWebAPIHandler;
import pro.gravit.utils.helper.LogHelper;
import pro.gravit.utils.helper.SecurityHelper;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class UnitPaySeverlet implements NettyWebAPIHandler.SimpleSeverletHandler {
    private final SimpleCabinetModule module;

    private Pattern pattern = Pattern.compile("params%5B(?<key>.+)%5D=(?<value>.+)");
    private Pattern pattern2 = Pattern.compile("(?<key>.+)=(?<value>.+)");

    public UnitPaySeverlet(SimpleCabinetModule module) {
        this.module = module;
    }

    static class UnitPayString {
        public final String message;

        UnitPayString(String message) {
            this.message = message;
        }
    }
    static class UnitPayResult {
        public UnitPayString result;
        public UnitPayString error;
    }
    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest msg, NettyConnectContext context) throws Exception {
        LogHelper.dev("UnitPay request: %s", msg.uri());
        StringTokenizer tokenizer = new StringTokenizer(msg.uri(), "?&");
        if(!tokenizer.hasMoreTokens()) {

            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
            // Close the connection as soon as the error message is sent.
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        String tok = tokenizer.nextToken();
        MessageDigest digest = MessageDigest.getInstance("SHA256");
        String token;
        List<PaymentService.KeyValuePair> hashParams = new ArrayList<>();
        String method = null;
        String paramSignature = null;
        int paymentId = 0;
        double sum = 0.0;
        while(tokenizer.hasMoreTokens())
        {
            token = tokenizer.nextToken();

            Matcher matcher = pattern.matcher(token);
            if(matcher.matches())
            {
                String key = matcher.group("key");
                String value = matcher.group("value");
                LogHelper.dev("UnitKey: %s = %s", key, value);
                if(!key.equals("signature"))
                {
                    if(key.equals("orderSum")) {
                        sum = Double.parseDouble(value);
                    }
                    else if(key.equals("account")) {
                        if(!value.equals("test"))
                        paymentId = Integer.parseInt(value);
                    }
                    hashParams.add(new PaymentService.KeyValuePair(key, value));
                } else {
                    paramSignature = value;
                }
            }
            else {
                matcher = pattern2.matcher(token);
                if(matcher.matches()) {
                    String key = matcher.group("key");
                    String value = matcher.group("value");
                    if(key.equals("method")) {
                        method = value;
                        LogHelper.dev("Found method: %s", value);
                    }
                }
            }
        }
        String signature = module.paymentService.calculateUnitPaySignature(method, hashParams);
        LogHelper.dev("Calculated hash sum: %s", signature);
        boolean error = false;
        String errorReason = "Unknown error";
        if(signature.equals(paramSignature) || "check".equals(method)) {
            LogHelper.dev("Signature OK");
            if("pay".equals(method)) {
                LogHelper.dev("Delivery payment");
                if(paymentId < 0 || sum < 0.01) {
                    LogHelper.error("paymentId / sum invalid");
                    error = true;
                    errorReason = "paymentId / sum invalid";
                }
                else {
                    try {
                        module.paymentService.serviceUnitPayResultCompleted(paymentId, sum);
                    } catch (Throwable e) {
                        LogHelper.error(e);
                    }
                }
            }
        }
        else {
            error = true;
            errorReason = "Invalid signature";
        }
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
        UnitPayResult result = new UnitPayResult();
        if(error) {
            result.error = new UnitPayString(errorReason);
        }
        else {
            result.result = new UnitPayString("OK");
        }
        response.content().writeBytes(Launcher.gsonManager.gson.toJson(result).getBytes());
        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
