package practice.samay.ordermanagementsystem.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import practice.samay.ordermanagementsystem.dao.HistoryDaoImpl;
import practice.samay.ordermanagementsystem.event.HistoryEvent;
import practice.samay.ordermanagementsystem.model.HistoryRecord;

@Component
@RequiredArgsConstructor
public class HistoryKafkaConsumer {

	private final HistoryDaoImpl historyDao;

	@KafkaListener(topics = "${app.kafka.history-topic}", groupId = "${spring.kafka.consumer.group-id}")
	@Transactional
	public void consume(HistoryEvent historyEvent) {
		HistoryRecord historyRecord = HistoryRecord.builder()
				.objectType(historyEvent.objectType())
				.objectId(historyEvent.objectId())
				.action(historyEvent.action())
				.eventPayload(historyEvent.payload())
				.build();
		historyDao.save(historyRecord);
	}
}