package com.linktech.saihub.entity.event

class ErrorEvent() {

    var status: Int? = null
    var message: String? = null
    var url: String? = null
    var method: String? = null


    private constructor(status: Int?, message: String?, url: String?, method: String?) : this() {
        this.status = status
        this.message = message
        this.url = url
        this.method = method
    }

    companion object {
        fun getInstance(status: Int?, message: String?, url: String?, method: String?): ErrorEvent {
            return ErrorEvent(status, message, url, method)
        }
    }
}