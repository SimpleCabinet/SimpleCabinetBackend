package pro.gravit.launchermodules.simplecabinet.event;

import pro.gravit.launcher.modules.LauncherModule;
import pro.gravit.launchermodules.simplecabinet.model.OrderEntity;

public class CreatedOrderEvent extends LauncherModule.Event {
    public OrderEntity order;

    public CreatedOrderEvent(OrderEntity order) {
        this.order = order;
    }
}
