package practice.samay.ordermanagementsystem.dao;

import practice.samay.ordermanagementsystem.enums.PaymentStatus;
import practice.samay.ordermanagementsystem.model.Payment;

import java.util.List;
import java.util.Optional;

/**
 * DAO interface for Payment entity operations.
 * Implementations use Hibernate SessionFactory directly.
 */
public interface PaymentDao {

    Payment save(Payment payment);

    Payment update(Payment payment);

    Optional<Payment> findById(Long id);

    Optional<Payment> findByPaymentReference(String paymentReference);

    List<Payment> findByOrderId(Long orderId);

    List<Payment> findByStatus(PaymentStatus status);
}
