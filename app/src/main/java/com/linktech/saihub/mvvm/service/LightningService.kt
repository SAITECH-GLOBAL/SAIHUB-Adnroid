package com.linktech.saihub.mvvm.service

import com.linktech.saihub.app.StringConstants
import com.linktech.saihub.entity.lightning.*
import retrofit2.http.*


interface LightningService {


    //创建账户
    @Headers("url_name:${StringConstants.LIGHTNING_HEADER}")
    @POST("create")
    suspend fun createAccount(
        @Header("host") host: String,
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): AccountEntity

    //获取access_token
    @Headers("url_name:${StringConstants.LIGHTNING_HEADER}")
    @POST("auth?type=auth")
    suspend fun getAccessToken(
        @Header("host") host: String,
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): AccessTokenEntity

    //获取链上BTC地址
    @Headers("url_name:${StringConstants.LIGHTNING_HEADER}")
    @GET("getbtc")
    suspend fun getBTCAddress(
        @Header("host") host: String,
        @Header("Authorization") Authorization: String,
    ): BTCAddressEntity

    //refresh token
    @Headers("url_name:${StringConstants.LIGHTNING_HEADER}")
    @POST("auth?type=refresh_token")
    suspend fun refreshToken(
        @Header("host") host: String,
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): AccessTokenEntity

    //创建invoice
    @Headers("url_name:${StringConstants.LIGHTNING_HEADER}")
    @POST("addinvoice")
    suspend fun addInvoice(
        @Header("host") host: String,
        @Header("Authorization") Authorization: String,
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): InvoiceEntity

    //查询用户invoice列表
    @Headers("url_name:${StringConstants.LIGHTNING_HEADER}")
    @GET("getuserinvoices")
    suspend fun getInvoiceList(
        @Header("host") host: String,
        @Header("Authorization") Authorization: String,
        @Query("limit") limit: Int
    ): InvoiceListEntity

    //查询余额
    @GET("balance")
    @Headers("url_name:${StringConstants.LIGHTNING_HEADER}")
    suspend fun getBalance(
        @Header("host") host: String,
        @Header("Authorization") Authorization: String
    ): LightningBalanceEntity

    //解析invoice
    @Headers("url_name:${StringConstants.LIGHTNING_HEADER}")
    @GET("decodeinvoice")
    suspend fun decodeInvoice(
        @Header("host") host: String,
        @Header("Authorization") Authorization: String,
        @Query("invoice") invoice: String
    ): DecodeInvoiceEntity


    //查询用户链上转账列表
    @Headers("url_name:${StringConstants.LIGHTNING_HEADER}")
    @GET("gettxs")
    suspend fun getTxsList(
        @Header("host") host: String,
        @Header("Authorization") Authorization: String,
        @Query("limit") limit: Int
//        @Query("offset") offset: Int
    ): TxListEntity

    //支付invoice
    @Headers("url_name:${StringConstants.LIGHTNING_HEADER}")
    @POST("payinvoice")
    suspend fun payInvoice(
        @Header("host") host: String,
        @Header("Authorization") Authorization: String,
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): Any

    //支付invoice 邮箱方式
    @Headers("url_name:${StringConstants.LIGHTNING_HEADER}")
    @POST("payinvoice")
    suspend fun payInvoiceUrlType(
        @Header("host") host: String,
        @Header("Authorization") Authorization: String,
        @Body params: Map<String, @JvmSuppressWildcards Any>
    ): Any

    //解析keysend 邮箱地址
    @Headers("url_name:${StringConstants.LIGHTNING_HEADER}")
    @GET(".well-known/lnurlp/{user}")
    suspend fun parseKeySend(
        @Header("host") host: String,
        @Header("Authorization") Authorization: String,
        @Path("user") user: String
    ): KeySendEntity

    //获取邮箱地址对应账单
    @GET
    suspend fun getKeySendCallback(
        @Url url: String,
        @Header("Authorization") Authorization: String,
        @Query("amount") amount: Long,//milliSatoshi
        @Query("nonce") nonce: Long,//随机数防止取缓存时间戳即可
        @Query("comment") comment: String
    ): KeySendCallbackEntity
}