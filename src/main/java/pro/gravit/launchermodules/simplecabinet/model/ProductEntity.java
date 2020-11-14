package pro.gravit.launchermodules.simplecabinet.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class ProductEntity {
    @Id
    @GeneratedValue
    private long id;

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

    public LocalDateTime getSysDate() {
        return sysDate;
    }

    public void setSysDate(LocalDateTime sysDate) {
        this.sysDate = sysDate;
    }

    public enum ProductType {
        GROUP, ITEM
    }
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
    //Sys
    @Column(name = "sys_id")
    private String sysId;
    @Column(name = "sys_quantity")
    private int sysQuantity;
    @Column(name = "sys_extra")
    private String sysExtra;
    @Column(name = "sys_date")
    private LocalDateTime sysDate;


}
