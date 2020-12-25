package pro.gravit.launchermodules.simplecabinet.response;

import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.event.request.FetchUsersRequestEvent;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.dao.SimpleCabinetUserDAO;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.socket.Client;
import pro.gravit.launchserver.socket.response.SimpleResponse;

import java.util.List;
import java.util.stream.Collectors;

public class FetchUsersResponse extends SimpleResponse {
    public static int MAX_QUERY = 12;
    public long lastId;
    public String filterByName;

    public static FetchUsersRequestEvent.UserPublicInfo fetchPublicInfo(User user) {
        return new FetchUsersRequestEvent.UserPublicInfo(user.getUsername(), user.getUuid(), user.getGender());
    }

    @Override
    public String getType() {
        return "lkFetchUsers";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) throws Exception {
        if (lastId < 0) {
            sendError("Invalid request");
            return;
        }
        SimpleCabinetModule module = server.modulesManager.getModule(SimpleCabinetModule.class);
        SimpleCabinetDAOProvider dao = (SimpleCabinetDAOProvider) server.config.dao;
        List<FetchUsersRequestEvent.UserPublicInfo> list = ((SimpleCabinetUserDAO) dao.userDAO).fetchPage((int) lastId * MAX_QUERY, MAX_QUERY, filterByName).stream().map(FetchUsersResponse::fetchPublicInfo).collect(Collectors.toList());
        sendResult(new FetchUsersRequestEvent(list, MAX_QUERY));
    }

}
