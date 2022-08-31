package com.linktech.saihub.db.bean;


import android.os.Parcel;
import android.os.Parcelable;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

@Entity
public class WalletBean implements Parcelable {
    static final long serialVersionUID = 42L;
    @Id(autoincrement = true)
    private Long id;
    /**
     * 钱包地址 -  当BTC钱包多个地址时候  为选择的地址
     */
    public String address = "";
    private String name = "";
    private String password = "";
    private String mnemonic = "";
    private boolean isCurrent;
    private boolean isBackup;//是否备份
    private boolean isObserver; //是否是观察者
    private int addressType; //地址类型 0 Native SegWit 原生 bc1     1 Nested SegWit 兼容 3
    private String privateKey = "";
    private String publicKey = "";
    private String publicKeyExt = "";
    private String passphrase = "";
    private long createTime;
    private int childAddressType;

    private boolean isReportAddressToServer;//是否上报地址到服务器
    //btc上报服务器地址
    private String addressToServer;
    //是否开启快捷支付  默认false
    private boolean isOpenTouchIdPay = false;

    private String asset = "0.00";
    private String assetUSD = "0.00";
    private String assetRub = "0.00";

    @Transient
    public boolean isCheck = false;

    //存在类型 1：助记词创建或导入 2：私钥导入 3：拓展公钥导入 4：观察者地址导入 5：多签(待定)
    private int existType;

    //多签类型 ex：2 of 3
    private String policy = "";
    //多签原始数据
    private String multiSigData = "";

    //闪电网络钱包 新增属性
    //域名、login、password、认证用accessToken、刷新token时使用的refreshToken
    private String host = "";
    private String login = "";
    private String lnPassword = "";
    private String accessToken = "";
    private String refreshToken = "";
    private String lnBalance = "0";
    private String lnSat = "0";
    //ln是否开启密码支付  默认false
    private boolean isLNOpenPwdPay = false;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMnemonic() {
        return this.mnemonic;
    }

    public void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public boolean getIsCurrent() {
        return this.isCurrent;
    }

