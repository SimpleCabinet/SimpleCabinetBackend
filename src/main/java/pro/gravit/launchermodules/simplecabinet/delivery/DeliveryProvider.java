package pro.gravit.launchermodules.simplecabinet.delivery;

import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.model.OrderEntity;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.utils.ProviderMap;
import pro.gravit.utils.helper.LogHelper;

public abstract class DeliveryProvider {
    public static ProviderMap<DeliveryProvider> providers = new ProviderMap<>();
    private boolean registeredProviders = false;
    public abstract void init(LaunchServer server, SimpleCabinetModule module);
    protected abstract void delivery(OrderEntity entity) throws Exception;
    public final boolean safeDelivery(OrderEntity entity)
    {
        try {
            delivery(entity);
            return true;
        } catch (Exception e) {
            LogHelper.warning("Error with delivery order %d", entity.getId());
            if(!(e instanceof RuntimeException))
            LogHelper.error(e);
            return false;
        }
    }
    public void registerProviders()
    {
        providers.register("event", EventDeliveryProvider.class);
        providers.register("debug", DebugDeliveryProvider.class);
        providers.register("luckperms", LuckPermsDeliveryProvider.class);
    }
}
