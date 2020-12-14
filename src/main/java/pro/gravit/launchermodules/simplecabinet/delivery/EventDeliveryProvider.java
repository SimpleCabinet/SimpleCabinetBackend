package pro.gravit.launchermodules.simplecabinet.delivery;

import pro.gravit.launcher.event.UserItemDeliveryEvent;
import pro.gravit.launcher.request.WebSocketEvent;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.model.OrderEntity;
import pro.gravit.launchermodules.simplecabinet.model.ProductEnchantEntity;
import pro.gravit.launchermodules.simplecabinet.model.ProductEntity;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.utils.helper.LogHelper;

import java.util.UUID;
import java.util.stream.Collectors;

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
        SimpleCabinetDAOProvider dao = (SimpleCabinetDAOProvider) server.config.dao;
        ProductEntity product = entity.getProduct();
        User user = entity.getUser();
        if(product.getType() != ProductEntity.ProductType.ITEM) {
            LogHelper.warning("EventDeliveryProvider not support type %s (order %d). Canceled", entity.getProduct().getType().toString(), entity.getId());
            module.orderService.failOrder(entity);
            return;
        }
        UserItemDeliveryEvent event = new UserItemDeliveryEvent();
        event.orderId = entity.getId();
        event.userUsername = user.getUsername();
        event.userUuid = user.getUuid();
        event.part = entity.getSysPart();
        event.data = new UserItemDeliveryEvent.OrderSystemInfo();
        event.data.itemId = product.getSysId();
        event.data.itemExtra = product.getSysExtra();
        event.data.enchants = dao.productDAO.fetchEnchantsInProduct(product).stream().map(this::getPublicEnchantInfo).collect(Collectors.toList());
        server.nettyServerSocketHandler.nettyServer.service.sendObjectToUUID(serverUUID, event, WebSocketEvent.class);
    }

    @Override
    public boolean isDeliveryUser(OrderEntity entity, User user) {
        return user.getUuid().equals(serverUUID);
    }

    @Override
    public UserItemDeliveryEvent.OrderSystemInfo fetchSystemItemInfo(OrderEntity entity) {
        ProductEntity product = entity.getProduct();
        SimpleCabinetDAOProvider dao = (SimpleCabinetDAOProvider) server.config.dao;
        UserItemDeliveryEvent.OrderSystemInfo data = new UserItemDeliveryEvent.OrderSystemInfo();
        data.itemId = product.getSysId();
        data.itemExtra = product.getSysExtra();
        data.enchants = dao.productDAO.fetchEnchantsInProduct(product).stream().map(this::getPublicEnchantInfo).collect(Collectors.toList());
        return data;
    }

    public UserItemDeliveryEvent.OrderSystemInfo.OrderSystemEnchantInfo getPublicEnchantInfo(ProductEnchantEntity enchant) {
        UserItemDeliveryEvent.OrderSystemInfo.OrderSystemEnchantInfo info = new UserItemDeliveryEvent.OrderSystemInfo.OrderSystemEnchantInfo();
        info.level = enchant.value;
        info.name = enchant.name;
        return info;
    }
}
