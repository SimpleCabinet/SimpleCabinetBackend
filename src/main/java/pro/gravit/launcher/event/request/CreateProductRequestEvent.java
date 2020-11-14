package pro.gravit.launcher.event.request;

import pro.gravit.launcher.events.RequestEvent;

public class CreateProductRequestEvent extends RequestEvent {
    public long id;

    public CreateProductRequestEvent(long id) {
        this.id = id;
    }

    @Override
    public String getType() {
        return "lkCreateProduct";
    }
}
