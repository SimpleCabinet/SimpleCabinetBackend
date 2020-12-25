package pro.gravit.launchermodules.simplecabinet.model;

import javax.persistence.*;

@Entity(name = "HardwareIdLog")
@Table(name = "hwids_log")
public class HardwareIdLogEntity {
    @Id
    @GeneratedValue
    private long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="hwid_id")
    private HardwareId hardwareId;
    private byte[] newPublicKey;

    public HardwareIdLogEntity() {
    }

    public HardwareIdLogEntity(HardwareId hardwareId, byte[] newPublicKey) {
        this.setHardwareId(hardwareId);
        this.setNewPublicKey(newPublicKey);
    }

    public HardwareId getHardwareId() {
        return hardwareId;
    }

    public void setHardwareId(HardwareId hardwareId) {
        this.hardwareId = hardwareId;
    }

    public byte[] getNewPublicKey() {
        return newPublicKey;
    }

    public void setNewPublicKey(byte[] newPublicKey) {
        this.newPublicKey = newPublicKey;
    }
}
