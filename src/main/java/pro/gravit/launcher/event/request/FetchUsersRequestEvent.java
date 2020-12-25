package pro.gravit.launcher.event.request;

import pro.gravit.launcher.events.RequestEvent;
import pro.gravit.launchermodules.simplecabinet.model.User;

import java.util.List;
import java.util.UUID;

public class FetchUsersRequestEvent extends RequestEvent {
    public List<UserPublicInfo> list;
    public int maxQuery;

    public FetchUsersRequestEvent(List<UserPublicInfo> list, int maxQuery) {
        this.list = list;
        this.maxQuery = maxQuery;
    }

    @Override
    public String getType() {
        return "lkFetchUsers";
    }

    public static class UserPublicInfo {
        public String username;
        public UUID uuid;
        public User.Gender gender;

        public UserPublicInfo(String username, UUID uuid, User.Gender gender) {
            this.username = username;
            this.uuid = uuid;
            this.gender = gender;
        }
    }
}
