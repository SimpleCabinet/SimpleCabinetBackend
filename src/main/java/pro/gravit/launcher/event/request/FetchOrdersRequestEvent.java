package pro.gravit.launcher.event.request;

import pro.gravit.launcher.event.UserItemDeliveryEvent;
import pro.gravit.launcher.events.RequestEvent;
import pro.gravit.launchermodules.simplecabinet.model.OrderEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class FetchOrdersRequestEvent extends RequestEvent {
    public List<PublicOrderInfo> list;
    public int maxQuery;

    public FetchOrdersRequestEvent(List<PublicOrderInfo> list) {
        this.list = list;
    }

    public FetchOrdersRequestEvent(List<PublicOrderInfo> list, int maxQuery) {
        this.list = list;
        this.maxQuery = maxQuery;
    }

    @Override
    public String getType() {
        return "lkFetchOrders";
    }

    public static class PublicOrderInfo {
        public long orderId;
        public long productId;
        public LocalDateTime date;
        public OrderEntity.OrderStatus status;
        public UserItemDeliveryEvent.OrderSystemInfo systemInfo;
        public int part;
        public String userUsername;
        public UUID userUUID;
        public boolean cantDelivery;
    }
}
