package pro.gravit.launchermodules.simplecabinet.dao;

import org.hibernate.*;
import pro.gravit.launchermodules.simplecabinet.model.OrderEntity;
import pro.gravit.launchermodules.simplecabinet.model.ProductEnchantEntity;
import pro.gravit.launchermodules.simplecabinet.model.ProductEntity;

import javax.persistence.Query;
import java.util.List;

public class SimpleCabinetOrderDAO {
    private final SessionFactory factory;

    public SimpleCabinetOrderDAO(SessionFactory factory) {
        this.factory = factory;
    }

    public OrderEntity findById(long id) {
        try (Session s = factory.openSession()) {
            return s.get(OrderEntity.class, id);
        }
    }

    @SuppressWarnings("unchecked")
    public List<OrderEntity> fetchPage(int startId, int limit)
    {
        try (Session s = factory.openSession()) {
            Query query = s.createQuery("From Order");
            query.setFirstResult(startId);
            query.setMaxResults(limit);

            return (List<OrderEntity>) query.getResultList();
        }
    }

    public ProductEntity fetchProductInOrder(OrderEntity entity)
    {
        try (Session session = factory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.lock(entity, LockMode.NONE);
            ProductEntity product = entity.getProduct();
            Hibernate.initialize(product);
            transaction.commit();
            return product;
        }
    }

    public void save(OrderEntity productEntity) {
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.save(productEntity);
            tx1.commit();
        }
    }

    public void update(OrderEntity productEntity) {
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.update(productEntity);
            tx1.commit();
        }
    }

    public void delete(OrderEntity productEntity) {
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.delete(productEntity);
            tx1.commit();
        }
    }
}
