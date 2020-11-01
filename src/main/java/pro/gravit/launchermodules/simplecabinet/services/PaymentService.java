package pro.gravit.launchermodules.simplecabinet.services;

import pro.gravit.launcher.HTTPRequest;
import pro.gravit.launcher.Launcher;
import pro.gravit.launcher.event.request.InitPaymentRequestEvent;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetConfig;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.model.PaymentId;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.utils.helper.LogHelper;
import pro.gravit.utils.helper.SecurityHelper;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class PaymentService {
    private transient final SimpleCabinetModule module;
    private transient final LaunchServer server;

    public PaymentService(SimpleCabinetModule module, LaunchServer server) {
        this.module = module;
        this.server = server;
    }

    public InitPaymentRequestEvent makeInitPaymentRequestEvent(PaymentId id, User user, String ip, InitPaymentRequestEvent.PaymentVariant variant) throws IOException {
        switch (variant) {
            case ROBOKASSA:
                return makeInitPaymentRobokassaRequestEvent(id, user, ip);
            case SELF:
                return makeInitPaymentSelfRequestEvent(id, user, ip);
            case UNITPAY:
                return makeInitPaymentUnitPayRequestEvent(id, user, ip);
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

    public boolean serviceUnitPayResultCompleted(int id, double sum) {
        SimpleCabinetDAOProvider dao = (SimpleCabinetDAOProvider) server.config.dao;
        PaymentId paymentId = dao.paymentDAO.findById(id);
        if(paymentId == null) {
            return false;
        }
        if(Double.compare(paymentId.getSum(), sum) != 0) {
            LogHelper.warning("Payment %d sum invalid. DB sum: %f, result sum %f", paymentId.getId(), paymentId.getSum(), sum);
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

    public static class KeyValuePair {
        public String key;
        public String value;

        public KeyValuePair(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
    public String calculateUnitPaySignature(String method, List<KeyValuePair> hashParams) {
        MessageDigest digest = SecurityHelper.newDigest(SecurityHelper.DigestAlgorithm.SHA256);
        hashParams.sort(Comparator.comparing((e) -> e.key));
        if(method != null)
        {
            digest.update(method.getBytes());
            digest.update("{up}".getBytes());
        }
        hashParams.sort(Comparator.comparing((e) -> e.key));
        for(KeyValuePair s : hashParams)
        {
            String realValue = URLDecoder.decode(s.value, StandardCharsets.UTF_8);
            LogHelper.dev("Hash to %s", realValue);
            digest.update(realValue.getBytes());
            digest.update("{up}".getBytes());
        }
        digest.update(module.config.payments.unitPay.secretKey.getBytes());
        return SecurityHelper.toHex(digest.digest());
    }

    private String formatParam(String key, String value)
    {
        return String.format("&params[%s]=%s", key, value);
    }

    static class UnitPayPaymentInitResult {
        public String type;
        public String redirectUrl;
        public String statusUrl;
        public String receiptUrl;
        public long paymentId;
        public String message;
    }

    static class UnitPayErrorResult {
        public String message;
    }

    static class UnitPayPaymentInitResultContainer {
        public UnitPayPaymentInitResult result;
        public UnitPayErrorResult error;
    }

    protected InitPaymentRequestEvent makeInitPaymentUnitPayRequestEvent(PaymentId id, User user, String ip) throws IOException {
        SimpleCabinetConfig.PaymentRobokassaConfig config = module.config.payments.robokassa;
        StringBuilder builder = new StringBuilder("https://unitpay.ru/api?method=initPayment");
        String paramAccount = String.valueOf(id.getId());
        String paramSum = String.valueOf(id.getSum());
        builder.append(formatParam("paymentType", "card"));
        builder.append(formatParam("account", paramAccount));
        builder.append(formatParam("sum", paramSum));
        builder.append(formatParam("projectId", String.valueOf(module.config.payments.unitPay.projectId)));
        if(module.config.payments.unitPay.resultUrl != null) builder.append(formatParam("resultUrl", module.config.payments.unitPay.resultUrl));
        builder.append(formatParam("desc", id.getDescription()));
        builder.append(formatParam("ip", ip));
        builder.append(formatParam("test", module.config.payments.unitPay.testMode ? "1" : "0"));
        if(module.config.payments.unitPay.login != null) builder.append(formatParam("login", module.config.payments.unitPay.login));
        builder.append(formatParam("secretKey", module.config.payments.unitPay.secretKey));
        List<KeyValuePair> hashParams = new ArrayList<>();
        hashParams.add(new KeyValuePair("account", paramAccount));
        hashParams.add(new KeyValuePair("desc", id.getDescription()));
        hashParams.add(new KeyValuePair("sum", paramSum));
        //String signatureString = String.format("%s{up}%s{up}%s{up}%s", args[1], args[4], args[2], module.config.payments.unitPay.secretKey);
        //String hex = SecurityHelper.toHex(SecurityHelper.digest(SecurityHelper.DigestAlgorithm.SHA256, signatureString));
        //LogHelper.dev("Digest %s", hex);
        String signature = calculateUnitPaySignature(null, hashParams);
        builder.append(formatParam("signature", signature));
        URL url = new URL(builder.toString());
        LogHelper.info(builder.toString());
        UnitPayPaymentInitResultContainer resultUnitPay = Launcher.gsonManager.gson.fromJson(HTTPRequest.jsonRequest(null, "GET", url), UnitPayPaymentInitResultContainer.class);
        LogHelper.dev("Output: %s", Launcher.gsonManager.configGson.toJson(resultUnitPay));
        if(resultUnitPay.error != null) {
            throw new SecurityException(resultUnitPay.error.message);
        }
        else if(resultUnitPay.result != null) {
            String redirectUrl = resultUnitPay.result.redirectUrl;
            InitPaymentRequestEvent result = new InitPaymentRequestEvent();
            result.redirectUri = redirectUrl;
            result.method = "GET";
            result.params = new HashMap<>();
            return result;
        }
        else {
            throw new SecurityException("Unknown UnitPay response");
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

    protected InitPaymentRequestEvent makeInitPaymentSelfRequestEvent(PaymentId id, User user, String ip) {
        SimpleCabinetConfig.PaymentSelfConfig config = module.config.payments.self;
        if(config  == null) throw new SecurityException("this type not allow");
        InitPaymentRequestEvent result = new InitPaymentRequestEvent();
        result.redirectUri = "https://auth.robokassa.ru/Merchant/Index.aspx";
        result.method = "POST";
        result.params = new HashMap<>();
        String outSum = String.valueOf(id.getSum());
        String invId = String.valueOf(id.getId());
        result.params.put("sum", outSum);
        result.params.put("paymentId", invId);
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
