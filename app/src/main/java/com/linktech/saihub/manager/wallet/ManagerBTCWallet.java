package com.linktech.saihub.manager.wallet;


import static com.linktech.saihub.app.Constants.CHILD_ADDRESS_SIZE;

import android.text.TextUtils;

import com.google.common.collect.ImmutableList;
import com.linktech.saihub.R;
import com.linktech.saihub.app.Constants;
import com.linktech.saihub.app.SaiHubApplication;
import com.linktech.saihub.app.StringConstants;
import com.linktech.saihub.db.bean.ChildAddressBean;
import com.linktech.saihub.db.bean.WalletBean;
import com.linktech.saihub.db.utils.ChildAddressDaoUtil;
import com.linktech.saihub.db.utils.TokenDaoUtil;
import com.linktech.saihub.db.utils.WalletDaoUtils;
import com.linktech.saihub.greendao.WalletBeanDao;
import com.linktech.saihub.manager.AES;
import com.linktech.saihub.manager.wallet.btc.ParamsManagerBtc;
import com.linktech.saihub.util.CommonUtil;
import com.linktech.saihub.util.LogUtils;
import com.linktech.saihub.util.Pub;
import com.linktech.saihub.util.system.CommonStringKt;
import com.linktech.saihub.util.walutils.NumericUtil;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.LegacyAddress;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.SegwitAddress;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.AbstractBitcoinNetParams;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * 以太坊钱包创建工具类
 */

public class ManagerBTCWallet implements ManagerWalletBaseIfc {

    private static volatile ManagerBTCWallet wallet;

    public static ManagerBTCWallet getInstance() {
        if (wallet != null) {
            return wallet;
        }
        synchronized (ManagerBTCWallet.class) {
            if (wallet != null) {
                return wallet;
            }
            wallet = new ManagerBTCWallet();
        }
        return wallet;
    }

    private ManagerBTCWallet() {
    }


    /**
     * 创建钱包  通过助记词模式
     *
     * @param name
     * @param pwd
     * @return
     * @throws UnreadableWalletException
     */
    @Override
    public WalletBean createWalletForMnemonic(String name, String pwd, List<String> mnemonic, int addressType, String passphrase) throws UnreadableWalletException {
        return generateMnemonic(name, mnemonic, pwd, true, addressType, passphrase);
    }


    /**
     * 判断助记词是否重复 (导入的时候)
     */
    public WalletBean generateMnemonic(String name, List<String> mnemonic, String pwd, boolean isCreateWallet, int addressType, String passphrase) throws UnreadableWalletException {
        MnemonicUtil.validateMnemonics(mnemonic);
        NetworkParameters params = ParamsManagerBtc.getParams();
        String seedCode = convertMnemonicList(mnemonic);
        long createTime = System.currentTimeMillis() / 1000;

        DeterministicSeed seed = new DeterministicSeed(seedCode, null, passphrase, createTime);
//        Wallet wallet = Wallet.fromSeed(params, seed);
        DeterministicKeyChain keyChain = DeterministicKeyChain.builder().seed(seed).build();
        String addressSelect;
        if (addressType == Constants.CHILD_ADDRESS_NATIVE) {
            //隔离见证原生地址
            DeterministicKey keySegWit84 = keyChain.getKeyByPath(generatePath("m/84'/0'/0'/0/" + 0), true);

            SegwitAddress segwitAddress = SegwitAddress.fromKey(params, keySegWit84);
            addressSelect = segwitAddress.toBech32();
        } else {
            //隔离见证兼容地址
            DeterministicKey keySegWit = keyChain.getKeyByPath(generatePath("m/49'/0'/0'" + "/0/" + 0), true);
            //隔离兼容地址
            addressSelect = getSegWitNestedAddress(params, keySegWit);
        }

        WalletBean btcWallet = new WalletBean();
        btcWallet.setName(name);
        btcWallet.setMnemonic(AES.encrypt(seedCode));
        btcWallet.setAddress(addressSelect);
        btcWallet.setAddressType(addressType);
        btcWallet.setChildAddressType(addressType);
        btcWallet.setPassword(AES.encrypt(pwd));
        btcWallet.setIsOpenTouchIdPay(false);
        btcWallet.setIsObserver(false);
        btcWallet.setExistType(Constants.EXIST_MNEMONIC);
        btcWallet.setIsBackup(!isCreateWallet);
        if (!TextUtils.isEmpty(passphrase)) {
            btcWallet.setPassphrase(AES.encrypt(passphrase));
        }
        btcWallet.setCreateTime(System.currentTimeMillis());
        LogUtils.e(StringConstants.SAIHUB_TAG, btcWallet.toString());
        return btcWallet;
    }