    public void setIsCurrent(boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public boolean getIsBackup() {
        return this.isBackup;
    }

    public void setIsBackup(boolean isBackup) {
        this.isBackup = isBackup;
    }

    public boolean getIsObserver() {
        return this.isObserver;
    }

    public void setIsObserver(boolean isObserver) {
        this.isObserver = isObserver;
    }

    public int getAddressType() {
        return this.addressType;
    }

    public void setAddressType(int addressType) {
        this.addressType = addressType;
    }

    public String getPrivateKey() {
        return this.privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return this.publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPassphrase() {
        return this.passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public long getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public int getChildAddressType() {
        return this.childAddressType;
    }

    public void setChildAddressType(int childAddressType) {
        this.childAddressType = childAddressType;
    }

    public boolean getIsReportAddressToServer() {
        return this.isReportAddressToServer;
    }

    public void setIsReportAddressToServer(boolean isReportAddressToServer) {
        this.isReportAddressToServer = isReportAddressToServer;
    }

    public String getAddressToServer() {
        return this.addressToServer;
    }

    public void setAddressToServer(String addressToServer) {
        this.addressToServer = addressToServer;
    }

    public boolean getIsOpenTouchIdPay() {
        return this.isOpenTouchIdPay;
    }

    public void setIsOpenTouchIdPay(boolean isOpenTouchIdPay) {
        this.isOpenTouchIdPay = isOpenTouchIdPay;
    }

    public String getAsset() {
        return this.asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public String getAssetUSD() {
        return this.assetUSD;
    }

    public void setAssetUSD(String assetUSD) {
        this.assetUSD = assetUSD;
    }

    public String getAssetRub() {
        return this.assetRub;
    }

    public void setAssetRub(String assetRub) {
        this.assetRub = assetRub;
    }

    public int getExistType() {
        return this.existType;
    }

    public void setExistType(int existType) {
        this.existType = existType;
    }

    public String getPolicy() {
        return this.policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public String getMultiSigData() {
        return this.multiSigData;
    }

    public void setMultiSigData(String multiSigData) {
        this.multiSigData = multiSigData;
    }

    public WalletBean() {
    }

    @Generated(hash = 83810226)
    public WalletBean(Long id, String address, String name, String password, String mnemonic, boolean isCurrent, boolean isBackup,
            boolean isObserver, int addressType, String privateKey, String publicKey, String publicKeyExt, String passphrase,
            long createTime, int childAddressType, boolean isReportAddressToServer, String addressToServer, boolean isOpenTouchIdPay,
            String asset, String assetUSD, String assetRub, int existType, String policy, String multiSigData, String host,
            String login, String lnPassword, String accessToken, String refreshToken, String lnBalance, String lnSat,
            boolean isLNOpenPwdPay) {
        this.id = id;
        this.address = address;
        this.name = name;
        this.password = password;
        this.mnemonic = mnemonic;
        this.isCurrent = isCurrent;
        this.isBackup = isBackup;
        this.isObserver = isObserver;
        this.addressType = addressType;
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.publicKeyExt = publicKeyExt;
        this.passphrase = passphrase;
        this.createTime = createTime;
        this.childAddressType = childAddressType;
        this.isReportAddressToServer = isReportAddressToServer;
        this.addressToServer = addressToServer;
        this.isOpenTouchIdPay = isOpenTouchIdPay;
        this.asset = asset;
        this.assetUSD = assetUSD;
        this.assetRub = assetRub;
        this.existType = existType;
        this.policy = policy;
        this.multiSigData = multiSigData;
        this.host = host;
        this.login = login;
        this.lnPassword = lnPassword;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.lnBalance = lnBalance;
        this.lnSat = lnSat;
        this.isLNOpenPwdPay = isLNOpenPwdPay;
    }

    public String getPublicKeyExt() {
        return this.publicKeyExt;
    }

    public void setPublicKeyExt(String publicKeyExt) {
        this.publicKeyExt = publicKeyExt;
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getLogin() {
        return this.login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLnPassword() {
        return this.lnPassword;
    }

    public void setLnPassword(String lnPassword) {
        this.lnPassword = lnPassword;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return this.refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getLnBalance() {
        return this.lnBalance;
    }

    public void setLnBalance(String lnBalance) {
        this.lnBalance = lnBalance;
    }

    public String getLnSat() {
        return this.lnSat;
    }

    public void setLnSat(String lnSat) {
        this.lnSat = lnSat;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.address);
        dest.writeString(this.name);
        dest.writeString(this.password);
        dest.writeString(this.mnemonic);
        dest.writeByte(this.isCurrent ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isBackup ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isObserver ? (byte) 1 : (byte) 0);
        dest.writeInt(this.addressType);
        dest.writeString(this.privateKey);
        dest.writeString(this.publicKey);
        dest.writeString(this.publicKeyExt);
        dest.writeString(this.passphrase);
        dest.writeLong(this.createTime);
        dest.writeInt(this.childAddressType);
        dest.writeByte(this.isReportAddressToServer ? (byte) 1 : (byte) 0);
        dest.writeString(this.addressToServer);
        dest.writeByte(this.isOpenTouchIdPay ? (byte) 1 : (byte) 0);
        dest.writeString(this.asset);
        dest.writeString(this.assetUSD);
        dest.writeString(this.assetRub);
        dest.writeByte(this.isCheck ? (byte) 1 : (byte) 0);
        dest.writeInt(this.existType);
        dest.writeString(this.policy);
        dest.writeString(this.multiSigData);
        dest.writeString(this.host);
        dest.writeString(this.login);
        dest.writeString(this.lnPassword);
        dest.writeString(this.accessToken);
        dest.writeString(this.refreshToken);
        dest.writeString(this.lnBalance);
        dest.writeString(this.lnSat);
        dest.writeByte(this.isLNOpenPwdPay ? (byte) 1 : (byte) 0);
    }

    public void readFromParcel(Parcel source) {
        this.id = (Long) source.readValue(Long.class.getClassLoader());
        this.address = source.readString();
        this.name = source.readString();
        this.password = source.readString();
        this.mnemonic = source.readString();
        this.isCurrent = source.readByte() != 0;
        this.isBackup = source.readByte() != 0;
        this.isObserver = source.readByte() != 0;
        this.addressType = source.readInt();
        this.privateKey = source.readString();
        this.publicKey = source.readString();
        this.publicKeyExt = source.readString();
        this.passphrase = source.readString();
        this.createTime = source.readLong();
        this.childAddressType = source.readInt();
        this.isReportAddressToServer = source.readByte() != 0;
        this.addressToServer = source.readString();
        this.isOpenTouchIdPay = source.readByte() != 0;
        this.asset = source.readString();
        this.assetUSD = source.readString();
        this.assetRub = source.readString();
        this.isCheck = source.readByte() != 0;
        this.existType = source.readInt();
        this.policy = source.readString();
        this.multiSigData = source.readString();
        this.host = source.readString();
        this.login = source.readString();
        this.lnPassword = source.readString();
        this.accessToken = source.readString();
        this.refreshToken = source.readString();
        this.lnBalance = source.readString();
        this.lnSat = source.readString();
        this.isLNOpenPwdPay = source.readByte() != 0;
    }

    public boolean getIsLNOpenPwdPay() {
        return this.isLNOpenPwdPay;
    }

    public void setIsLNOpenPwdPay(boolean isLNOpenPwdPay) {
        this.isLNOpenPwdPay = isLNOpenPwdPay;
    }

    protected WalletBean(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.address = in.readString();
        this.name = in.readString();
        this.password = in.readString();
        this.mnemonic = in.readString();
        this.isCurrent = in.readByte() != 0;
        this.isBackup = in.readByte() != 0;
        this.isObserver = in.readByte() != 0;
        this.addressType = in.readInt();
        this.privateKey = in.readString();
        this.publicKey = in.readString();
        this.publicKeyExt = in.readString();
        this.passphrase = in.readString();
        this.createTime = in.readLong();
        this.childAddressType = in.readInt();
        this.isReportAddressToServer = in.readByte() != 0;
        this.addressToServer = in.readString();
        this.isOpenTouchIdPay = in.readByte() != 0;
        this.asset = in.readString();
        this.assetUSD = in.readString();
        this.assetRub = in.readString();
        this.isCheck = in.readByte() != 0;
        this.existType = in.readInt();
        this.policy = in.readString();
        this.multiSigData = in.readString();
        this.host = in.readString();
        this.login = in.readString();
        this.lnPassword = in.readString();
        this.accessToken = in.readString();
        this.refreshToken = in.readString();
        this.lnBalance = in.readString();
        this.lnSat = in.readString();
        this.isLNOpenPwdPay = in.readByte() != 0;
    }

    public static final Creator<WalletBean> CREATOR = new Creator<WalletBean>() {
        @Override
        public WalletBean createFromParcel(Parcel source) {
            return new WalletBean(source);
        }

        @Override
        public WalletBean[] newArray(int size) {
            return new WalletBean[size];
        }
    };
}
