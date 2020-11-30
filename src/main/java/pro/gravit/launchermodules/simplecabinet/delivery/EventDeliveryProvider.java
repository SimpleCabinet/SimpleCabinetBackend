package pro.gravit.launchermodules.simplecabinet.delivery;

import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.model.OrderEntity;
import pro.gravit.launchserver.LaunchServer;

import java.util.UUID;

public class EventDeliveryProvider extends DeliveryProvider {
    private transient LaunchServer server;
    private transient SimpleCabinetModule module;
    public UUID serverUUID;

    @Override
    public void init(LaunchServer server, SimpleCabinetModule module) {
        this.server = server;
        this.module = module;
    }

    @Override
    public void delivery(OrderEntity entity) throws Exception {

    }
}
