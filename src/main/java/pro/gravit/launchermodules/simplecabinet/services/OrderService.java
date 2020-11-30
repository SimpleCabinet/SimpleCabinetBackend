package pro.gravit.launchermodules.simplecabinet.services;

import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.model.OrderEntity;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.LaunchServer;

import java.util.UUID;

public class OrderService {

    private transient final SimpleCabinetModule module;
    private transient final LaunchServer server;

    public OrderService(SimpleCabinetModule module, LaunchServer server) {
        this.module = module;
        this.server = server;
    }

    public void processOrder(OrderEntity entity)
    {
        SimpleCabinetDAOProvider dao = (SimpleCabinetDAOProvider) server.config.dao;
        User user = entity.getUser();
        if(entity.getSum() > user.getDonateMoney())
        {
            entity.setStatus(OrderEntity.OrderStatus.FAILED);
            dao.orderDAO.update(entity);
            return;
        }
        user.setDonateMoney(user.getDonateMoney() - entity.getSum());
        dao.userDAO.update(user);
        entity.setStatus(OrderEntity.OrderStatus.PROCESS);
        dao.orderDAO.update(entity);
        module.workers.submit(() -> {
            deliveryOrder(entity);
        });
    }

    public void completeOrder(OrderEntity entity)
    {
        SimpleCabinetDAOProvider dao = (SimpleCabinetDAOProvider) server.config.dao;
        entity.setStatus(OrderEntity.OrderStatus.FINISHED);
        dao.orderDAO.update(entity);
    }

    public void failOrder(OrderEntity entity)
    {
        SimpleCabinetDAOProvider dao = (SimpleCabinetDAOProvider) server.config.dao;
        entity.setStatus(OrderEntity.OrderStatus.FAILED);
        dao.orderDAO.update(entity);
    }

    private void deliveryOrder(OrderEntity entity)
    {
        //TODO: Implement
    }
}