    public ImmutableList<ChildNumber> generatePath(String path) {
        List<ChildNumber> list = new ArrayList<>();
        for (String p : path.split("/")) {
            if ("m".equalsIgnoreCase(p) || "".equals(p.trim())) {
                continue;
            } else if (p.charAt(p.length() - 1) == '\'') {
                list.add(new ChildNumber(Pub.GetInt(p.substring(0, p.length() - 1)), true));
            } else {
                list.add(new ChildNumber(Pub.GetInt(p), false));
            }
        }

        ImmutableList.Builder<ChildNumber> builder = ImmutableList.builder();
        return builder.addAll(list).build();
    }

    /**
     * 导入助记词
     *
     * @param walletName
     * @param mnemonic
     * @param pwd
     * @return
     * @throws UnreadableWalletException
     */
    @Override
    public WalletBean importMnemonic(String walletName, List<String> mnemonic, String pwd, int type, String passphrase) throws UnreadableWalletException {
        return generateMnemonic(walletName, mnemonic, pwd, false, type, passphrase);
    }

    /**
     * BTC中没有使用
     */
    @Override
    public WalletBean loadWalletByKeystore(String walletName, String keystore, String pwd) {
        return null;
    }


    /**
     * 导入私钥
     *
     * @param walletIntent
     * @param privateKey
     * @param pwd
     * @return
     */
    @Override
    public WalletBean loadWalletByPrivateKey(WalletBean walletIntent, String privateKey, String pwd) {
        NetworkParameters params = ParamsManagerBtc.getParams();

        ECKey key = getECKeyForPrivateKey(params, privateKey);

        List<ECKey> objects = new ArrayList<>();
        objects.add(key);

        String address;
        if (walletIntent.getAddressType() == Constants.CHILD_ADDRESS_NATIVE) {
            address = getSegWitAddress(params, key);
        } else {
            address = getSegWitNestedAddress(params, key);
        }

        WalletBean btcWallet = new WalletBean();
        btcWallet.setName(walletIntent.getName());
        btcWallet.setAddress(address);
        btcWallet.setAddressType(walletIntent.getAddressType());
        btcWallet.setPassword(AES.encrypt(pwd));
        btcWallet.setIsBackup(true);
        btcWallet.setIsOpenTouchIdPay(false);
        btcWallet.setIsObserver(false);
        btcWallet.setExistType(Constants.EXIST_PRIVATE_KEY);
        btcWallet.setChildAddressType(walletIntent.getAddressType());
        btcWallet.setPrivateKey(AES.encrypt(privateKey));
        btcWallet.setAddressToServer(address);
        btcWallet.setCreateTime(System.currentTimeMillis());
        return btcWallet;
    }

