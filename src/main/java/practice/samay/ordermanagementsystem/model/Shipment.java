package practice.samay.ordermanagementsystem.model;

import jakarta.persistence.*;
import lombok.*;
import practice.samay.ordermanagementsystem.enums.ShipmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(
    name = "shipments",
    indexes = {
        @Index(name = "idx_tracking_number",  columnList = "tracking_number"),
        @Index(name = "idx_shipment_order_id", columnList = "order_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "tracking_number", unique = true, nullable = false, length = 60)
    private String trackingNumber;

    @Column(name = "carrier", nullable = false, length = 100)
    private String carrier;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ShipmentStatus status;

    @Column(name = "shipping_address", nullable = false, columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "estimated_delivery")
    private LocalDate estimatedDelivery;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ShipmentStatus.PREPARING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
