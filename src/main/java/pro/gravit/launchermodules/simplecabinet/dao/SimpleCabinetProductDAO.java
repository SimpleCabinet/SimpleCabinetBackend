package pro.gravit.launchermodules.simplecabinet.dao;

import org.hibernate.*;
import pro.gravit.launchermodules.simplecabinet.model.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class SimpleCabinetProductDAO {
    private final SessionFactory factory;

    public SimpleCabinetProductDAO(SessionFactory factory) {
        this.factory = factory;
    }

    public ProductEntity findById(long id) {
        try (Session s = factory.openSession()) {
            return s.get(ProductEntity.class, id);
        }
    }
    @SuppressWarnings("unchecked")
    public List<ProductEntity> fetchPage(int startId, int limit)
    {
        try (Session s = factory.openSession()) {
            Query query = s.createQuery("From Product");
            query.setFirstResult(startId);
            query.setMaxResults(limit);

            return (List<ProductEntity>) query.getResultList();
        }
    }

    public List<ProductEnchantEntity> fetchEnchantsInProduct(ProductEntity entity)
    {
        try (Session session = factory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.lock(entity, LockMode.NONE);
            List<ProductEnchantEntity> list = entity.getEnchants();
            Hibernate.initialize(list);
            transaction.commit();
            return list;
        }
    }

    public void save(ProductEntity productEntity) {
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.save(productEntity);
            tx1.commit();
        }
    }

    public void update(ProductEntity productEntity) {
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.update(productEntity);
            tx1.commit();
        }
    }

    public void delete(ProductEntity productEntity) {
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.delete(productEntity);
            tx1.commit();
        }
    }
}
