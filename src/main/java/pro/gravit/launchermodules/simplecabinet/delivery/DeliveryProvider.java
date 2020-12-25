package pro.gravit.launchermodules.simplecabinet.delivery;

import pro.gravit.launcher.event.UserItemDeliveryEvent;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.model.OrderEntity;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.utils.ProviderMap;
import pro.gravit.utils.helper.LogHelper;

public abstract class DeliveryProvider {
    public static ProviderMap<DeliveryProvider> providers = new ProviderMap<>();
    private static boolean registeredProviders = false;
    public abstract void init(LaunchServer server, SimpleCabinetModule module);
    protected abstract void delivery(OrderEntity entity) throws Exception;
    public final boolean safeDelivery(OrderEntity entity)
    {
        try {
            delivery(entity);
            return true;
        } catch (Exception e) {
            LogHelper.warning("Error with delivery order %d", entity.getId());
            LogHelper.error(e);
            return false;
        }
    }
    public UserItemDeliveryEvent.OrderSystemInfo fetchSystemItemInfo(OrderEntity entity) {
        throw new UnsupportedOperationException();
    }
    public boolean isDeliveryUser(OrderEntity entity, User user) {
        return false;
    }
    public static void registerProviders()
    {
        if(!registeredProviders)
        {
            providers.register("event", EventDeliveryProvider.class);
            providers.register("debug", DebugDeliveryProvider.class);
            providers.register("luckperms", LuckPermsDeliveryProvider.class);
            providers.register("group", GroupDeliveryProvider.class);
            providers.register("cabinet", CabinetDeliveryProvider.class);
            registeredProviders = true;
        }
    }
}
