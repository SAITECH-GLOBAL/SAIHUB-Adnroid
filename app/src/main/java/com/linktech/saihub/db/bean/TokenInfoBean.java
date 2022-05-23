package com.linktech.saihub.db.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;

/**
 * token本地  --关联钱包
 */
@Entity
public class TokenInfoBean implements Serializable {
    static final long serialVersionUID = 42L;

    /**
     * id : 8
     * tokenShort : Super87Coin
     * tokenFull : Super87Coin
     * tokenType : 0
     * logo : "null"
     * places : 8
     * contractAddr : 0xcc5f733b0eb6d6e05a7c41cb827ec6e5fb3925ae
     * weights : 3
     * status : 1
     * createTimestamp : 1598889600000
     */

    @Id(autoincrement = true)
    private Long mId;

    private long id;
    private long walId;
    private long childAddressType;

    private String contractAddr;
    private String address;
    private String tokenShort;
    private String tokenFull;
    private int tokenType;
    private String logo;
    private int places;
    private int weights;
    private int status;
    private long createTimestamp;
    private boolean select;

    private String tokenBalance = "0";
    /**
     * 汇率
     */
    private String currency = "0.00";

    /**
     * 汇率
     */
    private String currencyUsd = "0.00";

    /**
     * 汇率
     */
    private String currencyRub = "0.00";

    private int tokenWalletType;

    @Generated(hash = 1293592244)
    public TokenInfoBean(Long mId, long id, long walId, long childAddressType,
            String contractAddr, String address, String tokenShort,
            String tokenFull, int tokenType, String logo, int places, int weights,
            int status, long createTimestamp, boolean select, String tokenBalance,
            String currency, String currencyUsd, String currencyRub,
            int tokenWalletType) {
        this.mId = mId;
        this.id = id;
        this.walId = walId;
        this.childAddressType = childAddressType;
        this.contractAddr = contractAddr;
        this.address = address;
        this.tokenShort = tokenShort;
        this.tokenFull = tokenFull;
        this.tokenType = tokenType;
        this.logo = logo;
        this.places = places;
        this.weights = weights;
        this.status = status;
        this.createTimestamp = createTimestamp;
        this.select = select;
        this.tokenBalance = tokenBalance;
        this.currency = currency;
        this.currencyUsd = currencyUsd;
        this.currencyRub = currencyRub;
        this.tokenWalletType = tokenWalletType;
    }

    @Generated(hash = 2002404654)
    public TokenInfoBean() {
    }

    public Long getMId() {
        return this.mId;
    }

    public void setMId(Long mId) {
        this.mId = mId;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getWalId() {
        return this.walId;
    }

    public void setWalId(long walId) {
        this.walId = walId;
    }

    public long getChildAddressType() {
        return this.childAddressType;
    }

    public void setChildAddressType(long childAddressType) {
        this.childAddressType = childAddressType;
    }

    public String getContractAddr() {
        return this.contractAddr;
    }

    public void setContractAddr(String contractAddr) {
        this.contractAddr = contractAddr;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTokenShort() {
        return this.tokenShort;
    }

    public void setTokenShort(String tokenShort) {
        this.tokenShort = tokenShort;
    }

    public String getTokenFull() {
        return this.tokenFull;
    }

    public void setTokenFull(String tokenFull) {
        this.tokenFull = tokenFull;
    }

    public int getTokenType() {
        return this.tokenType;
    }

    public void setTokenType(int tokenType) {
        this.tokenType = tokenType;
    }

    public String getLogo() {
        return this.logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public int getPlaces() {
        return this.places;
    }

    public void setPlaces(int places) {
        this.places = places;
    }

    public int getWeights() {
        return this.weights;
    }

    public void setWeights(int weights) {
        this.weights = weights;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getCreateTimestamp() {
        return this.createTimestamp;
    }

    public void setCreateTimestamp(long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public boolean getSelect() {
        return this.select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public String getTokenBalance() {
        return this.tokenBalance;
    }

    public void setTokenBalance(String tokenBalance) {
        this.tokenBalance = tokenBalance;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrencyUsd() {
        return this.currencyUsd;
    }

    public void setCurrencyUsd(String currencyUsd) {
        this.currencyUsd = currencyUsd;
    }

    public String getCurrencyRub() {
        return this.currencyRub;
    }

    public void setCurrencyRub(String currencyRub) {
        this.currencyRub = currencyRub;
    }

    public int getTokenWalletType() {
        return this.tokenWalletType;
    }

    public void setTokenWalletType(int tokenWalletType) {
        this.tokenWalletType = tokenWalletType;
    }


}
