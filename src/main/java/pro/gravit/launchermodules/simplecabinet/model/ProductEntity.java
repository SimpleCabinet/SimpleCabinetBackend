package pro.gravit.launchermodules.simplecabinet.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity(name = "Product")
@Table(name = "products")
public class ProductEntity {
    @Id
    @GeneratedValue
    private long id;
    private ProductType type;
    private String name;
    private String description;
    private double price;
    //Limitations
    private long count;
    @Column(name = "end_date")
    private LocalDateTime endDate;
    @Column(name = "allow_stack")
    private boolean allowStack;
    private boolean visible;
    @Column(name = "picture_url")
    private String pictureUrl;
    //Sys
    @Column(name = "sys_id")
    private String sysId;
    @Column(name = "sys_quantity")
    private int sysQuantity;
    @Column(name = "sys_extra")
    private String sysExtra;
    @Column(name = "sys_nbt")
    private String sysNbt;
    @Column(name = "sys_delivery_provider")
    private String sysDeliveryProvider;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductEnchantEntity> enchants;

    public long getId() {
        return id;
    }

    public ProductType getType() {
        return type;
    }

    public void setType(ProductType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public boolean isAllowStack() {
        return allowStack;
    }

    public void setAllowStack(boolean allowStack) {
        this.allowStack = allowStack;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getSysId() {
        return sysId;
    }

    public void setSysId(String sysId) {
        this.sysId = sysId;
    }

    public int getSysQuantity() {
        return sysQuantity;
    }

    public void setSysQuantity(int sysQuantity) {
        this.sysQuantity = sysQuantity;
    }

    public String getSysExtra() {
        return sysExtra;
    }

    public void setSysExtra(String sysExtra) {
        this.sysExtra = sysExtra;
    }

    public List<ProductEnchantEntity> getEnchants() {
        return enchants;
    }

    public void setEnchants(List<ProductEnchantEntity> enchants) {
        this.enchants = enchants;
    }

    public String getSysNbt() {
        return sysNbt;
    }

    public void setSysNbt(String sysNbt) {
        this.sysNbt = sysNbt;
    }

    public String getSysDeliveryProvider() {
        return sysDeliveryProvider;
    }

    public void setSysDeliveryProvider(String sysDeliveryProvider) {
        this.sysDeliveryProvider = sysDeliveryProvider;
    }

    public enum ProductType {
        GROUP, ITEM, SPECIAL
    }

}
