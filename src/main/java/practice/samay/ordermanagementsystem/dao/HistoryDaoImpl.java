package practice.samay.ordermanagementsystem.dao;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import practice.samay.ordermanagementsystem.model.HistoryRecord;

@Repository
public class HistoryDaoImpl extends GenericDao<HistoryRecord> {

	public HistoryDaoImpl(SessionFactory sessionFactory) {
		super(sessionFactory, HistoryRecord.class);
	}
}

