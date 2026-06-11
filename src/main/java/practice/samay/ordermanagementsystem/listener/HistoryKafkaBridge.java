package practice.samay.ordermanagementsystem.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import practice.samay.ordermanagementsystem.event.HistoryDomainEvent;
import practice.samay.ordermanagementsystem.event.HistoryEvent;

@Component
@RequiredArgsConstructor
public class HistoryKafkaBridge {

	private final KafkaTemplate<String, HistoryEvent> kafkaTemplate;

	@Value("${app.kafka.history-topic}")
	private String historyTopic;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onHistoryDomainEvent(HistoryDomainEvent domainEvent) {
		HistoryEvent historyEvent = domainEvent.historyEvent();
		kafkaTemplate.send(historyTopic, historyEvent.objectType() + ":" + historyEvent.objectId(), historyEvent);
	}
}