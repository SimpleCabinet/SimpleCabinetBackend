package pro.gravit.launchermodules.simplecabinet.model;

import pro.gravit.launcher.ClientPermissions;
import pro.gravit.launchermodules.simplecabinet.utils.PasswordHelper;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity(name = "User")
@Table(name = "users")
@Cacheable
public class User implements pro.gravit.launchserver.dao.User {

    public static HashType DEFAULT_PASSWORD_HASH = HashType.BCRYPT;
    //Base and launcher
    @Id
    @GeneratedValue
    private long id;
    @Column(unique = true)
    private String username;
    @Column(name = "access_token")
    private String accessToken;
    @Column(name = "server_id")
    private String serverId;
    @Column(unique = true)
    private UUID uuid;
    //Password and permissions
    @Column(name = "hash_type")
    @Enumerated(EnumType.ORDINAL)
    private HashType hashType = HashType.BCRYPT;
    private String password;
    private long permissions;
    private long flags;
    //Special
    @Column(unique = true)
    private String email;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hwid_id")
    private HardwareId hardwareId;
    //Economic info
    @Column(name = "economy_money")
    private long economyMoney;
    @Column(name = "donate_money")
    private double donateMoney;
    @Column(name = "extended_money")
    private double extendedMoney;
    //Addional info (may be null)
    @Enumerated(EnumType.ORDINAL)
    private Gender gender;
    private String status;
    @Column(name = "registration_date")
    private LocalDateTime registrationDate;
    @Column(name = "last_login")
    private LocalDateTime lastLoginDate;
    @Column(name = "totp_secret_key")
    private byte[] totpSecretKey;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserGroup> groups;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PaymentId> payments;

    public static boolean isCorrectEmail(String email) //Very simple check
    {
        return email != null && email.contains("@") && email.length() >= 3;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getEconomyMoney() {
        return economyMoney;
    }

    public void setEconomyMoney(long economyMoney) {
        this.economyMoney = economyMoney;
    }

    public double getDonateMoney() {
        return donateMoney;
    }

    public void setDonateMoney(double donateMoney) {
        this.donateMoney = donateMoney;
    }

    public double getExtendedMoney() {
        return extendedMoney;
    }

    public void setExtendedMoney(double extendedMoney) {
        this.extendedMoney = extendedMoney;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public HardwareId getHardwareId() {
        return hardwareId;
    }

    public void setHardwareId(HardwareId hardwareId) {
        this.hardwareId = hardwareId;
    }

    public byte[] getTotpSecretKey() {
        return totpSecretKey;
    }

    public void setTotpSecretKey(byte[] totpSecretKey) {
        this.totpSecretKey = totpSecretKey;
    }

    public List<UserGroup> getGroups() {
        return groups;
    }

    public void setGroups(List<UserGroup> groups) {
        this.groups = groups;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public ClientPermissions getPermissions() {
        return new ClientPermissions(permissions, flags);
    }

    @Override
    public void setPermissions(ClientPermissions permissions) {
        this.permissions = permissions.permissions;
        this.flags = permissions.flags;
    }

    @Override
    public boolean verifyPassword(String password) {
        return PasswordHelper.verifyPassword(hashType, this.password, password);
    }

    @Override
    public void setPassword(String password) {
        if (!hashType.isTrusted()) hashType = DEFAULT_PASSWORD_HASH;
        String newPassword = PasswordHelper.hashPassword(hashType, password);
        if (newPassword == null) {
            this.hashType = DEFAULT_PASSWORD_HASH;
            newPassword = PasswordHelper.hashPassword(hashType, password);
        }
        this.password = newPassword;
    }

    public void setRawPassword(String password) {
        this.password = password;
    }

    public void setRawPasswordType(HashType type) {
        this.hashType = type;
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String getServerID() {
        return serverId;
    }

    @Override
    public void setServerID(String serverID) {
        this.serverId = serverID;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDateTime getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(LocalDateTime lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public enum HashType {
        BCRYPT(true),
        DOUBLEMD5(false),
        MD5(false),
        SHA256(false),
        AUTHMESHA256(true),
        PHPASS(true);
        boolean trusted;

        HashType(boolean trusted) {
            this.trusted = trusted;
        }

        public boolean isTrusted() {
            return trusted;
        }
    }

    public enum Gender {
        FEMALE,
        MALE
    }
}
