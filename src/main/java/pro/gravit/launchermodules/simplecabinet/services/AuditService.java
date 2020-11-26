package pro.gravit.launchermodules.simplecabinet.services;

import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.model.AuditEntity;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.LaunchServer;

import java.time.LocalDateTime;

public class AuditService {
    private transient final SimpleCabinetModule module;
    private transient final LaunchServer server;

    public AuditService(SimpleCabinetModule module, LaunchServer server) {
        this.module = module;
        this.server = server;
    }

    public void pushBaseAudit(AuditEntity.AuditType type, User user, String userIp, User target)
    {
        AuditEntity entity = new AuditEntity();
        entity.setUser(user);
        entity.setIp(userIp);
        entity.setTarget(target);
        entity.setTime(LocalDateTime.now());
        entity.setType(type);
        SimpleCabinetDAOProvider dao = (SimpleCabinetDAOProvider) server.config.dao;
        dao.auditDAO.save(entity);
    }

    public void pushAdvancedAudit(AuditEntity.AuditType type, User user, String userIp, User target, String arg1, String arg2)
    {
        AuditEntity entity = new AuditEntity();
        entity.setUser(user);
        entity.setIp(userIp);
        entity.setTarget(target);
        entity.setArg1(arg1);
        entity.setArg2(arg2);
        entity.setTime(LocalDateTime.now());
        entity.setType(type);
        SimpleCabinetDAOProvider dao = (SimpleCabinetDAOProvider) server.config.dao;
        dao.auditDAO.save(entity);
    }
}
