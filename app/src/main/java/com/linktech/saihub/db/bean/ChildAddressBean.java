package com.linktech.saihub.db.bean;

import com.linktech.saihub.app.Constants;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.io.Serializable;

@Entity
public class ChildAddressBean implements Serializable {

    private static final long serialVersionUID = 8546252256580107245L;


    @Id(autoincrement = true)
    private Long mId;

    public Long walletId;
    private String childAddress;
    private String protocol;//协议

    private int childNumber;//当前子地址number
    private int childType;//0隔离原生地址  1隔离兼容地址 2普通地址
    private boolean isSelect;
    private boolean isShow; //是否显示
    private String privateKey; //私钥

    private int childChangeType;//0 普通地址  1 找零地址


    @Transient
    private String balanceStr = "0";


    @Generated(hash = 151703219)
    public ChildAddressBean(Long mId, Long walletId, String childAddress,
                            String protocol, int childNumber, int childType, boolean isSelect,
                            boolean isShow, String privateKey, int childChangeType) {
        this.mId = mId;
        this.walletId = walletId;
        this.childAddress = childAddress;
        this.protocol = protocol;
        this.childNumber = childNumber;
        this.childType = childType;
        this.isSelect = isSelect;
        this.isShow = isShow;
        this.privateKey = privateKey;
        this.childChangeType = childChangeType;
    }


    @Generated(hash = 640369235)
    public ChildAddressBean() {
    }


    public Long getMId() {
        return this.mId;
    }


    public void setMId(Long mId) {
        this.mId = mId;
    }


    public Long getWalletId() {
        return this.walletId;
    }


    public void setWalletId(Long walletId) {
        this.walletId = walletId;
    }


    public String getChildAddress() {
        return this.childAddress;
    }


    public void setChildAddress(String childAddress) {
        this.childAddress = childAddress;
    }


    public String getProtocol() {
        return this.protocol;
    }


    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }


    public int getChildNumber() {
        return this.childNumber;
    }


    public void setChildNumber(int childNumber) {
        this.childNumber = childNumber;
    }


    public int getChildType() {
        return this.childType;
    }


    public void setChildType(int childType) {
        this.childType = childType;
    }


    public boolean getIsSelect() {
        return this.isSelect;
    }


    public void setIsSelect(boolean isSelect) {
        this.isSelect = isSelect;
    }


    public boolean getIsShow() {
        return this.isShow;
    }


    public void setIsShow(boolean isShow) {
        this.isShow = isShow;
    }


    public String getPrivateKey() {
        return this.privateKey;
    }


    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }


    public int getChildChangeType() {
        return this.childChangeType;
    }


    public void setChildChangeType(int childChangeType) {
        this.childChangeType = childChangeType;
    }

    //childChangeType
    public static ChildAddressBean newInstance(String childAddress, long walletId, int number, int childType, boolean isSelect, String privateKey,
                                               int childChangeType) {
        ChildAddressBean childAddressBean = new ChildAddressBean();
        childAddressBean.setWalletId(walletId);
        childAddressBean.setChildAddress(childAddress);
        if (childChangeType == 1) {
            childAddressBean.setProtocol(((childType == Constants.CHILD_ADDRESS_NATIVE) ? ("m/84'/0'/0'" + "/1/") + number : ("m/49'/0'/0'" + "/1/")) + number);
        } else {
            childAddressBean.setProtocol(((childType == Constants.CHILD_ADDRESS_NATIVE) ? ("m/84'/0'/0'" + "/0/") + number : ("m/49'/0'/0'" + "/0/")) + number);
        }
        childAddressBean.setChildNumber(number);
        childAddressBean.setChildType(childType);
        childAddressBean.setIsSelect(number == 0 && isSelect);
        childAddressBean.setIsShow(number == 0);
        childAddressBean.setChildChangeType(childChangeType);
        childAddressBean.setPrivateKey(privateKey);
        return childAddressBean;
    }
}
