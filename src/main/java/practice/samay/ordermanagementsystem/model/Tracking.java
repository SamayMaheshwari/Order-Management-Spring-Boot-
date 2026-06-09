package practice.samay.ordermanagementsystem.model;

import jakarta.persistence.*;
import lombok.*;
import practice.samay.ordermanagementsystem.enums.ShipmentStatus;

import java.time.LocalDateTime;


@Entity
@Table(
    name = "tracking_events",
    indexes = {
        @Index(name = "idx_tracking_shipment_id", columnList = "shipment_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shipment_id", nullable = false)
    private Long shipmentId;

    @Column(name = "location", nullable = false, length = 200)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private ShipmentStatus status;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_timestamp", nullable = false)
    private LocalDateTime eventTimestamp;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.eventTimestamp == null) {
            this.eventTimestamp = LocalDateTime.now();
        }
    }
}
