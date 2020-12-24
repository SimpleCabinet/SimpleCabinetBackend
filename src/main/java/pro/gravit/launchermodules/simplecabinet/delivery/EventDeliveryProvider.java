package pro.gravit.launchermodules.simplecabinet.delivery;

import pro.gravit.launcher.event.UserItemDeliveryEvent;
import pro.gravit.launcher.request.WebSocketEvent;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.dao.SimpleCabinetOrderDAO;
import pro.gravit.launchermodules.simplecabinet.model.OrderEntity;
import pro.gravit.launchermodules.simplecabinet.model.ProductEnchantEntity;
import pro.gravit.launchermodules.simplecabinet.model.ProductEntity;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.utils.helper.LogHelper;

import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class EventDeliveryProvider extends DeliveryProvider {
    private transient LaunchServer server;
    private transient SimpleCabinetModule module;
    public UUID serverUUID;
    public boolean multiserver;
    public boolean noAutoDelivery;
    public List<UUID> list;

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
        event.data = fetchSystemItemInfo(entity);
        if(noAutoDelivery) {
            entity.setStatus(OrderEntity.OrderStatus.DELIVERY);
            dao.orderDAO.update(entity);
            module.orderService.updatedOrderStatus(entity.getId(), OrderEntity.OrderStatus.DELIVERY);
            module.orderService.notifyUser(entity);
        }
        else if(!multiserver) {
            schDeliveryToOneServerLoop(dao.orderDAO, entity, serverUUID, event);
        }
        else {
            schDeliveryToMultiServerLoop(dao.orderDAO, entity, list, event);
        }
    }

    public void deliveryToOneServer(UUID uuid, Object event) {
        server.nettyServerSocketHandler.nettyServer.service.sendObjectToUUID(uuid, event, WebSocketEvent.class);
    }

    public void schDeliveryToOneServerLoop(SimpleCabinetOrderDAO dao, OrderEntity entity, UUID uuid, Object event) {
        AtomicInteger count = new AtomicInteger(0);

        ScheduledFuture<?> future = module.scheduler.scheduleAtFixedRate(() -> {
                deliveryToOneServer(uuid, event);
                int current = count.incrementAndGet();
                if(current > 5) {
                    entity.setStatus(OrderEntity.OrderStatus.DELIVERY);
                    dao.update(entity);
                    module.orderService.updatedOrderStatus(entity.getId(), OrderEntity.OrderStatus.DELIVERY);
                    module.orderService.notifyUser(entity);
                }
            }, 0, 5, TimeUnit.SECONDS);
        module.orderService.addScheduledFuture(entity.getId(), future);
    }

    public void schDeliveryToMultiServerLoop(SimpleCabinetOrderDAO dao, OrderEntity entity, List<UUID> uuids, Object event) {
        Queue<UUID> queue = new ConcurrentLinkedQueue<>(uuids);
        Runnable runnable = () -> {
            UUID uuid = queue.poll();
            if(uuid == null) {
                entity.setStatus(OrderEntity.OrderStatus.DELIVERY);
                dao.update(entity);
                module.orderService.updatedOrderStatus(entity.getId(), OrderEntity.OrderStatus.DELIVERY);
                module.orderService.notifyUser(entity);
            }
            deliveryToOneServer(uuid, event);
        };
        ScheduledFuture<?> future = module.scheduler.scheduleAtFixedRate(runnable, 0, 5, TimeUnit.SECONDS);
        module.orderService.addScheduledFuture(entity.getId(), future);
    }

    @Override
    public boolean isDeliveryUser(OrderEntity entity, User user) {
        if(multiserver) {
            return list.contains(user.getUuid());
        }
        else {
            return user.getUuid().equals(serverUUID);
        }
    }

    @Override
    public UserItemDeliveryEvent.OrderSystemInfo fetchSystemItemInfo(OrderEntity entity) {
        ProductEntity product = entity.getProduct();
        SimpleCabinetDAOProvider dao = (SimpleCabinetDAOProvider) server.config.dao;
        UserItemDeliveryEvent.OrderSystemInfo data = new UserItemDeliveryEvent.OrderSystemInfo();
        data.itemId = product.getSysId();
        data.itemExtra = product.getSysExtra();
        data.itemNbt = product.getSysNbt();
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
