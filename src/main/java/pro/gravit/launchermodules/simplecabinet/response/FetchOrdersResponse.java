package pro.gravit.launchermodules.simplecabinet.response;

import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.ClientPermissions;
import pro.gravit.launcher.event.request.FetchOrdersRequestEvent;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.delivery.DeliveryProvider;
import pro.gravit.launchermodules.simplecabinet.model.OrderEntity;
import pro.gravit.launchermodules.simplecabinet.model.ProductEntity;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.socket.Client;
import pro.gravit.launchserver.socket.response.SimpleResponse;

import java.util.List;
import java.util.UUID;

public class FetchOrdersResponse extends AbstractUserResponse {
    public static int MAX_QUERY = 10;
    public long lastId;
    public OrderEntity.OrderStatus filterByType;
    public long orderId;
    @Override
    public String getType() {
        return "lkFetchOrders";
    }

    @Override
    public void executeByUser(ChannelHandlerContext channelHandlerContext, User user, boolean self, Client client) throws Exception {
        SimpleCabinetModule module = server.modulesManager.getModule(SimpleCabinetModule.class);
        SimpleCabinetDAOProvider dao = (SimpleCabinetDAOProvider) server.config.dao;
        if(orderId > 0) {
            if(!checkPermissionForNonSelf(client)) {
                sendError("Permissions denied");
                return;
            }
            OrderEntity entity = dao.orderDAO.findById(orderId);
            if(entity == null) {
                sendError("Order not found");
                return;
            }
            sendResult(new FetchOrdersRequestEvent(List.of(getPublicInfo(entity, true))));
        }
        else {
            sendError("Not implemented");
        }
    }

    public FetchOrdersRequestEvent.PublicOrderInfo getPublicInfo(OrderEntity entity, boolean isAdmin) {
        FetchOrdersRequestEvent.PublicOrderInfo orderInfo = new FetchOrdersRequestEvent.PublicOrderInfo();
        SimpleCabinetDAOProvider dao = (SimpleCabinetDAOProvider) server.config.dao;
        SimpleCabinetModule module = server.modulesManager.getModule(SimpleCabinetModule.class);
        orderInfo.orderId = entity.getId();
        orderInfo.date = null; // TODO
        orderInfo.part = entity.getSysPart();
        orderInfo.status = entity.getStatus();
        if(isAdmin) {
            try {
                ProductEntity product = dao.orderDAO.fetchProductInOrder(entity);
                User user = dao.orderDAO.fetchUserInOrder(entity);
                if(product.getType() != ProductEntity.ProductType.ITEM) return orderInfo;
                DeliveryProvider provider = module.config.deliveryProviders.get(product.getSysDeliveryProvider());
                if(provider == null) return orderInfo;
                orderInfo.systemInfo = provider.fetchSystemItemInfo(entity);
                orderInfo.userUsername = user.getUsername();
                orderInfo.userUUID = user.getUuid();
            } catch (UnsupportedOperationException ignored) {

            }
        }
        return orderInfo;
    }

    @Override
    public boolean checkPermissionForNonSelf(Client client) {
        return client.permissions != null && ( client.permissions.isPermission(ClientPermissions.PermissionConsts.ADMIN) || client.permissions.isPermission(ClientPermissions.PermissionConsts.MANAGEMENT) );
    }
}
