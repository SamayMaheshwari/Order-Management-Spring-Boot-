package practice.samay.ordermanagementsystem.dao;

import org.hibernate.SessionFactory;

import java.util.Optional;

public abstract class GenericDao<T> {

    protected final SessionFactory sessionFactory;
    private final Class<T> entityClass;

    protected GenericDao(SessionFactory sessionFactory, Class<T> entityClass) {
        this.sessionFactory = sessionFactory;
        this.entityClass = entityClass;
    }

    public T save(T entity) {
        sessionFactory.getCurrentSession().persist(entity);
        return entity;
    }

    public T update(T entity) {
        return sessionFactory.getCurrentSession().merge(entity);
    }

    public Optional<T> findById(Long id) {
        return Optional.ofNullable(
                sessionFactory.getCurrentSession().get(entityClass, id)
        );
    }

    public void delete(T entity) {
        sessionFactory.getCurrentSession().remove(entity);
    }
}