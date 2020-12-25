package pro.gravit.launchermodules.simplecabinet.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity(name = "PaymentId")
@Table(name = "payments")
public class PaymentId {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;
    private double sum;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;
    private String description;
    @Column(name = "initial_time")
    private LocalDateTime initialTime;
    @Column(name = "success_time")
    private LocalDateTime successTime;
    private PaymentStatus status;

    public int getId() {
        return id;
    }

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getInitialTime() {
        return initialTime;
    }

    public void setInitialTime(LocalDateTime initialTime) {
        this.initialTime = initialTime;
    }

    public LocalDateTime getSuccessTime() {
        return successTime;
    }

    public void setSuccessTime(LocalDateTime successTime) {
        this.successTime = successTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public enum PaymentStatus {
        CREATED, COMPLETED, FAILED
    }
}
