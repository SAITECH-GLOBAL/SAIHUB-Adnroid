package com.linktech.saihub.manager.wallet.btc;

import org.bitcoinj.params.AbstractBitcoinNetParams;
import org.bitcoinj.params.MainNetParams;

public class ParamsManagerBtc {

    public static AbstractBitcoinNetParams getParams() {
        MainNetParams params = MainNetParams.get();
//        TestNet3Params params = TestNet3Params.get();
        return params;
    }
}

