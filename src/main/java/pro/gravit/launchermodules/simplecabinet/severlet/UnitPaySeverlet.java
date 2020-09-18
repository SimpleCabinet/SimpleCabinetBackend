package pro.gravit.launchermodules.simplecabinet.severlet;

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

    private Pattern pattern = Pattern.compile("params\\[(?<key>.+)\\]=(?<value>.+)");

    public UnitPaySeverlet(SimpleCabinetModule module) {
        this.module = module;
    }

    static class KeyValuePair {
        public String key;
        public String value;

        public KeyValuePair(String key, String value) {
            this.key = key;
            this.value = value;
        }
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
        List<KeyValuePair> hashParams = new ArrayList<>();
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
                    hashParams.add(new KeyValuePair(key, value));
                } else
                    LogHelper.dev("Skip signature");
            }
        }
        hashParams.sort(Comparator.comparing((e) -> e.key));
        for(KeyValuePair s : hashParams)
        {
            LogHelper.dev("Hash to %s", s);
            digest.update(s.value.getBytes());
            digest.update("{up}".getBytes());
        }
        digest.update(module.config.payments.unitPay.secretKey.getBytes());
        String hex = SecurityHelper.toHex(digest.digest());
        LogHelper.dev("Calculated hash sum: %s", hex);
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
