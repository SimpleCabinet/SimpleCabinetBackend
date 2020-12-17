package pro.gravit.launchermodules.simplecabinet.delivery;

import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.dao.SimpleCabinetUserDAO;
import pro.gravit.launchermodules.simplecabinet.model.OrderEntity;
import pro.gravit.launchermodules.simplecabinet.model.ProductEntity;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchermodules.simplecabinet.model.UserGroup;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.utils.helper.LogHelper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class GroupDeliveryProvider extends DeliveryProvider {
    private transient LaunchServer server;
    private transient SimpleCabinetModule module;
    @Override
    public void init(LaunchServer server, SimpleCabinetModule module) {
        this.server = server;
        this.module = module;
    }

    @Override
    protected void delivery(OrderEntity entity) throws Exception {

    }

    public static LocalDateTime deliveryGroup(SimpleCabinetModule module, SimpleCabinetUserDAO userDAO, User user, String groupName, Duration duration, boolean stack) {
        List<UserGroup> list = userDAO.fetchGroups(user);
        UserGroup group = new UserGroup();
        LocalDateTime endDate = LocalDateTime.now().plus(duration);
        boolean isKnownGroup = false;
        for(UserGroup g : list) {
            if(g.getGroupName().equals(groupName)) {
                group = g;
                isKnownGroup = true;
                break;
            }
        }
        if(stack && group.getEndDate() != null)
        {
            endDate = endDate.plus(Duration.between(LocalDateTime.now(), group.getEndDate()));
        }
        group.setEndDate(endDate);
        if(!isKnownGroup) {
            if(module.config.findGroupByName(groupName) != null) {
                group.setStartDate(LocalDateTime.now());
                group.setUser(user);
                group.setGroupName(groupName);
                userDAO.save(group);
            }
            else return null;
        }
        else  {
            userDAO.update(group);
        }
        return endDate;
    }
}
