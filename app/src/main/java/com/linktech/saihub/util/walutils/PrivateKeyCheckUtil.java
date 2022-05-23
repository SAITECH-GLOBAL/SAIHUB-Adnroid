package com.linktech.saihub.util.walutils;

import com.linktech.saihub.manager.wallet.btc.ParamsManagerBtc;

import org.bitcoinj.core.AddressFormatException;
import org.bitcoinj.core.DumpedPrivateKey;

public class PrivateKeyCheckUtil {


    public static boolean checkPrivateKey(int walletType, String privateKeyStr) {
        return checkBTCPrivateKey(privateKeyStr);
    }

/*
    public static boolean validatePrivateKey(String privateKey) {
        byte[] decode = Base58.decode(privateKey);
        //判断私钥格式
        byte[] check = new byte[4];
        System.arraycopy(decode, decode.length - 4, check, 0, check.length);
        byte[] privateCheck = SHA256.doubleSha256(decode, 0, decode.length - 4);
        for (int i = 0; i < 4; i++) {
            if (check[i] != privateCheck[i]) {
//                throw new ValidationException("WIF Private Key Incorrect format");
                return false;
            }
        }
        return true;
    }
*/

    public static boolean checkBTCPrivateKey(String privateKey) {
        try {
            DumpedPrivateKey.fromBase58(ParamsManagerBtc.getParams(), privateKey);
        } catch (AddressFormatException.WrongNetwork addressException) {
//            throw new TokenException(Messages.WIF_WRONG_NETWORK);
            return false;
        } catch (AddressFormatException addressFormatException) {
//            throw new TokenException(Messages.WIF_INVALID);
            return false;
        }
        if (!DumpedPrivateKey.fromBase58(ParamsManagerBtc.getParams(), privateKey).getKey().isCompressed()) {
//            throw new TokenException(Messages.SEGWIT_NEEDS_COMPRESS_PUBLIC_KEY);
            return false;
        }
        return true;
    }
}

