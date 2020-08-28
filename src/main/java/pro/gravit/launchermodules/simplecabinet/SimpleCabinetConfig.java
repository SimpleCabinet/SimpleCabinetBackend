package pro.gravit.launchermodules.simplecabinet;

import pro.gravit.launchserver.auth.MySQLSourceConfig;

public class SimpleCabinetConfig {
    public static class SkinSizeConfig
    {
        public int maxHeight;
        public int maxWidth;
        public int maxBytes;
        public String url;
        public boolean useUuidInUrl = false;

        public SkinSizeConfig() {
        }

        public SkinSizeConfig(int maxHeight, int maxWidth, int maxBytes, String url) {
            this.maxHeight = maxHeight;
            this.maxWidth = maxWidth;
            this.maxBytes = maxBytes;
            this.url = url;
        }
    }
    public SkinSizeConfig maxSkin = new SkinSizeConfig(1024, 512, 1024 * 1024, "updates/skins/%s.png"); // 1MB
    public SkinSizeConfig maxCloak = new SkinSizeConfig(512, 256, 256 * 1024, "updates/cloaks/%s.png"); // 256Kb
    public MySQLSourceConfig migratorSource;
}
