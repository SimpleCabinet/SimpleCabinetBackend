package pro.gravit.launchermodules.simplecabinet.delivery;

import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.model.OrderEntity;
import pro.gravit.launchermodules.simplecabinet.model.ProductEntity;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.utils.helper.LogHelper;

public class CabinetDeliveryProvider extends DeliveryProvider {
    private transient LaunchServer server;
    private transient SimpleCabinetModule module;
    @Override
    public void init(LaunchServer server, SimpleCabinetModule module) {
        this.server = server;
        this.module = module;
    }

    @Override
    protected void delivery(OrderEntity entity) throws Exception {
        SimpleCabinetDAOProvider dao = (SimpleCabinetDAOProvider) server.config.dao;
        ProductEntity product = entity.getProduct();
        User user = entity.getUser();
        if(product.getType() != ProductEntity.ProductType.SPECIAL) {
            LogHelper.warning("CabinetDeliveryProvider not support type %s (order %d). Canceled", entity.getProduct().getType().toString(), entity.getId());
            module.orderService.failOrder(entity);
            return;
        }
        String moneyName = product.getSysId();
        long quantity = (long) product.getSysQuantity() *entity.getQuantity();
        if("economyMoney".equals(moneyName)) {
            user.setEconomyMoney(user.getEconomyMoney()+quantity);
        }
        else if("donateMoney".equals(moneyName)) {
            user.setDonateMoney(user.getDonateMoney()+quantity);
        }
        else if("extendedMoney".equals(moneyName)) {
            user.setExtendedMoney(user.getExtendedMoney()+quantity);
        }
        else {
            module.orderService.failOrder(entity);
            return;
        }
        dao.userDAO.update(user);
        module.orderService.completeOrder(entity);
    }
}
