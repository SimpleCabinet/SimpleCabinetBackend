package pro.gravit.launchermodules.simplecabinet.dao;

import org.hibernate.*;
import pro.gravit.launchermodules.simplecabinet.model.HardwareId;
import pro.gravit.launchermodules.simplecabinet.model.PasswordResetEntity;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchermodules.simplecabinet.model.UserGroup;
import pro.gravit.launchserver.dao.UserDAO;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.UUID;

public class SimpleCabinetUserDAO implements UserDAO {
    private final SessionFactory factory;

    public SimpleCabinetUserDAO(SessionFactory sessionFactory) {
        this.factory = sessionFactory;
    }

    public User findById(long id) {
        try (Session s = factory.openSession()) {
            return s.get(User.class, id);
        }
    }

    public PasswordResetEntity findPasswordResetById(long id) {
        try (Session s = factory.openSession()) {
            return s.get(PasswordResetEntity.class, id);
        }
    }

    @Override
    public User findById(int id) {
        return findById((long)id);
    }

    public User findByUsername(String username) {
        EntityManager em = factory.createEntityManager();
        em.getTransaction().begin();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> personCriteria = cb.createQuery(User.class);
        Root<User> rootUser = personCriteria.from(User.class);
        personCriteria.select(rootUser).where(cb.equal(rootUser.get("username"), username));
        List<User> ret = em.createQuery(personCriteria).getResultList();
        em.close();
        return ret.size() == 0 ? null : ret.get(0);
    }

    public User findByEmail(String email) {
        EntityManager em = factory.createEntityManager();
        em.getTransaction().begin();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> personCriteria = cb.createQuery(User.class);
        Root<User> rootUser = personCriteria.from(User.class);
        personCriteria.select(rootUser).where(cb.equal(rootUser.get("email"), email));
        List<User> ret = em.createQuery(personCriteria).getResultList();
        em.close();
        return ret.size() == 0 ? null : ret.get(0);
    }

    public User findByUUID(UUID uuid) {
        EntityManager em = factory.createEntityManager();
        em.getTransaction().begin();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> personCriteria = cb.createQuery(User.class);
        Root<User> rootUser = personCriteria.from(User.class);
        personCriteria.select(rootUser).where(cb.equal(rootUser.get("uuid"), uuid));
        List<User> ret = em.createQuery(personCriteria).getResultList();
        em.close();
        return ret.size() == 0 ? null : ret.get(0);
    }

    public HardwareId fetchHardwareId(User user)
    {
        try (Session session = factory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.lock(user, LockMode.NONE);
            HardwareId id = user.getHardwareId();
            Hibernate.initialize(id);
            transaction.commit();
            return id;
        }
    }

    public List<UserGroup> fetchGroups(User user)
    {
        try (Session session = factory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.lock(user, LockMode.NONE);
            List<UserGroup> groups = user.getGroups();
            Hibernate.initialize(groups);
            transaction.commit();
            return groups;
        }
    }

    public void save(pro.gravit.launchserver.dao.User user) {
        if(!(user instanceof User)) throw new IllegalArgumentException("User type unsupported");
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.save(user);
            tx1.commit();
        }
    }

    public void update(pro.gravit.launchserver.dao.User user) {
        if(!(user instanceof User)) throw new IllegalArgumentException("User type unsupported");
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.update(user);
            tx1.commit();
        }
    }

    public void delete(pro.gravit.launchserver.dao.User user) {
        if(!(user instanceof User)) throw new IllegalArgumentException("User type unsupported");
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.delete(user);
            tx1.commit();
        }
    }

    public void save(UserGroup user) {
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.save(user);
            tx1.commit();
        }
    }

    public void update(UserGroup user) {
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.update(user);
            tx1.commit();
        }
    }

    public void delete(UserGroup user) {
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.delete(user);
            tx1.commit();
        }
    }

    public void save(PasswordResetEntity user) {
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.save(user);
            tx1.commit();
        }
    }

    public void update(PasswordResetEntity user) {
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.update(user);
            tx1.commit();
        }
    }

    public void delete(PasswordResetEntity user) {
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.delete(user);
            tx1.commit();
        }
    }

    @SuppressWarnings("unchecked")
    public List<pro.gravit.launchserver.dao.User> findAll() {
        try (Session s = factory.openSession()) {
            return (List<pro.gravit.launchserver.dao.User>) s.createQuery("From User").list();
        }
    }
}
