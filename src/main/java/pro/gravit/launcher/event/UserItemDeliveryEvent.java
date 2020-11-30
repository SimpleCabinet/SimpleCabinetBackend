package pro.gravit.launcher.event;

import pro.gravit.launcher.request.WebSocketEvent;

import java.util.List;
import java.util.UUID;

public class UserItemDeliveryEvent implements WebSocketEvent {
    public long orderId;
    public String userUsername;
    public UUID userUuid;
    public static class OrderSystemInfo {
        public String itemId;
        public String itemExtra;
        public List<String> itemLore;
        public static class OrderSystemEnchantInfo {
            public String name;
            public int level;
        }
        public List<OrderSystemEnchantInfo> enchants;
        public byte[] compressedJsonNBTTags;
    }
    public OrderSystemInfo data;
    @Override
    public String getType() {
        return "lkUserOrderDelivery";
    }
}
