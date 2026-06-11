package practice.samay.ordermanagementsystem.event;

import java.io.Serializable;
import java.time.LocalDateTime;

public record HistoryEvent(

		String objectType,
		Long objectId,
		String action,
		String payload,
		LocalDateTime eventTimestamp
) implements Serializable {
}