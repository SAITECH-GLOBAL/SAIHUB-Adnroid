package com.linktech.saihub.entity.wallet.bean.transaction;

import static com.linktech.saihub.manager.RateAndLocalManager.RateKind.CNY;
import static com.linktech.saihub.manager.RateAndLocalManager.RateKind.RUB;
import static com.linktech.saihub.manager.RateAndLocalManager.RateKind.USD;

import android.text.TextUtils;

import com.linktech.saihub.app.SaiHubApplication;
import com.linktech.saihub.db.bean.TransferServerBean;
import com.linktech.saihub.db.bean.WalletBean;
import com.linktech.saihub.entity.wallet.bean.RateEntity;
import com.linktech.saihub.entity.wallet.bean.UnspentOutput;
import com.linktech.saihub.manager.CacheListManager;
import com.linktech.saihub.manager.RateAndLocalManager;
import com.linktech.saihub.util.NumberCountUtils;

import java.io.Serializable;
import java.util.List;


public class TransferSendBean implements Serializable {


    private static final long serialVersionUID = 1526645998817722076L;

    public TransferSendBean() {
    }


    public TransferSendBean(String moneyNumber, String tokenName, String toAddress, String address, String contractAddress, String gasPriceIntStr, String gasLimit) {
        this.moneyNumber = moneyNumber;
        this.tokenName = tokenName;
        this.toAddress = toAddress;
        this.address = address;
        this.contractAddress = contractAddress;
        this.gasPrice = gasPriceIntStr;
        this.gasLimit = gasLimit;
    }

    private String moneyNumber;
    private String tokenName;
    private String toAddress;
    private String address;
    private String contractAddress;
    private String gasLimit;
    private String password;
    private String convert;
    private String gas;
    private String gasConvert;

    public String getGas() {
        return gas;
    }

    public void setGas(String gas) {
        this.gas = gas;
    }

    public String getGasConvert() {
        return gasConvert;
    }

    public void setGasConvert(String gasConvert) {
        this.gasConvert = gasConvert;
    }

    public String getConvert() {
        return convert;
    }

    public void setConvert(String convert) {
        this.convert = convert;
    }

    //Btc 转账使用  uxto的list
    private int walletId;
    private int childAddressType;

    private String gasPrice;

    public int getWalletId() {
        return walletId;
    }

    public void setWalletId(int walletId) {
        this.walletId = walletId;
    }

    public int getChildAddressType() {
        return childAddressType;
    }

    public void setChildAddressType(int childAddressType) {
        this.childAddressType = childAddressType;
    }


    public String getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(String gasPrice) {
        this.gasPrice = gasPrice;
    }


    private List<UnspentOutput> utxoList;

    private List<UTXOMultiSigEntity> utxoHexList;

    public List<UTXOMultiSigEntity> getUtxoHexList() {
        return utxoHexList;
    }

    public void setUtxoHexList(List<UTXOMultiSigEntity> utxoHexList) {
        this.utxoHexList = utxoHexList;
    }

    public List<UnspentOutput> getUtxoList() {
        return utxoList;
    }

    public void setUtxoList(List<UnspentOutput> uxtoList) {
        this.utxoList = uxtoList;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMoneyNumber() {
        return TextUtils.isEmpty(moneyNumber) ? "0" : moneyNumber;
    }

    public void setMoneyNumber(String moneyNumber) {
        this.moneyNumber = moneyNumber;
    }

    public String getTokenName() {
        return tokenName == null ? "" : tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(String gasLimit) {
        this.gasLimit = gasLimit;
    }


    public static TransferSendBean newInstance(TransferServerBean serverBean, WalletBean walletBean) {
        TransferSendBean sendBean = new TransferSendBean();
        sendBean.setMoneyNumber(serverBean.getAmount());
        sendBean.setTokenName(serverBean.getCoin());
        sendBean.setToAddress(serverBean.getToAddress());
        sendBean.setAddress(serverBean.getFromAddress());
        return sendBean;

    }

    public void setGasSpend(int gasSpend) {
        this.gas = NumberCountUtils.getNumberScaleByPow(gasSpend + "", 8, 8);
        RateEntity rateEntity = CacheListManager.Companion.getInstance().getRateEntity();
        switch (RateAndLocalManager.getInstance(SaiHubApplication.Companion.getInstance()).getCurRateKind()) {
            case CNY:
                this.gasConvert = CNY.symbol + NumberCountUtils.getConvert(this.gas, rateEntity.getBtcCny());
                break;
            case USD:
                this.gasConvert = USD.symbol + NumberCountUtils.getConvert(this.gas, rateEntity.getBtcUsd());
                break;
            case RUB:
                this.gasConvert = RUB.symbol + NumberCountUtils.getConvert(this.gas, rateEntity.getBtcRub());
                break;

            default:
                break;
        }
    }
}