    /**
     * 公钥导入钱包 创建WalletBean对象
     *
     * @param walletIntent
     * @param extKey
     * @return
     */
    public WalletBean importPublicKey(WalletBean walletIntent, String extKey) {
        if (extKey.startsWith("ypub") || extKey.startsWith("Zpub") || extKey.startsWith("Ypub")) {
            extKey = CommonUtil.ypubToXpub(CommonUtil.ypubToXpub(extKey));
        }
        NetworkParameters params = ParamsManagerBtc.getParams();
        DeterministicKey parentDK = DeterministicKey.deserializeB58(extKey, params);
        DeterministicKey changeKey = HDKeyDerivation.deriveChildKey(parentDK, 0);
        DeterministicKey childDK = HDKeyDerivation.deriveChildKey(changeKey, 0);
        LogUtils.e("n -address--" + getAddress(params, childDK));
        LogUtils.e("na -address--" + getSegWitAddress(params, childDK));
        LogUtils.e("ne -address--" + getSegWitNestedAddress(params, childDK));
        String address;
        if (walletIntent.getAddressType() == Constants.CHILD_ADDRESS_NATIVE) {
            address = getSegWitAddress(params, childDK);
        } else {
            address = getSegWitNestedAddress(params, childDK);
        }
        WalletBean btcWallet = new WalletBean();
        btcWallet.setName(walletIntent.getName());
        btcWallet.setAddress(address);
        btcWallet.setAddressType(walletIntent.getAddressType());
        btcWallet.setIsBackup(true);
        btcWallet.setPublicKeyExt(walletIntent.getPublicKeyExt());
        btcWallet.setIsObserver(true);
        btcWallet.setExistType(Constants.EXIST_PUBLIC_KEY);
        btcWallet.setPublicKey(extKey);
        btcWallet.setChildAddressType(walletIntent.getAddressType());
        btcWallet.setAddressToServer(address);
        btcWallet.setCreateTime(System.currentTimeMillis());
        return btcWallet;
    }

    /**
     * 导入多签钱包 创建WalletBean对象
     *
     * @param walletIntent
     * @param content
     * @param output
     * @return
     */
    public WalletBean importMultiSig(WalletBean walletIntent, String content, List<String> normalList, List<? extends Object> output) {
        try {
            WalletBean btcWallet = new WalletBean();
            btcWallet.setName(walletIntent.getName());
            btcWallet.setMultiSigData(content);
            btcWallet.setAddress(normalList.get(0));
            btcWallet.setAddressType(CommonStringKt.getTypeForAddress(normalList.get(0)));
            btcWallet.setIsBackup(true);
            btcWallet.setIsObserver(true);
            btcWallet.setExistType(Constants.EXIST_MULTI_SIG);
            btcWallet.setPublicKey((String) output.get(2));
            btcWallet.setPolicy((String) output.get(0));
            btcWallet.setChildAddressType(walletIntent.getAddressType());
            btcWallet.setAddressToServer(normalList.get(0));
            btcWallet.setCreateTime(System.currentTimeMillis());
            return btcWallet;
        } catch (Exception e) {
            throw new IllegalArgumentException(SaiHubApplication.Companion.getInstance().getString(R.string.wallet_import_error));
        }
    }

    /**
     * 解析多签导入内容
     *
     * @param content
     */
    public List<Object> parseMultiSigInput(String content) {
        try {
            List<Object> output = new ArrayList<>();
            String[] temp0 = content.split("Policy: ");
            String[] temp1 = content.split("Derivation: ");
            int i = temp0[1].indexOf("\n");
            int k = temp1[1].indexOf("\n");
            String policy = temp0[1].substring(0, i);
            String[] policyArray = policy.split(" ");
            String derivation = temp1[1].substring(0, k);
            output.add(policyArray[0] + "," + policyArray[2]);

            String[] keyTempList = temp0[1].replace(" ", "").split("\n\n");
            StringBuilder builder = new StringBuilder();
            List<List<String>> zpubs = new ArrayList<>();
            if (keyTempList.length == 2) {
                String[] split = keyTempList[1].split("\n");
                for (int j = 0; j < split.length; j++) {
                    String[] zpubSplit = split[j].split(":");
                    List<String> zpubFinger = new ArrayList<>();
                    zpubFinger.add(zpubSplit[1]);
                    zpubFinger.add(zpubSplit[0]);
                    zpubs.add(zpubFinger);
                    if (j == 0) {
                        builder.append(zpubSplit[1]);
                    } else {
                        builder.append(",").append(zpubSplit[1]);
                    }
                }
            } else {
                for (int j = 1; j < keyTempList.length; j++) {
                    String[] split = keyTempList[j].split(":");
                    List<String> zpubFinger = new ArrayList<>();
                    zpubFinger.add(split[1]);
                    zpubFinger.add(split[0]);
                    zpubs.add(zpubFinger);
                    if (j == 1) {
                        builder.append(split[1]);
                    } else {
                        builder.append(",").append(split[1]);
                    }
                }
            }

            output.add(zpubs);
            output.add(builder.toString());
            return output;
        } catch (Exception e) {
            throw new IllegalArgumentException(SaiHubApplication.Companion.getInstance().getString(R.string.wallet_import_error));
        }
    }

