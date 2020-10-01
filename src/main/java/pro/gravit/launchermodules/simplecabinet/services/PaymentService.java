package pro.gravit.launchermodules.simplecabinet.services;

import pro.gravit.launcher.event.request.InitPaymentRequestEvent;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetConfig;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.dao.SimpleCabinetPaymentDAO;
import pro.gravit.launchermodules.simplecabinet.model.PaymentId;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.utils.helper.LogHelper;
import pro.gravit.utils.helper.SecurityHelper;

import java.time.LocalDateTime;
import java.util.HashMap;

public class PaymentService {
    private transient final SimpleCabinetModule module;
    private transient final LaunchServer server;

    public PaymentService(SimpleCabinetModule module, LaunchServer server) {
        this.module = module;
        this.server = server;
    }

    public InitPaymentRequestEvent makeInitPaymentRequestEvent(PaymentId id, User user, String ip, InitPaymentRequestEvent.PaymentVariant variant) {
        switch (variant) {
            case ROBOKASSA:
                return makeInitPaymentRobokassaRequestEvent(id, user, ip);
            default:
                throw new IllegalStateException("Unexpected value: " + variant);
        }
    }
    //WARNING: Transaction not used. TODO: add transactions
    public boolean serviceRobokassaResultCompleted(int id, double sum) {
        SimpleCabinetDAOProvider dao = (SimpleCabinetDAOProvider) server.config.dao;
        PaymentId paymentId = dao.paymentDAO.findById(id);
        if(paymentId == null) {
            return false;
        }
        if(Double.compare(paymentId.getSum(), sum) != 0) {
            LogHelper.warning("Payment %d sum invalid. DB sum: %f, result sum %f", paymentId.getSum(), sum);
            //paymentId.setSum(sum);
        }
        User user = dao.paymentDAO.fetchUser(paymentId);
        paymentId.setStatus(PaymentId.PaymentStatus.COMPLETED);
        paymentId.setSuccessTime(LocalDateTime.now());
        deliveryPaymentId(dao, paymentId, user);
        dao.paymentDAO.update(paymentId);
        LogHelper.debug("Payment %d success finished", id);
        return true;
    }

    protected void deliveryPaymentId(SimpleCabinetDAOProvider dao, PaymentId id, User user) {
        if(id.getSum() > 0)
        {
            user.setDonateMoney(user.getDonateMoney()+ id.getSum());
            dao.userDAO.update(user);
            module.syncService.updateUser(user, false, true);
        }
    }

    protected InitPaymentRequestEvent makeInitPaymentRobokassaRequestEvent(PaymentId id, User user, String ip) {
        SimpleCabinetConfig.PaymentRobokassaConfig config = module.config.payments.robokassa;
        InitPaymentRequestEvent result = new InitPaymentRequestEvent();
        result.redirectUri = "https://auth.robokassa.ru/Merchant/Index.aspx";
        result.method = "POST";
        result.params = new HashMap<>();
        String outSum = String.valueOf(id.getSum());
        String invId = String.valueOf(id.getId());
        result.params.put("MerchantLogin", config.merchantId);
        result.params.put("OutSum", outSum);
        result.params.put("InvId", invId);
        result.params.put("Email", user.getEmail());
        result.params.put("Description", id.getDescription());
        result.params.put("Encoding", "UTF-8");
        result.params.put("IsTest", config.test ? "1" : "0");
        String signature = calculateP2RobokassaHash(String.format("%s:%s:%s", config.merchantId, outSum, invId), null);
        result.params.put("SignatureValue", signature);
        return result;
    }
    protected String calculateP1RobokassaHashString(String params, String postParams) {
        if(postParams == null) {
            return String.format("%s:%s", params, module.config.payments.robokassa.password1);
        }
        else {
            return String.format("%s:%s:%s", params, module.config.payments.robokassa.password1, postParams);
        }
    }
    protected String calculateP2RobokassaHashString(String params, String postParams) {
        if(postParams == null) {
            return String.format("%s:%s", params, module.config.payments.robokassa.password2);
        }
        else {
            return String.format("%s:%s:%s", params, module.config.payments.robokassa.password2, postParams);
        }
    }
    protected String calculateP1RobokassaHash(String params, String postParams) {
        return SecurityHelper.toHex(SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256, calculateP1RobokassaHashString(params, postParams))).toUpperCase();
    }
    protected String calculateP2RobokassaHash(String params, String postParams) {
        return SecurityHelper.toHex(SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256, calculateP2RobokassaHashString(params, postParams))).toUpperCase();
    }
}
