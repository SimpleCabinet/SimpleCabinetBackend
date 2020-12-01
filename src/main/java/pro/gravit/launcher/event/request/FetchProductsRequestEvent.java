package pro.gravit.launcher.event.request;

import pro.gravit.launcher.events.RequestEvent;

import javax.persistence.Column;
import java.time.LocalDateTime;
import java.util.List;

public class FetchProductsRequestEvent extends RequestEvent {
    public static class PublicProductInfo
    {
        public long id;
        public String name;
        public String description;
        public double price;
        //Limitations
        public long count;
        public LocalDateTime endDate;
        public boolean allowStack;
    }
    public List<PublicProductInfo> products;
    public int maxQuery;

    public FetchProductsRequestEvent(List<PublicProductInfo> products) {
        this.products = products;
    }

    public FetchProductsRequestEvent(List<PublicProductInfo> products, int maxQuery) {
        this.products = products;
        this.maxQuery = maxQuery;
    }

    @Override
    public String getType() {
        return "lkFetchProducts";
    }
}
