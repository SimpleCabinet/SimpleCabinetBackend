package pro.gravit.launchermodules.simplecabinet.response;

import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.event.request.FetchProductsRequestEvent;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.model.ProductEntity;
import pro.gravit.launchserver.socket.Client;
import pro.gravit.launchserver.socket.response.SimpleResponse;

import java.util.List;
import java.util.stream.Collectors;

public class FetchProductsResponse extends SimpleResponse {
    public static int MAX_QUERY = 10;
    public long lastId;
    @Override
    public String getType() {
        return "lkFetchProducts";
    }

    @Override
    public void execute(ChannelHandlerContext channelHandlerContext, Client client) throws Exception {
        if(lastId <= 0) {
            sendError("Invalid request");
            return;
        }
        SimpleCabinetModule module = server.modulesManager.getModule(SimpleCabinetModule.class);
        SimpleCabinetDAOProvider dao = (SimpleCabinetDAOProvider) server.config.dao;
        List<FetchProductsRequestEvent.PublicProductInfo> list = dao.productDAO.fetchPage((int) lastId, MAX_QUERY).stream().map(FetchProductsResponse::fetchPublicInfo).collect(Collectors.toList());
        sendResult(new FetchProductsRequestEvent(list));
    }

    public static FetchProductsRequestEvent.PublicProductInfo fetchPublicInfo(ProductEntity productEntity)
    {
        FetchProductsRequestEvent.PublicProductInfo productInfo = new FetchProductsRequestEvent.PublicProductInfo();
        productInfo.name = productEntity.getName();
        productInfo.description = productEntity.getDescription();
        productInfo.id = productEntity.getId();
        productInfo.allowStack = productEntity.isAllowStack();
        productInfo.price = productEntity.getPrice();
        productInfo.count = productEntity.getCount();
        productInfo.endDate = productEntity.getEndDate();
        return productInfo;
    }
}
