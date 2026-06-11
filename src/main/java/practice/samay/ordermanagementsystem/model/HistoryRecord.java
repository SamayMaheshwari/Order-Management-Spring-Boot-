package practice.samay.ordermanagementsystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
		name = "history_records",
		indexes = {
				@Index(name = "idx_history_object_type", columnList = "object_type"),
				@Index(name = "idx_history_object_id", columnList = "object_id")
		}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "object_type", nullable = false, length = 50)
	private String objectType;

	@Column(name = "object_id", nullable = false)
	private Long objectId;

	@Column(name = "action", nullable = false, length = 20)
	private String action;

	@Column(name = "event_payload", columnDefinition = "LONGTEXT")
	private String eventPayload;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}
}