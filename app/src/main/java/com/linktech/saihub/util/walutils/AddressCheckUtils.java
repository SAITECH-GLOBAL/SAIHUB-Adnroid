package com.linktech.saihub.util.walutils;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;

public class AddressCheckUtils {


    public static boolean isBTCValidAddress(String input) {
        try {
            NetworkParameters networkParameters = null;
            networkParameters = MainNetParams.get();
            Address address = Address.fromString(networkParameters, input);
            if (address != null) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean checkAddress(int walletType, String address) {
        return isBTCValidAddress(address);
    }
}
