package pro.gravit.launchermodules.simplecabinet.event;

import pro.gravit.launcher.modules.LauncherModule;
import pro.gravit.launchermodules.simplecabinet.model.OrderEntity;

public class OrderStatusChangedEvent extends LauncherModule.Event {
    public OrderEntity.OrderStatus status;
    public OrderEntity entity;
    public boolean isParted;
    public int part;

    public OrderStatusChangedEvent(OrderEntity.OrderStatus status, OrderEntity entity, boolean isParted, int part) {
        this.status = status;
        this.entity = entity;
        this.isParted = isParted;
        this.part = part;
    }
}
