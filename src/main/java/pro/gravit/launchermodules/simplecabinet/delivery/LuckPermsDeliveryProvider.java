package pro.gravit.launchermodules.simplecabinet.delivery;

import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.dao.SimpleCabinetUserDAO;
import pro.gravit.launchermodules.simplecabinet.model.OrderEntity;
import pro.gravit.launchermodules.simplecabinet.model.ProductEntity;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchermodules.simplecabinet.model.UserGroup;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.launchserver.auth.MySQLSourceConfig;
import pro.gravit.launchserver.auth.PostgreSQLSourceConfig;
import pro.gravit.utils.helper.LogHelper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

public class LuckPermsDeliveryProvider extends DeliveryProvider implements AutoCloseable {
    private transient LaunchServer server;
    private transient SimpleCabinetModule module;
    public MySQLSourceConfig mySQLSource;
    public PostgreSQLSourceConfig postgreSQLSource;
    public String sql;
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
        if(product.getType() != ProductEntity.ProductType.GROUP) {
            LogHelper.warning("LuckyPermsDeliveryProvider not support type %s (order %d). Canceled", entity.getProduct().getType().toString(), entity.getId());
            module.orderService.failOrder(entity);
            return;
        }
        String groupName = product.getSysId();
        int days = product.getSysQuantity()*entity.getQuantity();
        LogHelper.debug("Delivery lk group %s to user %s (%d days)", groupName, user.getUsername(), days);
        LocalDateTime endDate = GroupDeliveryProvider.deliveryGroup(module, (SimpleCabinetUserDAO)dao.userDAO, user, groupName, Duration.ofDays(days), product.isAllowStack());
        if(endDate == null) endDate = LocalDateTime.now().plusDays(days);

        LogHelper.debug("Delivery luckyperms group %s to user %s (%d days)", groupName, user.getUsername(), days);
        deliveryWithSource(groupName, product.getSysExtra(), product.getSysNbt(), user.getUuid(), endDate);
        module.orderService.completeOrder(entity);
    }

    private void deliveryWithSource(String groupName, String extra, String nbt, UUID userUUID, LocalDateTime endDate) throws IOException, SQLException {
        if(mySQLSource != null)
            deliveryWithMySQLSource(groupName, extra, nbt, userUUID, endDate);
        else if(postgreSQLSource != null)
            deliveryWithPostgreSQLSource(groupName, extra, nbt, userUUID, endDate);
        else {
            throw new IllegalArgumentException("mySQLSource or postgreSQLSource not configured");
        }
    }

    private void deliveryWithMySQLSource(String groupName, String extra, String nbt, UUID userUUID, LocalDateTime endDate) throws IOException, SQLException {
        LogHelper.debug("Delivery lk group %s to user %s (%s end date)", groupName, userUUID.toString(), endDate.toString());
        long timestamp = endDate.toEpochSecond(ZoneOffset.UTC);
        try(Connection connection = mySQLSource.getConnection())
        {
            PreparedStatement query = connection.prepareStatement(sql);
            query.setString(1, userUUID.toString());
            query.setString(2, "group.".concat(groupName.toLowerCase()));
            query.setBoolean(3, true);
            query.setString(4, extra == null ? "global" : extra);
            query.setString(5, nbt == null ? "global" : nbt);
            query.setLong(6, timestamp);
            query.setString(7, "{}");
            query.execute();
        }
    }

    private void deliveryWithPostgreSQLSource(String groupName, String extra, String nbt, UUID userUUID, LocalDateTime endDate) throws IOException, SQLException {
        LogHelper.debug("Delivery lk group %s to user %s (%s end date)", groupName, userUUID.toString(), endDate.toString());
        long timestamp = endDate.toEpochSecond(ZoneOffset.UTC);
        try(Connection connection = postgreSQLSource.getConnection())
        {
            PreparedStatement query = connection.prepareStatement(sql);
            query.setString(1, userUUID.toString());
            query.setString(2, "group.".concat(groupName.toLowerCase()));
            query.setBoolean(3, true);
            query.setString(4, extra == null ? "global" : extra);
            query.setString(5, nbt == null ? "global" : nbt);
            query.setLong(6, timestamp);
            query.setString(7, "{}");
            query.execute();
        }
    }

    @Override
    public void close() throws Exception {
        if(mySQLSource != null)
            mySQLSource.close();
        if(postgreSQLSource != null)
            postgreSQLSource.close();
    }
}
