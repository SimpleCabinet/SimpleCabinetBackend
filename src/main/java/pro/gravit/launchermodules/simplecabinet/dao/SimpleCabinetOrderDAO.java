package pro.gravit.launchermodules.simplecabinet.dao;

import org.hibernate.*;
import pro.gravit.launchermodules.simplecabinet.model.OrderEntity;
import pro.gravit.launchermodules.simplecabinet.model.ProductEntity;
import pro.gravit.launchermodules.simplecabinet.model.User;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
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
    public List<OrderEntity> fetchPage(int startId, int limit, OrderEntity.OrderStatus status, User user) {
        EntityManager em = factory.createEntityManager();
        em.getTransaction().begin();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderEntity> personCriteria = cb.createQuery(OrderEntity.class);
        Root<OrderEntity> rootUser = personCriteria.from(OrderEntity.class);
        Expression<Boolean> where = null;
        personCriteria.select(rootUser);
        if (status != null) {
            where = cb.equal(rootUser.get("status"), status);
        }
        if (user != null) {
            if (where == null) where = cb.equal(rootUser.get("user"), user);
            else where = cb.and(where, cb.equal(rootUser.get("user"), user));
        }
        if (where != null) personCriteria.where(where);
        Query query = em.createQuery(personCriteria);

        query.setFirstResult(startId);
        query.setMaxResults(limit);
        List<OrderEntity> result = query.getResultList();
        em.close();

        return result;
    }

    public ProductEntity fetchProductInOrder(OrderEntity entity) {
        try (Session session = factory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.lock(entity, LockMode.NONE);
            ProductEntity product = entity.getProduct();
            Hibernate.initialize(product);
            transaction.commit();
            return product;
        }
    }

    public User fetchUserInOrder(OrderEntity entity) {
        try (Session session = factory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.lock(entity, LockMode.NONE);
            User user = entity.getUser();
            Hibernate.initialize(user);
            transaction.commit();
            return user;
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
