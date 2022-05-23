package com.linktech.saihub.net

/**
 * Created by tromo on 2021/7/27.
 */
data class BaseResp<T>(var code: Int, var msg: String, var data: DataEntity<T>?)


data class DataEntity<T>(var status: Int, var message: String, var result: T?)

