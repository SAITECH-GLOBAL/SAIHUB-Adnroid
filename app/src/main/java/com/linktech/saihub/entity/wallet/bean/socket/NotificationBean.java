package com.linktech.saihub.entity.wallet.bean.socket;

import android.os.Parcel;
import android.os.Parcelable;

import com.linktech.saihub.db.bean.TokenInfoBean;


public class NotificationBean implements Parcelable {
    String chainCoin;
    String chainAddress;
    TokenInfoBean tokenInfoData;
    public NotificationBean() {
    }


    public String getChainCoin() {
        return chainCoin;
    }

    public void setChainCoin(String chainCoin) {
        this.chainCoin = chainCoin;
    }

    public String getChainAddress() {
        return chainAddress;
    }

    public void setChainAddress(String chainAddress) {
        this.chainAddress = chainAddress;
    }

    public TokenInfoBean getTokenInfoData() {
        return tokenInfoData;
    }

    public void setTokenInfoData(TokenInfoBean tokenInfoData) {
        this.tokenInfoData = tokenInfoData;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.chainCoin);
        dest.writeString(this.chainAddress);
        dest.writeSerializable(this.tokenInfoData);
    }

    public void readFromParcel(Parcel source) {
        this.chainCoin = source.readString();
        this.chainAddress = source.readString();
        this.tokenInfoData = (TokenInfoBean) source.readSerializable();
    }

    protected NotificationBean(Parcel in) {
        this.chainCoin = in.readString();
        this.chainAddress = in.readString();
        this.tokenInfoData = (TokenInfoBean) in.readSerializable();
    }

    public static final Parcelable.Creator<NotificationBean> CREATOR = new Parcelable.Creator<NotificationBean>() {
        @Override
        public NotificationBean createFromParcel(Parcel source) {
            return new NotificationBean(source);
        }

        @Override
        public NotificationBean[] newArray(int size) {
            return new NotificationBean[size];
        }
    };
}
