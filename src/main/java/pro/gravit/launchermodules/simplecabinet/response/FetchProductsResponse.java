package pro.gravit.launchermodules.simplecabinet.response;

import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.event.request.FetchProductsRequestEvent;
import pro.gravit.launchermodules.simplecabinet.model.ProductEntity;
import pro.gravit.launchserver.socket.Client;
import pro.gravit.launchserver.socket.response.SimpleResponse;

public class FetchProductsResponse extends SimpleResponse {
    public static int MAX_QUERY = 10;
    public int page;
    @Override
    public String getType() {
        return "lkFetchProducts";
    }

    @Override
    public void execute(ChannelHandlerContext channelHandlerContext, Client client) throws Exception {
        sendError("Not implemented");
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
