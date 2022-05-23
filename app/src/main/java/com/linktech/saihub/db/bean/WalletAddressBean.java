package com.linktech.saihub.db.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;

/**
 * 钱包地址-地址本地址
 */
@Entity
public class WalletAddressBean implements Serializable {
    static final long serialVersionUID = 42L;

    @Id(autoincrement = true)
    private Long mId;

    private String addressName;
    private String address;
    private long createTime;

    @Generated(hash = 445357259)
    public WalletAddressBean(Long mId, String addressName, String address,
                             long createTime) {
        this.mId = mId;
        this.addressName = addressName;
        this.address = address;
        this.createTime = createTime;
    }

    @Generated(hash = 1919460458)
    public WalletAddressBean() {
    }

    public Long getMId() {
        return this.mId;
    }

    public void setMId(Long mId) {
        this.mId = mId;
    }

    public String getAddressName() {
        return this.addressName;
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }


    public WalletAddressBean cloneData() {
        return new WalletAddressBean(mId, addressName, address, createTime);
    }
}
