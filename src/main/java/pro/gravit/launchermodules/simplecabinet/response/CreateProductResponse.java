package pro.gravit.launchermodules.simplecabinet.response;

import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.ClientPermissions;
import pro.gravit.launcher.event.request.CreateProductRequestEvent;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.model.AuditEntity;
import pro.gravit.launchermodules.simplecabinet.model.ProductEntity;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.socket.Client;
import pro.gravit.launchserver.socket.response.SimpleResponse;

import java.time.LocalDateTime;

public class CreateProductResponse extends SimpleResponse {
    public ProductEntity.ProductType productType;
    public String name;
    public String description;
    public double price;
    //Limitations
    public long count;
    public LocalDateTime endDate;
    public boolean allowStack;
    public boolean visible;
    //Sys
    public String sysId;
    public int sysQuantity;
    public String sysExtra;
    public String sysNbt;
    public String sysDeliveryProvider;

    @Override
    public String getType() {
        return "lkCreateProduct";
    }

    @Override
    public void execute(ChannelHandlerContext channelHandlerContext, Client client) throws Exception {
        if (productType == null || name == null || price < 0 || sysDeliveryProvider == null || count == 0) {
            sendError("Invalid request");
            return;
        }
        if (!client.isAuth || client.username == null || client.permissions == null || !client.permissions.isPermission(ClientPermissions.PermissionConsts.ADMIN)) {
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
        ProductEntity productEntity = new ProductEntity();
        productEntity.setType(productType);
        productEntity.setName(name);
        productEntity.setDescription(description);
        productEntity.setVisible(visible);
        productEntity.setPrice(price);
        productEntity.setCount(count);
        productEntity.setEndDate(endDate);
        productEntity.setAllowStack(allowStack);
        productEntity.setSysId(sysId);
        productEntity.setSysNbt(sysNbt);
        productEntity.setSysDeliveryProvider(sysDeliveryProvider);
        productEntity.setSysExtra(sysExtra);
        productEntity.setSysQuantity(sysQuantity);
        dao.productDAO.save(productEntity);
        server.modulesManager.getModule(SimpleCabinetModule.class).auditService.pushAdvancedAudit(AuditEntity.AuditType.CREATE_PRODUCT, user, ip, null, String.valueOf(productEntity.getId()), null);
        sendResult(new CreateProductRequestEvent(productEntity.getId()));
    }
}
