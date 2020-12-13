package pro.gravit.launchermodules.simplecabinet.response;

import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.event.request.CreateOrderRequestEvent;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.model.OrderEntity;
import pro.gravit.launchermodules.simplecabinet.model.ProductEntity;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.socket.Client;
import pro.gravit.launchserver.socket.response.SimpleResponse;

public class CreateOrderResponse extends SimpleResponse {
    public long productId;
    public int quantity;
    @Override
    public String getType() {
        return "lkCreateOrder";
    }

    @Override
    public void execute(ChannelHandlerContext channelHandlerContext, Client client) throws Exception {
        if(productId <= 0 || quantity <= 0) {
            sendError("Invalid request");
            return;
        }
        if(!client.isAuth || client.username == null)
        {
            sendError("Permissions denied");
            return;
        }
        if(client.daoObject == null)
        {
            sendError("Your account not connected to lk");
            return;
        }
        SimpleCabinetModule module = server.modulesManager.getModule(SimpleCabinetModule.class);
        SimpleCabinetDAOProvider dao = (SimpleCabinetDAOProvider) server.config.dao;
        User user = (User) client.daoObject;
        ProductEntity productEntity = dao.productDAO.findById(productId);
        if(productEntity == null)
        {
            sendError("Product not found");
            return;
        }
        double totalSum = productEntity.getPrice() * quantity;
        if(totalSum < 0)
        {
            sendError("Sum invalid");
            return;
        }
        if(totalSum > user.getDonateMoney())
        {
            sendError("Insufficient funds in your account");
            return;
        }
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setStatus(OrderEntity.OrderStatus.CREATED);
        orderEntity.setProduct(productEntity);
        orderEntity.setUser(user);
        orderEntity.setQuantity(quantity);
        orderEntity.setSum(totalSum);
        orderEntity.setSysPart(quantity*productEntity.getSysQuantity());
        dao.orderDAO.save(orderEntity);
        module.workers.submit(() -> {
            module.orderService.processOrder(orderEntity);
        });
        sendResult(new CreateOrderRequestEvent(OrderEntity.OrderStatus.CREATED, orderEntity.getSum(), orderEntity.getId()));
    }
}
