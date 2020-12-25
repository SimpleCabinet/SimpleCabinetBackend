package pro.gravit.launcher.event;

import pro.gravit.launcher.request.WebSocketEvent;

import java.util.List;
import java.util.UUID;

public class UserItemDeliveryEvent implements WebSocketEvent {
    public long orderId;
    public String userUsername;
    public UUID userUuid;
    public int part;
    public OrderSystemInfo data;

    @Override
    public String getType() {
        return "lkUserOrderDelivery";
    }

    public static class OrderSystemInfo {
        public String itemId;
        public String itemExtra;
        public List<OrderSystemEnchantInfo> enchants;
        public String itemNbt;

        public static class OrderSystemEnchantInfo {
            public String name;
            public int level;
        }
    }
}
