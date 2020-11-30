package pro.gravit.launchermodules.simplecabinet;

import pro.gravit.launchermodules.simplecabinet.delivery.DeliveryProvider;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchermodules.simplecabinet.model.UserGroup;
import pro.gravit.launchermodules.simplecabinet.response.UploadSkinResponse;
import pro.gravit.launchserver.auth.MySQLSourceConfig;
import pro.gravit.utils.helper.JVMHelper;
import pro.gravit.utils.helper.LogHelper;

import java.util.*;

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
    public static class UploadSkinEntity
    {
        public SkinSizeConfig config;
        public String groupName;
        public UploadSkinResponse.SkinType skinType;
        public transient GroupEntity group;

        public UploadSkinEntity() {
        }

        public UploadSkinEntity(SkinSizeConfig config, String groupName, UploadSkinResponse.SkinType skinType) {
            this.config = config;
            this.groupName = groupName;
            this.skinType = skinType;
        }
    }
    public static class GroupEntity
    {
        public String name;
        public long permission;
        public int priority;

        public GroupEntity(String name, long permission, int priority) {
            this.name = name;
            this.permission = permission;
            this.priority = priority;
        }
    }
    public static class PaymentUnitPayConfig {
        public String secretKey;
        public int projectId;
        public String resultUrl;
        public boolean testMode;
        public String login;
    }
    public static class PaymentRobokassaConfig {
        public String merchantId;
        public String password1;
        public String password2;
        public boolean test;
    }
    public static class PaymentSelfConfig {

    }
    public static class PaymentsConfig {
        public PaymentUnitPayConfig unitPay;
        public PaymentRobokassaConfig robokassa;
        public PaymentSelfConfig self;
    }
    public static class MailSenderConfig {
        public String host;
        public int port;
        public boolean auth;
        public String username;
        public String password;
        public String from;
    }
    public static class UrlConfig {
        public String frontendUrl;
    }
    public int schedulerCorePoolSize;
    public int workersCorePoolSize;
    public List<UploadSkinEntity> uploads = new ArrayList<>();
    public List<GroupEntity> groups = new ArrayList<>();
    public Map<String, DeliveryProvider> deliveryProviders = new HashMap<>();

    public PaymentsConfig payments;
    public MailSenderConfig mail;
    public UrlConfig urls;

    //public SkinSizeConfig maxSkin = new SkinSizeConfig(1024, 512, 1024 * 1024, "updates/skins/%s.png"); // 1MB
    //public SkinSizeConfig maxCloak = new SkinSizeConfig(512, 256, 256 * 1024, "updates/cloaks/%s.png"); // 256Kb
    public void init()
    {
        groups.sort(Comparator.comparingInt(e -> e.priority));
        for(UploadSkinEntity e : uploads)
        {
            if(e.groupName == null) continue;
            e.group = findGroupByName(e.groupName);
            if(e.group == null)
            {
                throw new IllegalArgumentException(String.format("group %s not found", e.groupName));
            }
        }
    }
    public static SimpleCabinetConfig getDefault()
    {
        SimpleCabinetConfig config = new SimpleCabinetConfig();
        config.workersCorePoolSize = 3;
        config.schedulerCorePoolSize = 2;
        config.uploads.add(new UploadSkinEntity(new SkinSizeConfig(64, 64, 100 * 1024, "updates/skins/%s.png"), null, UploadSkinResponse.SkinType.SKIN));
        config.uploads.add(new UploadSkinEntity(new SkinSizeConfig(32, 32, 40 * 1024, "updates/cloaks/%s.png"), null, UploadSkinResponse.SkinType.CLOAK));
        config.groups.add(new GroupEntity("HD", 0, 1));
        config.uploads.add(new UploadSkinEntity(new SkinSizeConfig(1024, 1024, 1024 * 1024, "updates/skins/%s.png"), "HD", UploadSkinResponse.SkinType.SKIN));
        config.uploads.add(new UploadSkinEntity(new SkinSizeConfig(512, 512, 512 * 1024, "updates/cloaks/%s.png"), "HD", UploadSkinResponse.SkinType.CLOAK));

        config.payments = new PaymentsConfig();
        config.payments.unitPay = new PaymentUnitPayConfig();
        config.payments.unitPay.secretKey = "yourSecretKey";
        config.payments.robokassa = new PaymentRobokassaConfig();
        config.payments.robokassa.merchantId = "yourMerchantId";
        config.payments.robokassa.password1 = "yourPassword1";
        config.payments.robokassa.password2 = "yourPassword2";
        config.payments.robokassa.test = true;

        config.mail = new MailSenderConfig();
        config.mail.host = "smtp.yandex.com";
        config.mail.port = 465;
        config.mail.auth = true;
        config.mail.from = "noreply@example.com";
        config.mail.username = "noreply@example.com";
        config.mail.password = "yourpassword";

        config.urls = new UrlConfig();
        config.urls.frontendUrl = "https://cabinet.yoursite.ru";
        return config;
    }
    public GroupEntity findGroupByName(String name)
    {
        for(GroupEntity e : groups)
        {
            if(e.name.equals(name))
            {
                return e;
            }
        }
        return null;
    }
    public SkinSizeConfig findSkinSizeConfig(User user, UploadSkinResponse.SkinType type)
    {
        UploadSkinEntity result = null;
        for(UploadSkinEntity e : uploads)
        {
            if(e.skinType != type) continue;
            if(e.group == null)
            {
                if(result == null) result = e;
            }
            else
            {
                for(UserGroup group : user.getGroups())
                {
                    GroupEntity en = findGroupByName(group.getGroupName());
                    LogHelper.debug("ADH %s %s", group.getGroupName(), en == null ? null : en.name);
                    if(result == null || result.group == null || en.priority > result.group.priority)
                    {
                        result = e;
                        break;
                    }
                }
            }
        }
        return result == null ? null : result.config;
    }

    public MySQLSourceConfig migratorSource = new MySQLSourceConfig("migrator", "localhost", 3306, "root", "", "launchserver");
}
