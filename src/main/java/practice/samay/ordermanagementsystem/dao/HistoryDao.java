package practice.samay.ordermanagementsystem.dao;

import practice.samay.ordermanagementsystem.model.HistoryRecord;

public interface HistoryDao {

	HistoryRecord save(HistoryRecord historyRecord);
}