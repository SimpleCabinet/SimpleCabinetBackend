package pro.gravit.launchermodules.simplecabinet.response;

import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.event.request.UploadSkinRequestEvent;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetConfig;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.dao.SimpleCabinetUserDAO;
import pro.gravit.launchermodules.simplecabinet.event.UploadedSkinEvent;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchermodules.simplecabinet.providers.CabinetTextureProvider;
import pro.gravit.launchserver.socket.Client;
import pro.gravit.launchserver.socket.response.SimpleResponse;
import pro.gravit.utils.helper.IOHelper;
import pro.gravit.utils.helper.LogHelper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class UploadSkinResponse extends SimpleResponse {
    public SkinType skinType;
    public byte[] data;
    public boolean remove;

    @Override
    public String getType() {
        return "lkUploadSkin";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) throws Exception {
        if (!client.isAuth || client.username == null || skinType == null || (data == null && !remove)) {
            sendError("Permissions denied or invalid request");
            return;
        }
        if (client.daoObject == null) {
            sendError("Your account not connected to lk");
            return;
        }
        SimpleCabinetConfig.SkinSizeConfig sizeConfig = null;
        SimpleCabinetModule module = server.modulesManager.getModule(SimpleCabinetModule.class);
        ((SimpleCabinetUserDAO) server.config.dao.userDAO).fetchGroups((User) client.daoObject);
        sizeConfig = module.configurable.getConfig().findSkinSizeConfig((User) client.daoObject, skinType);
        if (sizeConfig == null) {
            sendError("Permissions denied");
            return;
        }
        Path targetPath = Paths.get(CabinetTextureProvider.getTextureURL(sizeConfig.url, client.uuid, client.username, ""));
        if(remove) {
            if(Files.exists(targetPath)) {
                Files.delete(targetPath);
                sendResult(new UploadSkinRequestEvent());
            }
        }
        if (data.length > sizeConfig.maxBytes) {
            sendError("The file too large");
            return;
        }
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
            if (image.getHeight() > sizeConfig.maxHeight || image.getWidth() > sizeConfig.maxWidth) {
                sendError("Image height or width too high");
                return;
            }
            LogHelper.debug("User %s upload skin. Write %d bytes to %s", client.username, data.length, targetPath.toAbsolutePath().toString());
            IOHelper.createParentDirs(targetPath);
            IOHelper.write(targetPath, data);
            server.modulesManager.invokeEvent(new UploadedSkinEvent((User) client.daoObject, skinType, targetPath));
            sendResult(new UploadSkinRequestEvent());
        } catch (IOException ex) {
            sendError("This file not valid image");
        }

    }

    public enum SkinType {
        SKIN,
        CLOAK
    }
}
