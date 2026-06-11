package practice.samay.ordermanagementsystem.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import practice.samay.ordermanagementsystem.event.HistoryDomainEvent;
import practice.samay.ordermanagementsystem.event.HistoryEvent;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class HistoryEventPublisher {

	private final ApplicationEventPublisher applicationEventPublisher;
	private final ObjectMapper objectMapper;

	public void publish(String objectType, Long objectId, String action, Object payload) {
		try {
			HistoryEvent historyEvent = new HistoryEvent(
					objectType,
					objectId,
					action,
					objectMapper.writeValueAsString(payload),
					LocalDateTime.now()
			);
			applicationEventPublisher.publishEvent(new HistoryDomainEvent(historyEvent));
		} catch (Exception exception) {
			throw new IllegalStateException("Failed to publish history event", exception);
		}
	}
}