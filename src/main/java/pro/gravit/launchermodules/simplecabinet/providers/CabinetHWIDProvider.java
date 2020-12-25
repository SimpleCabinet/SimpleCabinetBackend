package pro.gravit.launchermodules.simplecabinet.providers;

import pro.gravit.launcher.request.secure.HardwareReportRequest;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetDAOProvider;
import pro.gravit.launchermodules.simplecabinet.SimpleCabinetModule;
import pro.gravit.launchermodules.simplecabinet.dao.SimpleCabinetHwidDAO;
import pro.gravit.launchermodules.simplecabinet.dao.SimpleCabinetUserDAO;
import pro.gravit.launchermodules.simplecabinet.model.HardwareId;
import pro.gravit.launchermodules.simplecabinet.model.HardwareIdLogEntity;
import pro.gravit.launchermodules.simplecabinet.model.User;
import pro.gravit.launchserver.LaunchServer;
import pro.gravit.launchserver.Reconfigurable;
import pro.gravit.launchserver.auth.protect.hwid.HWIDException;
import pro.gravit.launchserver.auth.protect.hwid.HWIDProvider;
import pro.gravit.launchserver.socket.Client;
import pro.gravit.utils.command.Command;
import pro.gravit.utils.command.SubCommand;
import pro.gravit.utils.helper.LogHelper;

import java.util.HashMap;
import java.util.Map;

public class CabinetHWIDProvider extends HWIDProvider implements Reconfigurable {
    public double criticalCompareLevel = 1.0;
    private transient SimpleCabinetModule module;
    private transient SimpleCabinetHwidDAO hwidDAO;
    private transient SimpleCabinetUserDAO userDAO;
    private transient LaunchServer server;

    @Override
    public void init(LaunchServer server) {
        this.server = server;
        module = server.modulesManager.getModule(SimpleCabinetModule.class);
        hwidDAO = ((SimpleCabinetDAOProvider) server.config.dao).hwidDAO;
        userDAO = (SimpleCabinetUserDAO) server.config.dao.userDAO;
    }

    @Override
    public HardwareReportRequest.HardwareInfo findHardwareInfoByPublicKey(byte[] bytes, Client client) throws HWIDException {
        HardwareId id = hwidDAO.findByPublicKey(bytes);
        if (id != null && id.isBanned()) {
            throw new SecurityException("Your HWID banned");
        }
        return id == null ? null : id.toHardwareInfo();
    }

    @Override
    public void createHardwareInfo(HardwareReportRequest.HardwareInfo hardwareInfo, byte[] bytes, Client client) throws HWIDException {
        HardwareId hardwareId = new HardwareId();
        hardwareId.loadFromHardwareInfo(hardwareInfo);
        hardwareId.setPublicKey(bytes);
        hwidDAO.save(hardwareId);
        hwidDAO.saveLog(new HardwareIdLogEntity(hardwareId, bytes));
        if (client.daoObject != null) {
            User user = (User) client.daoObject;
            user.setHardwareId(hardwareId);
            userDAO.update(user);
        }
    }

    @Override
    public boolean addPublicKeyToHardwareInfo(HardwareReportRequest.HardwareInfo hardwareInfo, byte[] bytes, Client client) throws HWIDException {
        if (client.daoObject == null) {
            throw new SecurityException("Your account not connected to cabinet");
        }
        HardwareId id = hwidDAO.findHardwareForAll((hwid) -> {
            HardwareInfoCompareResult result = compareHardwareInfo(hwid.toHardwareInfo(), hardwareInfo);
            return result.compareLevel > criticalCompareLevel;
        });
        if (id == null) return false;
        if (id.isBanned()) {
            throw new SecurityException("Your HWID banned");
        }
        id.setPublicKey(bytes);
        hwidDAO.update(id);
        hwidDAO.saveLog(new HardwareIdLogEntity(id, bytes));
        if (client.daoObject != null) {
            User user = (User) client.daoObject;
            user.setHardwareId(id);
            userDAO.update(user);
            return true;
        }
        return false;
    }

    @Override
    public Map<String, Command> getCommands() {
        Map<String, Command> result = new HashMap<>();
        result.put("hardwareban", new SubCommand() {
            @Override
            public void invoke(String... strings) throws Exception {
                verifyArgs(strings, 1);
                User user = userDAO.findByUsername(strings[0]);
                if (user == null) {
                    throw new IllegalArgumentException(String.format("User %s not found", strings[0]));
                }
                HardwareId id = userDAO.fetchHardwareId(user);
                if (id == null) {
                    LogHelper.error("User hwid not found");
                    return;
                }
                if (id.isBanned()) {
                    LogHelper.info("User %s already banned", user.getUsername());
                } else {
                    id.setBanned(true);
                    hwidDAO.update(id);
                    LogHelper.info("User %s banned", user.getUsername());
                }
            }
        });
        result.put("hardwareunban", new SubCommand() {
            @Override
            public void invoke(String... strings) throws Exception {
                verifyArgs(strings, 1);
                User user = userDAO.findByUsername(strings[0]);
                if (user == null) {
                    throw new IllegalArgumentException(String.format("User %s not found", strings[0]));
                }
                HardwareId id = userDAO.fetchHardwareId(user);
                if (id == null) {
                    LogHelper.error("User hwid not found");
                    return;
                }
                if (!id.isBanned()) {
                    LogHelper.info("User %s not banned", user.getUsername());
                } else {
                    id.setBanned(false);
                    hwidDAO.update(id);
                    LogHelper.info("User %s unbanned", user.getUsername());
                }
            }
        });
        result.put("hardwareinfo", new SubCommand() {
            @Override
            public void invoke(String... strings) throws Exception {
                verifyArgs(strings, 1);
                User user = userDAO.findByUsername(strings[0]);
                if (user == null) {
                    throw new IllegalArgumentException(String.format("User %s not found", strings[0]));
                }
                HardwareId id = userDAO.fetchHardwareId(user);
                String username = user.getUsername();
                if (id == null) {
                    LogHelper.error("User hwid not found");
                    return;
                }
                LogHelper.info("[%s] baseboardSerialNumber: %s", username, id.getBaseboardSerialNumber() == null ? "null" : id.getBaseboardSerialNumber());
                LogHelper.info("[%s] hwDiskId: %s", username, id.getHwDiskId() == null ? "null" : id.getHwDiskId());
                LogHelper.info("[%s] Processor: %d freq ( %d cores %d threads)", username, id.getProcessorMaxFreq(), id.getPhysicalProcessors(), id.getLogicalProcessors());
                LogHelper.info("[%s] Memory %d bytes ( %.2f GBytes )", username, id.getTotalMemory(), (double) id.getTotalMemory() / (1 << 30));

            }
        });
        result.put("hardwarecompare", new SubCommand() {
            @Override
            public void invoke(String... strings) throws Exception {
                verifyArgs(strings, 2);
                HardwareId id1 = hwidDAO.findById(Integer.parseInt(strings[0]));
                HardwareId id2 = hwidDAO.findById(Integer.parseInt(strings[1]));
                if (id1 == null) {
                    LogHelper.error("First HWID not found");
                    return;
                }
                if (id2 == null) {
                    LogHelper.error("Second HWID not found");
                    return;
                }
                HardwareInfoCompareResult data = compareHardwareInfo(id1.toHardwareInfo(), id2.toHardwareInfo());
                LogHelper.info("compareLevel %.3f, spoofLevels: %.3f first | %.3f second", data.compareLevel, data.firstSpoofingLevel, data.secondSpoofingLevel);
            }
        });
        return result;
    }
}
