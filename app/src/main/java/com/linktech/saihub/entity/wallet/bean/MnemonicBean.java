package com.linktech.saihub.entity.wallet.bean;

public class MnemonicBean {

    public MnemonicBean( ) {
    }

    public MnemonicBean(int index, String mnemonics) {
        this.index = index;
        this.mnemonics = mnemonics;
    }

    private int index;

    private String mnemonics;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getMnemonics() {
        return mnemonics;
    }

    public void setMnemonics(String mnemonics) {
        this.mnemonics = mnemonics;
    }
}
