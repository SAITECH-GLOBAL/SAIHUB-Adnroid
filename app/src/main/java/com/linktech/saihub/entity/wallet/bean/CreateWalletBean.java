package com.linktech.saihub.entity.wallet.bean;

public class CreateWalletBean {

    public CreateWalletBean(String currencyName, int iconRes, boolean select, int currentcyType) {
        this.currencyName = currencyName;
        this.iconRes = iconRes;
        this.select = select;
        this.currentcyType = currentcyType;
    }

    /**
     * 币种名称
     */
    private String currencyName;

    /**
     * 币种logo
     */
    private int iconRes;

    /**
     * 是否选中
     */
    private  boolean select;

    /**
     * 钱包类型 0BTC 1ETH 2EOS 3TRX 4DOT
     */
    private  int currentcyType;

    public int getCurrentcyType() {
        return currentcyType;
    }

    public void setCurrentcyType(int currentcyType) {
        this.currentcyType = currentcyType;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public void setCurrencyName(String currencyName) {
        this.currencyName = currencyName;
    }

    public int getIconRes() {
        return iconRes;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }
}
