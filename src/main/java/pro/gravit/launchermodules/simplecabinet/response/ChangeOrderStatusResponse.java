package pro.gravit.launchermodules.simplecabinet.response;

import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.ClientPermissions;
import pro.gravit.launcher.event.request.ChangeOrderStatusRequestEvent;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.event.OrderStatusChangedEvent;
import pro.gravit.launchermodules.simplecabinet.model.OrderEntity;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.socket.Client;
import pro.gravit.launchserver.socket.response.SimpleResponse;

public class ChangeOrderStatusResponse extends SimpleResponse {
    public long orderId;
    public OrderEntity.OrderStatus status;
    public boolean isParted;
    public int part;

    @Override
    public String getType() {
        return "lkChangeOrderStatus";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) throws Exception {
        if (orderId <= 0 || status == null) {
            sendError("Invalid request");
            return;
        }
        if (!client.isAuth || client.username == null || client.permissions == null || !(client.permissions.isPermission(ClientPermissions.PermissionConsts.ADMIN) || client.permissions.isPermission(ClientPermissions.PermissionConsts.MANAGEMENT))) {
            sendError("Permissions denied");
            return;
        }
        if (client.daoObject == null) {
            sendError("Your account not connected to lk");
            return;
        }
        SimpleCabinetModule module = server.modulesManager.getModule(SimpleCabinetModule.class);
        SimpleCabinetDAOProvider dao = (SimpleCabinetDAOProvider) server.config.dao;
        User user = (User) client.daoObject;
        OrderEntity entity = dao.orderDAO.findById(orderId);
        if (entity == null) {
            sendError("Order not found");
            return;
        }
        if (isParted) {
            entity.setSysPart(part);
        }
        entity.setStatus(status);
        switch (status) {
            case CREATED:
                break;
            case PROCESS:
                break;
            case DELIVERY:
            case FINISHED:
            case FAILED: {
                dao.orderDAO.fetchUserInOrder(entity);
                module.orderService.notifyUser(entity);
                break;
            }
        }
        dao.orderDAO.update(entity);
        module.orderService.updatedOrderStatus(entity.getId(), status);
        server.modulesManager.invokeEvent(new OrderStatusChangedEvent(status, entity, isParted, part));
        sendResult(new ChangeOrderStatusRequestEvent());
    }
}
