package com.linktech.saihub.mvvm.service

import com.google.gson.JsonObject
import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.entity.wallet.bean.UTXODataBean
import com.linktech.saihub.entity.wallet.bean.*
import com.linktech.saihub.net.BaseResp
import okhttp3.RequestBody
import retrofit2.http.*


interface WalletService {


    //获取BTC及USDT汇率
    @GET("rest/asset/rate")
    suspend fun getRate(): BaseResp<RateEntity>

    //获取BTC余额
    @GET("rest/asset/balance/btc")
    suspend fun getBTCBalance(
        @Query("address") address: String
    ): BaseResp<JsonObject>

    //获取OMNI余额  usdt id 31
    @GET("rest/asset/omni/rpc/balance")
    suspend fun getOMNIBalance(
        @Query("address") address: String,
        @Query("propertyId") propertyId: Int
    ): BaseResp<OMNIBalanceEntity>

    //上报地址
    @POST("rest/address/add/v2")
    suspend fun sendAddressToServer(
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): BaseResp<Boolean>

    //删除上报地址
    @POST("rest/address/remove")
    suspend fun removeAddress(
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): BaseResp<Boolean>

    //BTC转账记录
    @GET("rest/asset/record/list/btc")
    suspend fun getBTCRecord(
        @Query("address") address: String,
        @Query("pageNo") pageNo: Int,
        @Query("pageSize") pageSize: Int
    ): BaseResp<TransactionRecordEntity>

    //OMNI转账记录
    @GET("rest/asset/record/list/omni")
    suspend fun getOMNIRecord(
        @Query("address") address: String,
        @Query("propertyId") propertyId: Int,
        @Query("pageNo") pageNo: Int,
        @Query("pageSize") pageSize: Int
    ): BaseResp<TransactionRecordEntity>

    //获取gas price
    @GET("rest/asset/get/gasPrice")
    suspend fun getGasPrice(): BaseResp<GasEntity>

    //批量获取UTXO
    @GET("rest/asset/utxo")
    suspend fun getUTXO(
        @Query("address") address: String
    ): BaseResp<List<UTXOEntity>>

    //批量获取UTXO
    @Headers("url_name:${StringConstants.BLOCK_CHAIN_HEADER}")
    @GET("unspent")
    suspend fun getUTXOFromBlockChain(
        @Query("active") address: String
    ): UTXODataBean

    //批量获取UTXO
    @POST("rest/asset/broadcast/send/v1")
    suspend fun sendTransaction(
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): BaseResp<String>

    //获取UTXO tx_hex
    @Headers("url_name:${StringConstants.BLOCK_CHAIN_HEADER}")
    @GET("rawtx/{hash}?format=hex")
    suspend fun getUTXOHex(
        @Path("hash") hash: String
    ): String

}