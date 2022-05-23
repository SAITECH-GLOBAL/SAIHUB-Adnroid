package com.linktech.saihub.db.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;

@Entity
public class PowerBean implements Serializable {

    private static final long serialVersionUID = 8546252256580107245L;
    @Id(autoincrement = true)
    private Long mId;

    String name = "";
    String number = "";

    @Generated(hash = 1380388062)
    public PowerBean(Long mId, String name, String number) {
        this.mId = mId;
        this.name = name;
        this.number = number;
    }

    @Generated(hash = 1154027371)
    public PowerBean() {
    }

    public Long getMId() {
        return this.mId;
    }

    public void setMId(Long mId) {
        this.mId = mId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }


    public PowerBean cloneData() {
        return new PowerBean(mId, name, number);
    }
}
