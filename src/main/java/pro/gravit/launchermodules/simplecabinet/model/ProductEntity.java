package pro.gravit.launchermodules.simplecabinet.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class ProductEntity {
    @Id
    @GeneratedValue
    public long id;
    public enum ProductType {
        GROUP, ITEM
    }
    public ProductType type;
    public String name;
    public String description;
    public double price;
    //Limitations
    public long count;
    @Column(name = "end_date")
    public LocalDateTime endDate;
    @Column(name = "allow_stack")
    public boolean allowStack;
    public boolean visible;
    //Sys
    @Column(name = "sys_id")
    public String sysId;
    @Column(name = "sys_quantity")
    public int sysQuantity;
    @Column(name = "sys_extra")
    public String sysExtra;
    @Column(name = "sys_date")
    public LocalDateTime sysDate;
}
