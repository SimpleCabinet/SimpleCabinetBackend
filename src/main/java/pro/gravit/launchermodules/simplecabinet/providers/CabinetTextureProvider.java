package pro.gravit.launchermodules.simplecabinet.providers;

import pro.gravit.launcher.Launcher;
import pro.gravit.launcher.profiles.Texture;
import pro.gravit.launchserver.auth.texture.TextureProvider;
import pro.gravit.utils.helper.CommonHelper;
import pro.gravit.utils.helper.IOHelper;
import pro.gravit.utils.helper.LogHelper;
import pro.gravit.utils.helper.SecurityHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class CabinetTextureProvider extends TextureProvider {
    // Instance
    private String skinURL;
    private String cloakURL;
    private String localSkinPath;
    private String localCloakPath;

    public CabinetTextureProvider() {
    }

    public CabinetTextureProvider(String skinURL, String cloakURL) {
        this.skinURL = skinURL;
        this.cloakURL = cloakURL;
    }

    public CabinetTextureProvider(String skinURL, String cloakURL, String localSkinPath, String localCloakPath) {
        this.skinURL = skinURL;
        this.cloakURL = cloakURL;
        this.localSkinPath = localSkinPath;
        this.localCloakPath = localCloakPath;
    }

    private static Texture getTexture(String url, String localPath, boolean cloak) throws IOException {
        if (LogHelper.isDebugEnabled()) {
            LogHelper.debug("Getting texture: '%s'", url);
        }
        try {
            if (localPath != null) {
                Path path = Path.of(localPath);
                if (Files.exists(path)) {
                    byte[] digest = SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256,
                            IOHelper.read(path));
                    return new Texture(url, digest);
                } else {
                    LogHelper.subDebug("Texture not found :(");
                    return null;
                }
            }
            return new Texture(url, cloak);
        } catch (FileNotFoundException ignored) {
            LogHelper.subDebug("Texture not found :(");
            return null; // Simply not found
        }
    }

    public static String getTextureURL(String url, UUID uuid, String username, String client) {
        return CommonHelper.replace(url, "username", IOHelper.urlEncode(username),
                "uuid", IOHelper.urlEncode(uuid.toString()), "hash", IOHelper.urlEncode(Launcher.toHash(uuid)),
                "client", IOHelper.urlEncode(client == null ? "unknown" : client));
    }

    @Override
    public void close() {
        // Do nothing
    }

    @Override
    public Texture getCloakTexture(UUID uuid, String username, String client) throws IOException {
        return getTexture(getTextureURL(cloakURL, uuid, username, client), getTextureURL(localCloakPath, uuid, username, client), true);
    }

    @Override
    public Texture getSkinTexture(UUID uuid, String username, String client) throws IOException {
        return getTexture(getTextureURL(skinURL, uuid, username, client), getTextureURL(localSkinPath, uuid, username, client), false);
    }
}
