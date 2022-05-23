package com.linktech.saihub.db.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class PollBean implements Serializable {

    private static final long serialVersionUID = 8546252256580107245L;
    @Id(autoincrement = true)
    private Long mId;

    String pollName = "";
    String pollUrl = "";
    @Generated(hash = 1632159522)
    public PollBean(Long mId, String pollName, String pollUrl) {
        this.mId = mId;
        this.pollName = pollName;
        this.pollUrl = pollUrl;
    }
    @Generated(hash = 653919914)
    public PollBean() {
    }
    public Long getMId() {
        return this.mId;
    }
    public void setMId(Long mId) {
        this.mId = mId;
    }
    public String getPollName() {
        return this.pollName;
    }
    public void setPollName(String pollName) {
        this.pollName = pollName;
    }
    public String getPollUrl() {
        return this.pollUrl;
    }
    public void setPollUrl(String pollUrl) {
        this.pollUrl = pollUrl;
    }
}
