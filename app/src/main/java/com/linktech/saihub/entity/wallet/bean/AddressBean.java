package com.linktech.saihub.entity.wallet.bean;

public class AddressBean {

    public static final int TYPE_CREATE = 1;
    public static final int TYPE_IMPORT = 2;

    private String address;//地址(以太坊必须转小写)
    private String coin;//币种(大写)
    private int type;//类型 1创建 2导入(删除时可不传)

    public AddressBean( ) {
    }

    public AddressBean(String address, String coin, int type) {
        this.address = address;
        this.coin = coin;
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }


}
