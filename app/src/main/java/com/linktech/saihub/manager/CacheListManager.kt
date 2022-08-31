package com.linktech.saihub.manager

import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.linktech.saihub.entity.lightning.TxEntity
import com.linktech.saihub.entity.wallet.bean.RateEntity

/**
 * Created by tromo on 2021/6/2.
 */
class CacheListManager {

    companion object {
        var instance: CacheListManager? = null
            get() {
                if (field == null) {
                    field = CacheListManager()
                }
                return field
            }
            private set
    }

    /**
     * 保存是否创建或导入钱包标志位
     *
     * @param flag
     */
    fun saveWalletFlag(flag: Int) {
        MMKVManager.getInstance().saveWalletFlag(flag.toString()) //加载到缓存
    }

    /**
     * 获取是否创建或导入钱包标志位
     *
     * @return
     */
    val walletFlag: Int
        get() {
            val flag = MMKVManager.getInstance().walletFlag
            return if (TextUtils.isEmpty(flag)) {
                -1
            } else {
                flag.toInt()
            }
        }

    /**
     * 保存汇率缓存
     *
     */
    fun saveExchangeRate(rateEntity: RateEntity) {
        val gson = Gson()
        val s = gson.toJson(rateEntity)
        MMKVManager.getInstance().saveExchangeRate(s) //加载到缓存
    }

    /**
     * 获取汇率缓存
     *
     */
    val rateEntity: RateEntity
        get() {
            val exchangeRate = MMKVManager.getInstance().exchangeRate
            val gson = Gson()
            return gson.fromJson(exchangeRate, RateEntity::class.java)
        }

    fun saveLnListCacheByType(listData: List<TxEntity>, id: Long) {
        val dAppListCache: MutableMap<Long, String> = getLnListCache()
        dAppListCache[id] = Gson().toJson(listData)
        saveLnListCache(dAppListCache)
    }

    private fun saveLnListCache(lnList: MutableMap<Long, String>) {
        val gson = Gson()
        val s = gson.toJson(lnList)
        MMKVManager.getInstance().saveLnCacheList(s) //加载到缓存
    }

    private fun getLnListCache(): MutableMap<Long, String> {
        val mapData: MutableMap<Long, String>
        val gson = Gson()
        val dAppTypeCacheList: String? = MMKVManager.getInstance().lnCacheList
        mapData = if (!TextUtils.isEmpty(dAppTypeCacheList)) {
            gson.fromJson(
                dAppTypeCacheList,
                object : TypeToken<Map<Long, String>>() {}.type
            )
        } else {
            hashMapOf()
        }
        return mapData
    }

    /**
     * 根据钱包id获取闪电账单缓存
     *
     * @return
     */
    fun getLnListCacheById(id: Long): List<TxEntity> {
        val lnListCache: Map<Long, String> = getLnListCache()
        val txList: List<TxEntity> = if (lnListCache.containsKey(id)) {
            val s = lnListCache[id]!!
            Gson().fromJson(s, object : TypeToken<List<TxEntity>>() {}.type)
        } else {
            listOf()
        }
        return txList
    }
}