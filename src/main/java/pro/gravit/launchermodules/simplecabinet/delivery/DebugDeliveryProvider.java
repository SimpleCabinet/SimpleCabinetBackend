package pro.gravit.launchermodules.simplecabinet.delivery;

import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.model.OrderEntity;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.utils.helper.LogHelper;

public class DebugDeliveryProvider extends DeliveryProvider {
    private transient LaunchServer server;
    private transient SimpleCabinetModule module;

    @Override
    public void init(LaunchServer server, SimpleCabinetModule module) {
        this.server = server;
        this.module = module;
    }

    @Override
    public void delivery(OrderEntity entity) throws Exception {
        LogHelper.info("[DebugDelivery] Delivery order %d for user %s", entity.getId(), entity.getUser().getUsername());
        module.orderService.completeOrder(entity);
    }
}
