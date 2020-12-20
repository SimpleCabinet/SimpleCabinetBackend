package pro.gravit.launcher.event.request;

import pro.gravit.launcher.ClientPermissions;
import pro.gravit.launcher.events.RequestEvent;
import pro.gravit.launchermodules.simplecabinet.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtendedInfoRequestEvent extends RequestEvent {
    public User.Gender gender;
    public String status;
    public String email;
    public long economyMoney;
    public double donateMoney;
    public double extendedMoney;

    public String registrationDate;
    //Addional info
    public boolean isBanned;
    public static class ExtendedGroup {
        public String key;
        public String name;
        public int days;

        public ExtendedGroup(String key, String name) {
            this.key = key;
            this.name = name;
            this.days = -1;
        }

        public static Map<Long, ExtendedGroup> PERMISSIONS_GROUP = new HashMap<>();
        public static Map<Long, ExtendedGroup> FLAGS_GROUP = new HashMap<>();
        static {
            PERMISSIONS_GROUP.put(ClientPermissions.PermissionConsts.ADMIN.mask, new ExtendedGroup("ADMIN", "Администратор"));
            PERMISSIONS_GROUP.put(ClientPermissions.PermissionConsts.MANAGEMENT.mask, new ExtendedGroup("MANAGEMENT", "Управление"));
            FLAGS_GROUP.put(ClientPermissions.FlagConsts.BANNED.mask, new ExtendedGroup("BANNED", "Заблокирован"));
            FLAGS_GROUP.put(ClientPermissions.FlagConsts.HIDDEN.mask, new ExtendedGroup("HIDDEN", "Скрыт"));
            FLAGS_GROUP.put(ClientPermissions.FlagConsts.SYSTEM.mask, new ExtendedGroup("SYSTEM", "Системный"));
            FLAGS_GROUP.put(ClientPermissions.FlagConsts.UNTRUSTED.mask, new ExtendedGroup("UNTRUSTED", "Недоверенный"));
        }
        public static List<ExtendedGroup> getGroupsByClientPermissions(ClientPermissions permissions)
        {
            List<ExtendedGroup> groups = new ArrayList<>(8);
            PERMISSIONS_GROUP.forEach((k,v) -> {
                if(permissions.isPermission(k))
                {
                    groups.add(v);
                }
            });
            FLAGS_GROUP.forEach((k,v) -> {
                if(permissions.isFlag(k))
                {
                    groups.add(v);
                }
            });
            return groups;
        }
    }
    public static class PrivateUserZone {
        public boolean isEnabled2FA;
    }

    public static String formatDate(LocalDate date) {
        return date.toString();
    }
    //Group info
    public List<ExtendedGroup> groups;
    public PrivateUserZone privateUserZone;
    @Override
    public String getType() {
        return "lkExtendedInfo";
    }
}
