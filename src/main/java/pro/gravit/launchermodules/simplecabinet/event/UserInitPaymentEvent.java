package pro.gravit.launchermodules.simplecabinet.event;

import pro.gravit.launcher.modules.LauncherModule;
import pro.gravit.launchermodules.simplecabinet.model.PaymentId;
import pro.gravit.launchermodules.simplecabinet.model.User;

public class UserInitPaymentEvent extends LauncherModule.Event {
    public User user;
    public PaymentId paymentId;

    public UserInitPaymentEvent(User user, PaymentId paymentId) {
        this.user = user;
        this.paymentId = paymentId;
    }
}