    /**
     * 地址导入观察者钱包
     *
     * @param walletIntent
     * @param address
     * @return
     */
    public WalletBean importAddressObserver(WalletBean walletIntent, String address) {
        WalletBean btcWallet = new WalletBean();
        btcWallet.setName(walletIntent.getName());
        btcWallet.setAddress(address);
        btcWallet.setAddressType(walletIntent.getAddressType());
        btcWallet.setIsBackup(true);
        btcWallet.setExistType(Constants.EXIST_ADDRESS);
        btcWallet.setIsObserver(true);
        btcWallet.setChildAddressType(walletIntent.getAddressType());
        btcWallet.setAddressToServer(address);
        btcWallet.setCreateTime(System.currentTimeMillis());
        return btcWallet;
    }

    @Override
    public boolean deleteFile(String fileName) {
        return true;
    }

    @Override
    public String deriveKeystore(long walletId) {
        return null;
    }

    @Override
    public String derivePrivateKey(long walletId, String pwd) {
        return null;
    }


    /**
     * 获得普通地址
     *
     * @param param
     * @param key
     * @return
     */
    public String getAddress(NetworkParameters param, ECKey key) {
        return LegacyAddress.fromKey(param, key).toString();
    }

    //获取隔离见证 原生地址
    public String getSegWitAddress(NetworkParameters param, ECKey key) {
        return Address.fromKey(param, key, Script.ScriptType.P2WPKH).toString();
    }

    //获取隔离见证 兼容地址
    public String getSegWitNestedAddress(NetworkParameters param, ECKey key) {
        String redeemScript = String.format("0x0014%s", NumericUtil.bytesToHex(key.getPubKeyHash()));
        return LegacyAddress.fromScriptHash(param, Utils.sha256hash160(NumericUtil.hexToBytes(redeemScript))).toString();
    }

