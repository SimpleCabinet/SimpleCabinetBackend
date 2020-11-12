package pro.gravit.launchermodules.simplecabinet.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import pro.gravit.launchermodules.simplecabinet.model.AuditEntity;
import pro.gravit.launchermodules.simplecabinet.model.ProductEntity;

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
