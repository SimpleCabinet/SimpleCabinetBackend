package pro.gravit.launchermodules.simplecabinet.model;

import javax.persistence.*;

@Entity
@Table(name = "product_enchants")
public class ProductEnchantEntity {
    @Id
    @GeneratedValue
    public long id;
    @ManyToOne
    @JoinColumn(name = "product_id")
    public ProductEntity product;
    public String name;
    public int value;
}
