package pro.gravit.launchermodules.simplecabinet.delivery;

import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.model.OrderEntity;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.utils.ProviderMap;

public abstract class DeliveryProvider {
    public static ProviderMap<DeliveryProvider> providers = new ProviderMap<>();
    private boolean registeredProviders = false;
    public abstract void init(LaunchServer server, SimpleCabinetModule module);
    public abstract void delivery(OrderEntity entity) throws Exception;
    public void registerProviders()
    {
        providers.register("event", EventDeliveryProvider.class);
    }
}
