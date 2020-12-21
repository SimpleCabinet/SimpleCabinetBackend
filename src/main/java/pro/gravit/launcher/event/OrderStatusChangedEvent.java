package pro.gravit.launcher.event;

import pro.gravit.launcher.request.WebSocketEvent;
import pro.gravit.launchermodules.simplecabinet.model.OrderEntity;

public class OrderStatusChangedEvent implements WebSocketEvent {
    public long orderId;
    public OrderEntity.OrderStatus newStatus;
    public int part;

    public OrderStatusChangedEvent(long orderId, OrderEntity.OrderStatus newStatus, int part) {
        this.orderId = orderId;
        this.newStatus = newStatus;
        this.part = part;
    }

    @Override
    public String getType() {
        return "lkOrderStatusChanged";
    }
}
