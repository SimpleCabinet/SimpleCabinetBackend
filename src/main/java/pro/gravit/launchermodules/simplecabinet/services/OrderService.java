package pro.gravit.launchermodules.simplecabinet.services;

import pro.gravit.launcher.event.OrderStatusChangedEvent;
import pro.gravit.launcher.request.WebSocketEvent;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.delivery.DeliveryProvider;
import pro.gravit.launchermodules.simplecabinet.model.OrderEntity;
import pro.gravit.launchermodules.simplecabinet.model.ProductEntity;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.utils.helper.LogHelper;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public class OrderService {

    private transient final SimpleCabinetModule module;
    private transient final LaunchServer server;
    private transient final Map<Long, ScheduledFuture<?>> processingOrdersMap = new ConcurrentHashMap<>();

    public OrderService(SimpleCabinetModule module, LaunchServer server) {
        this.module = module;
        this.server = server;
    }

    public void processOrder(OrderEntity entity) {
        SimpleCabinetDAOProvider dao = (SimpleCabinetDAOProvider) server.config.dao;
        ProductEntity product = entity.getProduct();
        User user = entity.getUser();
        if (entity.getSum() > user.getDonateMoney()) {
            entity.setStatus(OrderEntity.OrderStatus.FAILED);
            dao.orderDAO.update(entity);
            return;
        }
        if (product.getCount() > 0) {
            product.setCount(product.getCount() - 1);
            dao.productDAO.update(product);
        }
        user.setDonateMoney(user.getDonateMoney() - entity.getSum());
        dao.userDAO.update(user);
        entity.setStatus(OrderEntity.OrderStatus.PROCESS);
        dao.orderDAO.update(entity);
        module.workers.submit(() -> {
            deliveryOrder(entity);
        });
    }

    public void completeOrder(OrderEntity entity) {
        SimpleCabinetDAOProvider dao = (SimpleCabinetDAOProvider) server.config.dao;
        entity.setStatus(OrderEntity.OrderStatus.FINISHED);
        dao.orderDAO.update(entity);
        notifyUser(entity);
    }

    public void failOrder(OrderEntity entity) {
        SimpleCabinetDAOProvider dao = (SimpleCabinetDAOProvider) server.config.dao;
        entity.setStatus(OrderEntity.OrderStatus.FAILED);
        dao.orderDAO.update(entity);
        notifyUser(entity);
    }

    private void deliveryOrder(OrderEntity entity) {
        String providerName = entity.getProduct().getSysDeliveryProvider();
        DeliveryProvider provider = module.config.deliveryProviders.get(providerName);
        if (provider == null) {
            LogHelper.warning("Error processing order %d. DeliveryProvider %s not found", entity.getId(), providerName == null ? "null" : providerName);
            failOrder(entity);
            return;
        }
        if (!provider.safeDelivery(entity)) {
            failOrder(entity);
        }
    }

    public void notifyUser(OrderEntity entity) {
        UUID userUUID = entity.getUser().getUuid();
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(entity.getId(), entity.getStatus(), entity.getSysPart());
        server.nettyServerSocketHandler.nettyServer.service.sendObjectToUUID(userUUID, event, WebSocketEvent.class);
    }

    public void addScheduledFuture(long orderId, ScheduledFuture<?> future) {
        processingOrdersMap.put(orderId, future);
    }

    public void updatedOrderStatus(long orderId, OrderEntity.OrderStatus status) {
        ScheduledFuture<?> future = processingOrdersMap.remove(orderId);
        if (future != null) {
            future.cancel(true);
        }
    }
}
