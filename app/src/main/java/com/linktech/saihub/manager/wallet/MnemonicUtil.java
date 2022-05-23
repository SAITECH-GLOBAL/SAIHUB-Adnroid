package com.linktech.saihub.manager.wallet;


import com.linktech.saihub.manager.wallet.exception.Messages;
import com.linktech.saihub.manager.wallet.exception.TokenException;

import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.wallet.DeterministicSeed;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MnemonicUtil {

    public static List<String> createMnemonic(String mnemonicCodes) {
        String[] words = mnemonicCodes.split("\\s+");
        return Arrays.asList(words);
    }

    public static boolean checkMnemonic(String mnemonicCodes) {
        String[] words = mnemonicCodes.split("\\s+");
        List list = java.util.Arrays.asList(words);
        return checkMnemonics(list);
    }

    public static boolean checkMnemonics(List<String> mnemonicCodes) {
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

    public static void validateMnemonics(String mnemonicCodes) {
        String[] words = mnemonicCodes.split("\\s+");
        List list = java.util.Arrays.asList(words);
        validateMnemonics(list);
    }

    public static void validateMnemonics(List<String> mnemonicCodes) {
        try {
            MnemonicCode.INSTANCE.check(mnemonicCodes);
        } catch (MnemonicException.MnemonicLengthException e) {
            throw new TokenException(Messages.MNEMONIC_INVALID_LENGTH);
        } catch (MnemonicException.MnemonicWordException e) {
            throw new TokenException(Messages.MNEMONIC_BAD_WORD);
        } catch (Exception e) {
            throw new TokenException(Messages.MNEMONIC_CHECKSUM);
        }
    }

    public static List<String> createMnemonic(int type) throws MnemonicException.MnemonicLengthException {
        SecureRandom secureRandom = new SecureRandom();
        byte[] entropy;
        if (type == 0) {
            entropy = new byte[DeterministicSeed.DEFAULT_SEED_ENTROPY_BITS / 8];
        } else {
            entropy = new byte[DeterministicSeed.DEFAULT_SEED_ENTROPY_BITS / 4];
        }
        secureRandom.nextBytes(entropy);
        //生成12位 24位助记词
        return MnemonicCode.INSTANCE.toMnemonic(entropy);
    }

}
