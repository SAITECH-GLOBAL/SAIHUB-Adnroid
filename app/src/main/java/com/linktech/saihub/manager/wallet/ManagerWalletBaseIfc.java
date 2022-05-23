package com.linktech.saihub.manager.wallet;

import com.linktech.saihub.db.bean.WalletBean;

import org.bitcoinj.wallet.UnreadableWalletException;

import java.util.List;

public interface ManagerWalletBaseIfc {

    void coverWallet(WalletBean walletBean);

    WalletBean importMnemonic(String walletName, List<String> list, String pwd, int type, String passphrase) throws UnreadableWalletException;

    WalletBean createWalletForMnemonic(String name, String pwd, List<String> mnemonic, int type, String passphrase) throws UnreadableWalletException;

    WalletBean loadWalletByKeystore(String walletName, String keystore, String pwd);


    WalletBean loadWalletByPrivateKey(WalletBean walletIntent, String privateKey, String pwd);

    boolean deleteFile(String fileName);

    String deriveKeystore(long walletId);

    String derivePrivateKey(long walletId, String pwd);


    String getPrivateForMnemonic(List<String> mnemonic);

}
