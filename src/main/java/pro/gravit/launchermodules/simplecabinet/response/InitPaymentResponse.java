package pro.gravit.launchermodules.simplecabinet.response;

import io.netty.channel.ChannelHandlerContext;
import pro.gravit.launcher.event.request.InitPaymentRequestEvent;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.event.UserInitPaymentEvent;
import pro.gravit.launchermodules.simplecabinet.model.PaymentId;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.socket.Client;
import pro.gravit.launchserver.socket.response.SimpleResponse;
import pro.gravit.utils.helper.LogHelper;

import java.time.LocalDateTime;

public class InitPaymentResponse extends SimpleResponse {
    public double sum;
    public InitPaymentRequestEvent.PaymentVariant variant;

    @Override
    public String getType() {
        return "lkInitPayment";
    }

    @Override
    public void execute(ChannelHandlerContext ctx, Client client) throws Exception {
        if (sum <= 0.01 || variant == null) {
            sendError("Invalid request");
            return;
        }
        if (!client.isAuth || client.username == null) {
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
        PaymentId paymentId = new PaymentId();
        paymentId.setSum(sum);
        paymentId.setUser(user);
        paymentId.setInitialTime(LocalDateTime.now());
        paymentId.setDescription("Balance replenishment");
        paymentId.setStatus(PaymentId.PaymentStatus.CREATED);
        dao.paymentDAO.save(paymentId);
        int id = paymentId.getId();
        LogHelper.debug("User %s initial payment %d", user.getUsername(), id);
        server.modulesManager.invokeEvent(new UserInitPaymentEvent(user, paymentId));
        sendResult(module.paymentService.makeInitPaymentRequestEvent(paymentId, user, ip, variant));
    }
}
