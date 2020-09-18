package pro.gravit.launchermodules.simplecabinet.dao;

import org.hibernate.*;
import org.hibernate.query.Query;
import pro.gravit.launchermodules.simplecabinet.model.HardwareId;
import pro.gravit.launchermodules.simplecabinet.model.HardwareIdLogEntity;
import pro.gravit.launchermodules.simplecabinet.model.User;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.function.Predicate;

public class SimpleCabinetHwidDAO {
    private final SessionFactory factory;

    public SimpleCabinetHwidDAO(SessionFactory factory) {
        this.factory = factory;
    }

    public HardwareId findById(long id) {
        try (Session s = factory.openSession()) {
            return s.get(HardwareId.class, id);
        }
    }

    public HardwareId findByPublicKey(byte[] publicKey) {
        EntityManager em = factory.createEntityManager();
        em.getTransaction().begin();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<HardwareId> personCriteria = cb.createQuery(HardwareId.class);
        Root<HardwareId> rootUser = personCriteria.from(HardwareId.class);
        personCriteria.select(rootUser).where(cb.equal(rootUser.get("publicKey"), publicKey));
        List<HardwareId> ret = em.createQuery(personCriteria).getResultList();
        em.close();
        return ret.size() == 0 ? null : ret.get(0);
    }

    public HardwareId findHardwareForAll(Predicate<HardwareId> predicate) //WARNING: This operation very slow(filter by application)
    {
        try(StatelessSession session = factory.openStatelessSession())
        {
            session.setJdbcBatchSize(256);
            CriteriaBuilder cb = factory.getCriteriaBuilder();
            CriteriaQuery<HardwareId> personCriteria = cb.createQuery(HardwareId.class);
            Root<HardwareId> rootUser = personCriteria.from(HardwareId.class);
            personCriteria.select(rootUser);
            ScrollableResults ret = session.createQuery(personCriteria)
                    .setReadOnly(true)

                    .scroll(ScrollMode.FORWARD_ONLY);
            while( ret.next() )
            {
                HardwareId id = (HardwareId) ret.get(0);
                if(predicate.test(id))
                {
                    return id;
                }
            }
        }
        return null;
    }

    public void save(HardwareId hardwareId) {
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.save(hardwareId);
            tx1.commit();
        }
    }

    public void saveLog(HardwareIdLogEntity hardwareId) {
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.save(hardwareId);
            tx1.commit();
        }
    }

    public void update(HardwareId hardwareId) {
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.update(hardwareId);
            tx1.commit();
        }
    }

    public void delete(HardwareId hardwareId) {
        try (Session session = factory.openSession()) {
            Transaction tx1 = session.beginTransaction();
            session.delete(hardwareId);
            tx1.commit();
        }
    }

    @SuppressWarnings("unchecked")
    public List<HardwareId> findAll() {
        try (Session s = factory.openSession()) {
            return (List<HardwareId>) s.createQuery("From HardwareId").list();
        }
    }
}
