package practice.samay.ordermanagementsystem.model;

import jakarta.persistence.*;
import lombok.*;
import practice.samay.ordermanagementsystem.enums.PaymentMethod;
import practice.samay.ordermanagementsystem.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_payment_reference", columnList = "payment_reference"),
        @Index(name = "idx_payment_order_id",  columnList = "order_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_reference", unique = true, nullable = false, length = 60)
    private String paymentReference;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = PaymentStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
