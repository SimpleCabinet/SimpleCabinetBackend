package pro.gravit.launchermodules.simplecabinet.response;

import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.event.request.UploadSkinRequestEvent;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetConfig;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.socket.Client;
import pro.gravit.launchserver.socket.response.SimpleResponse;
import pro.gravit.utils.helper.IOHelper;
import pro.gravit.utils.helper.LogHelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UploadSkinResponse extends SimpleResponse {
    public enum SkinType
    {
        SKIN,
        CLOAK
    }
    public SkinType skinType;
    public byte[] data;
    @Override
    public String getType() {
        return "lkUploadSkin";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) throws Exception {
        if(!client.isAuth || client.username == null || skinType == null || data == null)
        {
            sendError("Permissions denied or invalid request");
            return;
        }
        if(client.daoObject == null)
        {
            sendError("Your account not connected to lk");
            return;
        }
        SimpleCabinetConfig.SkinSizeConfig sizeConfig = null;
        SimpleCabinetModule module = server.modulesManager.getModule(SimpleCabinetModule.class);
        sizeConfig = module.configurable.getConfig().findSkinSizeConfig((User) client.daoObject, skinType);
        if(sizeConfig == null)
        {
            sendError("Permissions denied");
            return;
        }
        if(data.length > sizeConfig.maxBytes)
        {
            sendError("The file too large");
            return;
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
            if(image.getHeight() > sizeConfig.maxHeight || image.getWidth() > sizeConfig.maxWidth)
            {
                sendError("Image height or width too high");
                return;
            }
            Path targetPath = Paths.get(String.format(sizeConfig.url, sizeConfig.useUuidInUrl ? client.daoObject.getUuid() : client.username));
            LogHelper.debug("User %s upload skin. Write %d bytes to %s", client.username, data.length, targetPath.toAbsolutePath().toString());
            IOHelper.createParentDirs(targetPath);
            IOHelper.write(targetPath, data);
            sendResult(new UploadSkinRequestEvent());
        } catch (IOException ex)
        {
            sendError("This file not valid image");
        }

    }
}
