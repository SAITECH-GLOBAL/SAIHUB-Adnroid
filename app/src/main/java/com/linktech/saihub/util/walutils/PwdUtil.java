package com.linktech.saihub.util.walutils;


import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;

import java.util.Arrays;
import java.util.List;

public class PwdUtil {

    public static boolean checkMnemonic(String mnemonicStr) {
        String[] words = mnemonicStr.split("\\s+");
        if (words.length != 12) {
            return false;
        }
        List list = Arrays.asList(words);
        return validateMnemonics(list);
    }

    /**
     * 检查是否是助记词
     *
     * @param mnemonicCodes
     */
    public static boolean validateMnemonics(List<String> mnemonicCodes) {
        try {
            MnemonicCode.INSTANCE.check(mnemonicCodes);
        } catch (MnemonicException.MnemonicLengthException e) {
            return false;
        } catch (MnemonicException.MnemonicWordException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

}
