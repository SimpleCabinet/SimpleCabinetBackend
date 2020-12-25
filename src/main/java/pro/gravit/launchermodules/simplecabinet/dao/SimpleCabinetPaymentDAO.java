package pro.gravit.launchermodules.simplecabinet.dao;

import org.hibernate.*;
import pro.gravit.launchermodules.simplecabinet.model.PaymentId;
import pro.gravit.launchermodules.simplecabinet.model.User;

public class SimpleCabinetPaymentDAO {
    private transient final SessionFactory factory;

    public SimpleCabinetPaymentDAO(SessionFactory sessionFactory) {
        this.factory = sessionFactory;
    }

    public PaymentId findById(int id) {
        try (Session s = factory.openSession()) {
            return s.get(PaymentId.class, id);
        }
    }

    public User fetchUser(PaymentId paymentId) {
        try (Session session = factory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.lock(paymentId, LockMode.NONE);
            User user = paymentId.getUser();
            Hibernate.initialize(user);
            transaction.commit();
            return user;
        }
    }

    public void save(PaymentId user) {
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.save(user);
            tx1.commit();
        }
    }

    public void update(PaymentId user) {
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.update(user);
            tx1.commit();
        }
    }

    public void delete(PaymentId user) {
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.delete(user);
            tx1.commit();
        }
    }
}
