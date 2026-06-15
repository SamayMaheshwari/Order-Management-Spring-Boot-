package practice.samay.ordermanagementsystem.dao;

import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import practice.samay.ordermanagementsystem.model.HistoryRecord;

@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class HistoryDaoImpl {

	private final SessionFactory sessionFactory;

	public HistoryRecord save(HistoryRecord historyRecord) {
		sessionFactory.getCurrentSession().persist(historyRecord);
		return historyRecord;
	}
}