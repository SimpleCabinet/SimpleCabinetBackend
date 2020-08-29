package pro.gravit.launchermodules.simplecabinet.model;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "UserGroup")
@Table(name = "user_groups")
public class UserGroup {
    @Id
    private long id;
    private String groupName;
    private Date startDate;
    private Date endDate;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public long getId() {
        return id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
