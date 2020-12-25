package pro.gravit.launchermodules.simplecabinet.severlet;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchserver.socket.NettyConnectContext;
import pro.gravit.launchserver.socket.handlers.NettyWebAPIHandler;
import pro.gravit.utils.helper.LogHelper;
import pro.gravit.utils.helper.SecurityHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class RobokassaSeverlet implements NettyWebAPIHandler.SimpleSeverletHandler {
    private final SimpleCabinetModule module;

    private final Pattern pattern = Pattern.compile("(?<key>.+)=(?<value>.+)");

    public RobokassaSeverlet(SimpleCabinetModule module) {
        this.module = module;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, FullHttpRequest msg, NettyConnectContext context) throws Exception {
        LogHelper.dev("Robokassa request: %s", msg.uri());
        StringTokenizer tokenizer = new StringTokenizer(msg.uri(), "?&");
        if (!tokenizer.hasMoreTokens()) {

            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
            // Close the connection as soon as the error message is sent.
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        String tok = tokenizer.nextToken();
        String token;
        Map<String, String> params = new HashMap<>();
        String robokassaSignature = null;
        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            Matcher matcher = pattern.matcher(token);
            if (matcher.matches()) {
                String key = matcher.group("key");
                String value = matcher.group("value");
                LogHelper.dev("RobokassaKey: %s = %s", key, value);
                if (!key.equals("SignatureValue")) {
                    params.put(key, value);
                } else {
                    LogHelper.dev("Found signature: %s", value);
                    robokassaSignature = value;
                }
            }
        }
        String outSum = params.get("OutSum");
        String InvId = params.get("InvId");
        String calcHashTo = String.format("%s:%s:%s", outSum, InvId, module.config.payments.robokassa.password2);
        LogHelper.dev("CalcHashTo: %s", calcHashTo);
        String hex = SecurityHelper.toHex(SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256, calcHashTo.getBytes())).toUpperCase();
        LogHelper.dev("Calculated hash sum: %s", hex);
        if (hex.equals(robokassaSignature)) {
            double sum = Double.parseDouble(outSum);
            LogHelper.info("Great! Sum: %f for id: %s", sum, InvId);
            module.paymentService.serviceRobokassaResultCompleted(Integer.parseInt(InvId), sum);
        }
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(String.format("OK%s", InvId).getBytes()));
        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
