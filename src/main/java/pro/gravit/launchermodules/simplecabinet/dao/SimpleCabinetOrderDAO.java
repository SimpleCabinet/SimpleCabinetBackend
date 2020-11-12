package pro.gravit.launchermodules.simplecabinet.dao;

import org.hibernate.SessionFactory;

public class SimpleCabinetOrderDAO {
    private final SessionFactory factory;

    public SimpleCabinetOrderDAO(SessionFactory factory) {
        this.factory = factory;
    }
}