    /**
     * 助记词转换为String
     *
     * @param mnemonics
     * @return
     */
    private String convertMnemonicList(List<String> mnemonics) {
        StringBuilder sb = new StringBuilder();
        int size = mnemonics.size();
        for (int i = 0; i < size; i++) {
            sb.append(mnemonics.get(i));
            if (i != size - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }


    /**
     * 覆盖钱包的操作
     *
     * @param bean
     */
    @Override
    public void coverWallet(WalletBean bean) {
        List<WalletBean> list = null;
        if (!TextUtils.isEmpty(bean.getMnemonic())) {
            list = WalletDaoUtils.walletDao.queryBuilder().where(WalletBeanDao.Properties.Mnemonic.eq(bean.getMnemonic())).list();
        } else if (!TextUtils.isEmpty(bean.getPrivateKey())) {
            list = WalletDaoUtils.walletDao.queryBuilder().where(WalletBeanDao.Properties.PrivateKey.eq(bean.getPrivateKey())).list();
        } else {
            list = WalletDaoUtils.walletDao.queryBuilder().where(WalletBeanDao.Properties.Address.eq(bean.getAddress()),
                    WalletBeanDao.Properties.IsObserver.eq(true)
            ).list();
        }
        if (list != null) {
            WalletBean walletBean = list.get(0);
            WalletDaoUtils.walletDao.delete(walletBean);
            TokenDaoUtil.deleteWallet(walletBean.getId());
            if (!bean.getIsObserver()) {
                ChildAddressDaoUtil.deleteChildAddressForWalletId(walletBean.getId());
            }
        }
    }


    /**
     * 获得DeterministicSeed 助记词生成的种子
     *
     * @param params
     * @param mnemonic
     * @return
     * @throws UnreadableWalletException
     */
    public DeterministicSeed getSeedForMnemonic(NetworkParameters params, String mnemonic, String passphrase) throws UnreadableWalletException {
        String decrypt = AES.decrypt(mnemonic);
        List<String> mnemonicList = Arrays.asList(decrypt.split(" "));
        MnemonicUtil.validateMnemonics(mnemonicList);
        String seedCode = convertMnemonicList(mnemonicList);
        long createTime = System.currentTimeMillis() / 1000;
        DeterministicSeed seed = null;
        seed = new DeterministicSeed(seedCode, null, passphrase, createTime);
        Wallet wallet = Wallet.fromSeed(params, seed);
        return seed;
    }


    public DeterministicKeyChain getKeyChainForMnemonic(String mnemonic, String passphrase) throws UnreadableWalletException {
        long createTime = System.currentTimeMillis() / 1000;
        DeterministicSeed seed = new DeterministicSeed(mnemonic, null, passphrase, createTime);
        return DeterministicKeyChain.builder().seed(seed).build();
    }

    /**
     * 创建钱包后  存储普通地址及找零地址 除多签类型
     *
     * @param walletBean
     * @throws UnreadableWalletException
     */
    public List<ChildAddressBean> getChildAddressNormalBeans(WalletBean walletBean) throws UnreadableWalletException {
        List<ChildAddressBean> childAddressBeans = new ArrayList<>();
        NetworkParameters params = ParamsManagerBtc.getParams();
        switch (walletBean.getExistType()) {
            case Constants.EXIST_PRIVATE_KEY: {
                //私钥类型存储子地址及找零地址
                String decrypt = AES.decrypt(walletBean.getPrivateKey());
                ECKey indexKey = getECKeyForPrivateKey(params, decrypt);
                //根据选择协议类型保存子地址 找零地址也是自身
                String childAddress;

                if (walletBean.getAddressType() == Constants.CHILD_ADDRESS_NATIVE) {
                    childAddress = getSegWitAddress(params, indexKey);
                } else {
                    childAddress = getSegWitNestedAddress(params, indexKey);
                }

                ChildAddressBean childAddressSegWitBean = ChildAddressBean.newInstance(childAddress, walletBean.getId(), 0, walletBean.getAddressType(),
                        true, walletBean.getPrivateKey(), Constants.CHILD_ADDRESS_CHANGE_TYPE);
                childAddressBeans.add(childAddressSegWitBean);
                break;
            }
            case Constants.EXIST_MNEMONIC: {
                //助记词类型存储子地址及找零地址
                String mnemonic = AES.decrypt(walletBean.getMnemonic());
                long createTime = System.currentTimeMillis() / 1000;
                DeterministicSeed seed = new DeterministicSeed(mnemonic, null, AES.decrypt(walletBean.getPassphrase()), createTime);
                DeterministicKeyChain keyChain = DeterministicKeyChain.builder().seed(seed).build();
                for (int i = 0; i < CHILD_ADDRESS_SIZE; i++) {
                    //子地址
                    String childAddress;
                    //找零地址
                    String childChangeAddress;

                    DeterministicKey key;
                    DeterministicKey keyChange;
                    if (walletBean.getAddressType() == Constants.CHILD_ADDRESS_NATIVE) {
                        key = keyChain.getKeyByPath(generatePath("m/84'/0'/0'" + "/0/" + i), true);
                        childAddress = getSegWitAddress(params, key);
                        keyChange = keyChain.getKeyByPath(generatePath("m/84'/0'/0'" + "/1/" + i), true);
                        childChangeAddress = getSegWitAddress(params, keyChange);
                    } else {
                        key = keyChain.getKeyByPath(generatePath("m/49'/0'/0'" + "/0/" + i), true);
                        childAddress = getSegWitNestedAddress(params, key);
                        keyChange = keyChain.getKeyByPath(generatePath("m/49'/0'/0'" + "/1/" + i), true);
                        childChangeAddress = getSegWitNestedAddress(params, keyChange);
                    }

                    ChildAddressBean childAddressBean = ChildAddressBean.newInstance(childAddress, walletBean.getId(), i, walletBean.getAddressType(),
                            false, AES.encrypt(key.getPrivateKeyAsWiF(params)), Constants.CHILD_ADDRESS_NOT_CHANGE_TYPE);
                    ChildAddressBean childAddressChangeBean = ChildAddressBean.newInstance(childChangeAddress, walletBean.getId(), i, walletBean.getAddressType(),
                            false, AES.encrypt(keyChange.getPrivateKeyAsWiF(params)), Constants.CHILD_ADDRESS_CHANGE_TYPE);

                    childAddressBeans.add(childAddressBean);
                    childAddressBeans.add(childAddressChangeBean);
                }
                break;
            }
            case Constants.EXIST_PUBLIC_KEY: {
                //公钥类型存储子地址
                DeterministicKey parentDK = DeterministicKey.deserializeB58(walletBean.getPublicKey(), params);
                DeterministicKey changeKey = HDKeyDerivation.deriveChildKey(parentDK, 0);
                for (int i = 0; i < CHILD_ADDRESS_SIZE; i++) {
                    DeterministicKey childDK = HDKeyDerivation.deriveChildKey(changeKey, i);
                    String childAddress;
                    if (walletBean.getAddressType() == Constants.CHILD_ADDRESS_NATIVE) {
                        childAddress = getSegWitAddress(params, childDK);
                    } else {
                        childAddress = getSegWitNestedAddress(params, childDK);
                    }

                    ChildAddressBean childAddressBean = ChildAddressBean.newInstance(childAddress, walletBean.getId(), i, walletBean.getAddressType(),
                            true, "", Constants.CHILD_ADDRESS_NOT_CHANGE_TYPE);
                    childAddressBeans.add(childAddressBean);
                }
                DeterministicKey childChangeKey = HDKeyDerivation.deriveChildKey(parentDK, 1);
                for (int i = 0; i < CHILD_ADDRESS_SIZE; i++) {
                    DeterministicKey childDK = HDKeyDerivation.deriveChildKey(childChangeKey, i);
                    String childAddress;
                    if (walletBean.getAddressType() == Constants.CHILD_ADDRESS_NATIVE) {
                        childAddress = getSegWitAddress(params, childDK);
                    } else {
                        childAddress = getSegWitNestedAddress(params, childDK);
                    }

                    ChildAddressBean childAddressBean = ChildAddressBean.newInstance(childAddress, walletBean.getId(), i, walletBean.getAddressType(),
                            true, "", Constants.CHILD_ADDRESS_CHANGE_TYPE);
                    childAddressBeans.add(childAddressBean);
                }
                break;
            }

            case Constants.EXIST_ADDRESS: {
                //观察者存储子地址
                ChildAddressBean childAddressBean = ChildAddressBean.newInstance(walletBean.getAddress(), walletBean.getId(), 0, walletBean.getAddressType(),
                        true, "", Constants.CHILD_ADDRESS_NOT_CHANGE_TYPE);
                childAddressBeans.add(childAddressBean);
                break;
            }

            case Constants.EXIST_MULTI_SIG:
                //多签存储子地址
                String[] keys = walletBean.getPublicKey().split(",");
                for (String key : keys) {
                    DeterministicKey parentDK = DeterministicKey.deserializeB58(CommonUtil.ypubToXpub(key), params);
                    DeterministicKey changeKey = HDKeyDerivation.deriveChildKey(parentDK, 0);
                    for (int i = 0; i < CHILD_ADDRESS_SIZE; i++) {
                        DeterministicKey childDK = HDKeyDerivation.deriveChildKey(changeKey, i);
                        String childAddress;
                        if (walletBean.getAddressType() == Constants.CHILD_ADDRESS_NATIVE) {
                            childAddress = getSegWitAddress(params, childDK);
                        } else {
                            childAddress = getSegWitNestedAddress(params, childDK);
                        }

                        ChildAddressBean childAddressBean = ChildAddressBean.newInstance(childAddress, walletBean.getId(), i, walletBean.getAddressType(),
                                true, "", Constants.CHILD_ADDRESS_NOT_CHANGE_TYPE);
                        childAddressBeans.add(childAddressBean);
                    }
                }

                break;

            default:
                break;
        }

        return childAddressBeans;
    }

    public List<ChildAddressBean> getChildAddressNormalBeans(List<String> normalList, List<String> changeList, WalletBean walletBean) throws UnreadableWalletException {
        List<ChildAddressBean> childAddressBeans = new ArrayList<>();
        //多签存储子地址
        for (int i = 0; i < normalList.size(); i++) {
            ChildAddressBean childAddressBean = ChildAddressBean.newInstance(normalList.get(i), walletBean.getId(), i, walletBean.getAddressType(),
                    true, "", Constants.CHILD_ADDRESS_NOT_CHANGE_TYPE);
            ChildAddressBean childAddressChangeBean = ChildAddressBean.newInstance(changeList.get(i), walletBean.getId(), i, walletBean.getAddressType(),
                    true, "", Constants.CHILD_ADDRESS_CHANGE_TYPE);
            childAddressBeans.add(childAddressBean);
            childAddressBeans.add(childAddressChangeBean);
        }
        return childAddressBeans;
    }

    /**
     * 导出私钥  base58私钥
     *
     * @param currentWallet
     * @param childAddressBean
     */
    public String getPrivateKey(WalletBean currentWallet, ChildAddressBean childAddressBean) throws UnreadableWalletException {
    /*    NetworkParameters params = ParamsManagerBtc.getParams();
        if (!TextUtils.isEmpty(currentWallet.getPrivateKey())) {
            String keyStr = AES.decrypt(currentWallet.getPrivateKey());
            String privateKey = bigIntegerToBase58(keyStr);
            return privateKey;
        }
        if (!TextUtils.isEmpty(currentWallet.getMnemonic())) {
            DeterministicSeed seedForMnemonic = getSeedForMnemonic(params, currentWallet.getMnemonic());
            DeterministicKeyChain keyChain = DeterministicKeyChain.builder().seed(seedForMnemonic).build();
            DeterministicKey key = null;
            if (childAddressBean.getChildType() == Constants.CHILD_ADDRESS_NOMAL) {
                key = keyChain.getKeyByPath(generatePath("m/44'/0'/0'" + "/0/" + 0), true);
            }
            if (childAddressBean.getChildType() == Constants.CHILD_ADDRESS_SWIGET) {
                key = keyChain.getKeyByPath(generatePath("m/49'/0'/0'" + "/0/" + 0), true);
            }
            String privateKey = bigIntegerToBase58(key.getPrivateKeyAsHex());
            return privateKey;
        }*/
        if (!TextUtils.isEmpty(childAddressBean.getPrivateKey())) {
            String keyStr = AES.decrypt(childAddressBean.getPrivateKey());
            return bigIntegerToBase58(keyStr);
        }
        return "";
    }

    public byte[] getMnemonicDeterministicKey(WalletBean walletBean) throws UnreadableWalletException {
        byte[] mnemonicDeterministicKey = null;
        if (!TextUtils.isEmpty(walletBean.getMnemonic())) {
            mnemonicDeterministicKey = getMnemonicDeterministicKey(walletBean.getMnemonic(), walletBean.getPassphrase());
        }
        if (!TextUtils.isEmpty(walletBean.getPrivateKey())) {
            mnemonicDeterministicKey = getPublicKeyHash(walletBean.getMnemonic());
        }
        return mnemonicDeterministicKey;
    }

    public byte[] getMnemonicDeterministicKey(String mnemonic, String passphrase) throws UnreadableWalletException {
        long createTime = System.currentTimeMillis() / 1000;
        String decrypt = AES.decrypt(mnemonic);
        DeterministicSeed seed = new DeterministicSeed(decrypt, null, passphrase, createTime);
        DeterministicKeyChain keyChain = DeterministicKeyChain.builder().seed(seed).build();
        DeterministicKey parent = keyChain.getKeyByPath(generatePath("m/44'/0'/0'"), true);
        return parent.getPubKeyHash();
    }

    public byte[] getPublicKeyHash(String privateKey) throws UnreadableWalletException {
//        String privateKeyStr = "KzVqyEGkF3uZdiwFaAZirSwXVdEWqK4UfLQAjGLYhciunaqeKcoU";  //有余额
        String keyStr = AES.decrypt(privateKey);
        AbstractBitcoinNetParams params = ParamsManagerBtc.getParams();
        ECKey key = getECKeyForPrivateKey(params, privateKey);
        return key.getPubKeyHash();
    }


    /**
     * 获得交易签名的私钥
     *
     * @param currentWallet 32xFzZZQLvKZ4G83DmqUWQCyahkJtYTPVG
     */
    public ECKey getTransferPrivateKey(WalletBean currentWallet, String address) throws UnreadableWalletException {
        NetworkParameters params = ParamsManagerBtc.getParams();
        if (!TextUtils.isEmpty(currentWallet.getPrivateKey())) {
            String keyStr = AES.decrypt(currentWallet.getPrivateKey());
            DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, keyStr);
            ECKey ecKey = dumpedPrivateKey.getKey();
            return ecKey;
        }
        if (!TextUtils.isEmpty(currentWallet.getMnemonic())) {
            DeterministicSeed seedForMnemonic = getSeedForMnemonic(params, currentWallet.getMnemonic(), currentWallet.getPassphrase());
            DeterministicKeyChain keyChain = DeterministicKeyChain.builder().seed(seedForMnemonic).build();

            int childNumberForAddress = ChildAddressDaoUtil.getChildNumberForAddress(address, currentWallet.getId());
            DeterministicKey nomalKey = keyChain.getKeyByPath(generatePath("m/44'/0'/0'" + "/0/" + childNumberForAddress), true);

            DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, nomalKey.getPrivateKeyAsHex());
            ECKey ecKey = dumpedPrivateKey.getKey();
            return ecKey;
        }
        return null;
    }

