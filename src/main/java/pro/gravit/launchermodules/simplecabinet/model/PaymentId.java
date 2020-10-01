package pro.gravit.launchermodules.simplecabinet.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
public class PaymentId {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;
    private double sum;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="user_id")
    private User user;
    private String description;
    private LocalDateTime initialTime;
    private LocalDateTime successTime;
    public enum PaymentStatus {
        CREATED, COMPLETED, FAILED
    }
    private PaymentStatus status;

    public int getId() {
        return id;
    }

    public double getSum() {
        return sum;
    }

    public User getUser() {
        return user;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public LocalDateTime getInitialTime() {
        return initialTime;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
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
}
