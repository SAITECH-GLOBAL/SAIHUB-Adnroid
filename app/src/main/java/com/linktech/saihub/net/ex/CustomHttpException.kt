package com.linktech.saihub.net.ex

/**
 * Created by tromo on 2021/7/27.
 */
class CustomHttpException(code: Int) : RuntimeException() {

    init {
        val errorMessage = getCustomHttpException(code)
//        throw RuntimeException(errorMessage)
    }

    private fun getCustomHttpException(code: Int): String {
        when (code) {
            201 -> return "."
            500 -> return "."
            else -> {

            }
        }
        return "."
    }

}