    /**
     * int转换为base58
     *
     * @param hex
     * @return
     */
    public String bigIntegerToBase58(String hex) {
        hex = "80" + hex + "01";
        byte[] hash1 = Sha256Hash.hash(Hex.decode(hex));
        byte[] hash2 = Sha256Hash.hash(hash1);
        String result = hex + Hex.toHexString(hash2).substring(0, 8);
        return Base58.encode(Hex.decode(result));
    }

    /**
     * 获得ECkey对象
     *
     * @param params
     * @param privateKey
     * @return
     */
    public ECKey getECKeyForPrivateKey(NetworkParameters params, String privateKey) {
        ECKey key;
        if (privateKey.length() == 51 || privateKey.length() == 52) {
            DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, privateKey);
            key = dumpedPrivateKey.getKey();
        } else {
            BigInteger privKey = Base58.decodeToBigInteger(privateKey);
            key = ECKey.fromPrivate(privKey);
        }
        return key;
    }

    //BTC 不使用  应用于重置密码  但是btc 助记词不为时候 只能验证助记词
    @Override
    public String getPrivateForMnemonic(List<String> mnemonic) {
        return "null";
    }

    /**
     * 验证地址合法性，不支持bip44地址
     *
     * @param input
     * @return
     */
    public boolean validAddress(String input) {
        try {
            if (input.startsWith("1")) {
                return false;
            }
            NetworkParameters networkParameters = ParamsManagerBtc.getParams();
            Address address = Address.fromString(networkParameters, input);
            if (address != null)
                return true;
            else
                return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证地址合法性，支持bip44地址
     *
     * @param input
     * @return
     */
    public boolean validAddressTransfer(String input) {
        try {
            NetworkParameters networkParameters = ParamsManagerBtc.getParams();
            Address address = Address.fromString(networkParameters, input);
            if (address != null)
                return true;
            else
                return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证是否是多签钱包
     *
     * @param input
     * @return
     */
    public boolean validMultiSig(String input) {
        return input.contains("Policy: ") && input.contains("Format: ") && !input.contains("xpub");
    }

}
