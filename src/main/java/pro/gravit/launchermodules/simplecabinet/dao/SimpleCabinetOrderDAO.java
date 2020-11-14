package pro.gravit.launchermodules.simplecabinet.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import pro.gravit.launchermodules.simplecabinet.model.OrderEntity;
import pro.gravit.launchermodules.simplecabinet.model.ProductEntity;

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
