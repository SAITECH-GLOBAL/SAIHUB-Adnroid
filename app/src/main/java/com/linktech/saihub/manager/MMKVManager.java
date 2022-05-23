package com.linktech.saihub.manager;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.tencent.mmkv.MMKV;

public class MMKVManager {

    private MMKV mmkv;
    private MMKV mmkvCacheData;
    private static volatile MMKVManager _INSTANCE;

    private MMKVManager() {
        mmkv = MMKV.defaultMMKV();
        String encryptKey = Hashing.sha256().hashString("", Charsets.UTF_8).toString();
        mmkvCacheData = MMKV.mmkvWithID("cache_data", MMKV.SINGLE_PROCESS_MODE, encryptKey);
    }

    public static MMKVManager getInstance() {
        if (_INSTANCE == null) {
            synchronized (MMKVManager.class) {
                if (_INSTANCE == null) {
                    _INSTANCE = new MMKVManager();
                }
            }
        }
        return _INSTANCE;
    }

    public MMKV mmkv() {
        return mmkv;
    }

    /**
     * 保存是否创建或导入钱包标志位  0 否 1 是
     *
     * @param walletFlag
     */
    public void saveWalletFlag(String walletFlag) {
        mmkvCacheData.encode("is_has_wallet", walletFlag);
    }

    /**
     * 获取是否创建或导入钱包标志位
     *
     * @return
     */
    public String getWalletFlag() {
        return mmkvCacheData.decodeString("is_has_wallet");
    }

    /**
     * 保存汇率
     *
     * @param exchangeRate
     */
    public void saveExchangeRate(String exchangeRate) {
        mmkvCacheData.encode("exchange_rate", exchangeRate);
    }

    /**
     * 获取汇率
     *
     * @return
     */
    public String getExchangeRate() {
        return mmkvCacheData.decodeString("exchange_rate");
    }
}
