package practice.samay.ordermanagementsystem.dao;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import practice.samay.ordermanagementsystem.enums.PaymentStatus;
import practice.samay.ordermanagementsystem.model.Payment;

import java.util.List;
import java.util.Optional;

@Repository
public class PaymentDaoImpl extends GenericDao<Payment> {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentDaoImpl.class);

    public PaymentDaoImpl(SessionFactory sessionFactory) {
        super(sessionFactory, Payment.class);
    }

    public Optional<Payment> findByPaymentReference(String paymentReference) {

        log.debug(
                "DAO: Finding payment by reference: {}",
                paymentReference);

        return sessionFactory.getCurrentSession()
                .createQuery(
                        "FROM Payment p WHERE p.paymentReference = :ref",
                        Payment.class)
                .setParameter("ref", paymentReference)
                .uniqueResultOptional();
    }

    public List<Payment> findByOrderId(Long orderId) {

        log.debug("DAO: Finding payments for order id: {}", orderId);

        return sessionFactory.getCurrentSession()
                .createQuery(
                        "FROM Payment p WHERE p.orderId = :orderId " +
                                "ORDER BY p.createdAt DESC",
                        Payment.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    public List<Payment> findByStatus(PaymentStatus status) {

        log.debug("DAO: Finding payments with status: {}", status);

        return sessionFactory.getCurrentSession()
                .createQuery(
                        "FROM Payment p WHERE p.status = :status " +
                                "ORDER BY p.createdAt DESC",
                        Payment.class)
                .setParameter("status", status)
                .getResultList();
    }
}
