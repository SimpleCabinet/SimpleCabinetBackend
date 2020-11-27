package pro.gravit.launcher.event.request;

import pro.gravit.launcher.events.RequestEvent;
import pro.gravit.launcher.request.secure.HardwareReportRequest;

public class UserHardwareInfoRequestEvent extends RequestEvent {
    public HardwareReportRequest.HardwareInfo info;
    public byte[] publicKey;

    public UserHardwareInfoRequestEvent(HardwareReportRequest.HardwareInfo info, byte[] publicKey) {
        this.info = info;
        this.publicKey = publicKey;
    }

    @Override
    public String getType() {
        return "lkUserHardwareInfo";
    }
}
