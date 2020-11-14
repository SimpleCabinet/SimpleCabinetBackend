package pro.gravit.launcher.event.request;

import pro.gravit.launcher.events.RequestEvent;
import pro.gravit.launchermodules.simplecabinet.model.OrderEntity;

public class CreateOrderRequestEvent extends RequestEvent {
    public OrderEntity.OrderStatus status;
    public double totalSum;
    public long orderId;

    public CreateOrderRequestEvent(OrderEntity.OrderStatus status, double totalSum, long orderId) {
        this.status = status;
        this.totalSum = totalSum;
        this.orderId = orderId;
    }

    @Override
    public String getType() {
        return "lkCreateOrder";
    }
}
