package pro.gravit.launchermodules.simplecabinet.model;

import javax.persistence.*;

@Entity(name = "Order")
@Table(name = "orders")
public class OrderEntity {
    @Id
    @GeneratedValue
    private long id;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private ProductEntity product;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    private double sum;
    private int quantity;
    private OrderStatus status;
    @Column(name = "sys_part")
    private int sysPart;

    public long getId() {
        return id;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public int getSysPart() {
        return sysPart;
    }

    public void setSysPart(int sysPart) {
        this.sysPart = sysPart;
    }

    public enum OrderStatus {
        CREATED, PROCESS, DELIVERY, FINISHED, FAILED
    }
}
