package practice.samay.ordermanagementsystem.dao;

import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import practice.samay.ordermanagementsystem.enums.PaymentStatus;
import practice.samay.ordermanagementsystem.model.Payment;

import java.util.List;
import java.util.Optional;

/**
 * Hibernate SessionFactory-based implementation of PaymentDao.
 */
@Repository
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PaymentDaoImpl implements PaymentDao {

    private static final Logger log = LoggerFactory.getLogger(PaymentDaoImpl.class);

    private final SessionFactory sessionFactory;

    @Override
    public Payment save(Payment payment) {
        log.debug("DAO: Persisting new payment with reference: {}", payment.getPaymentReference());
        sessionFactory.getCurrentSession().persist(payment);
        return payment;
    }

    @Override
    public Payment update(Payment payment) {
        log.debug("DAO: Merging payment with id: {}", payment.getId());
        return sessionFactory.getCurrentSession().merge(payment);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        log.debug("DAO: Finding payment by id: {}", id);
        return Optional.ofNullable(
                sessionFactory.getCurrentSession().get(Payment.class, id)
        );
    }

    @Override
    public Optional<Payment> findByPaymentReference(String paymentReference) {
        log.debug("DAO: Finding payment by reference: {}", paymentReference);
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Payment p WHERE p.paymentReference = :ref", Payment.class)
                .setParameter("ref", paymentReference)
                .uniqueResultOptional();
    }

    @Override
    public List<Payment> findByOrderId(Long orderId) {
        log.debug("DAO: Finding payments for order id: {}", orderId);
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Payment p WHERE p.orderId = :orderId ORDER BY p.createdAt DESC", Payment.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        log.debug("DAO: Finding payments with status: {}", status);
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Payment p WHERE p.status = :status ORDER BY p.createdAt DESC", Payment.class)
                .setParameter("status", status)
                .getResultList();
    }
}
