package pro.gravit.launchermodules.simplecabinet.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import pro.gravit.launchermodules.simplecabinet.model.AuditEntity;

public class SimpleCabinetAuditDAO {
    private final SessionFactory factory;

    public SimpleCabinetAuditDAO(SessionFactory factory) {
        this.factory = factory;
    }

    public AuditEntity findById(long id) {
        try (Session s = factory.openSession()) {
            return s.get(AuditEntity.class, id);
        }
    }

    public void save(AuditEntity auditEntity) {
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.save(auditEntity);
            tx1.commit();
        }
    }

    public void update(AuditEntity auditEntity) {
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.update(auditEntity);
            tx1.commit();
        }
    }

    public void delete(AuditEntity auditEntity) {
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.delete(auditEntity);
            tx1.commit();
        }
    }
}
