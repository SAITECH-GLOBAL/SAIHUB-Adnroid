package com.linktech.saihub.util.system

import android.view.View
import android.widget.ImageView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.lang.reflect.Type


/**
 * Created by tromo on 2021/9/22.
 */

fun ImageView.load(url: String) {
    viewScope.launch(Dispatchers.IO) {

    }

}

val View.viewScope: CoroutineScope
    get() {
        // 获取现有 viewScope 对象
        val key = "ViewScope".hashCode()
        var scope = getTag(key) as? CoroutineScope
        // 若不存在则新建 viewScope 对象
        if (scope == null) {
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            // 将 viewScope 对象缓存为 View 的 tag
            setTag(key, scope)
            val listener = object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View?) {}

                override fun onViewDetachedFromWindow(v: View?) {
                    // 当 view detach 时 取消协程的任务
                    scope.cancel()
                }

            }
            addOnAttachStateChangeListener(listener)
        }
        return scope
    }


fun countDownCoroutines(
    timeMillis: Long,
    scope: LifecycleCoroutineScope,
    onTick: () -> Unit,
    onStart: (() -> Unit)? = null,
    onFinish: (() -> Unit)? = null,
): Job {
    return flow {
        while (true) {
            delay(timeMillis)
            emit(true)
        }
    }.flowOn(Dispatchers.IO)
        .onStart { onStart?.invoke() }
        .onCompletion { onFinish?.invoke() }
        .onEach { onTick.invoke() }
        .launchIn(scope)
}

fun countDownCoroutines(
    timeMillis: Long,
    scope: LifecycleCoroutineScope,
    onTick: () -> Unit,
    onFinish: (() -> Unit)? = null,
): Job {
    return flow {
        while (true) {
            emit(true)
            delay(timeMillis)
        }
    }.flowOn(Dispatchers.IO)
        .onCompletion {
            scope?.launch(Dispatchers.Main) {
                onFinish?.invoke()
            }
        }
        .onEach {
            scope?.launch(Dispatchers.Main) {
                onTick.invoke()
            }
        }
        .launchIn(scope)
}

fun View.setVisible(isVisible: Boolean) {
    this.visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun View.setInVisible(isVisible: Boolean) {
    this.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
}

fun RecyclerView.scrollItemToTop(position: Int) {
    val smoothScroller: LinearSmoothScroller = object : LinearSmoothScroller(context) {
        override fun getVerticalSnapPreference(): Int {
            return SNAP_TO_START
        }
    }
    smoothScroller.targetPosition = position
    layoutManager?.startSmoothScroll(smoothScroller)
}

//将集合分成几个集合 返回list
fun <T> averageAssign(source: List<T>, size: Int): List<List<T>> {
    val result = ArrayList<List<T>>()
    var remaider = source.size % size //(先计算出余数)
    val number = source.size / size  //然后是商
    var offset = 0//偏移量
    for (i in 0 until size) {
        var value: List<T>?
        if (remaider > 0) {
            value = source.subList(i * number + offset, (i + 1) * number + offset + 1)
            remaider--
            offset++
        } else {
            value = source.subList(i * number + offset, (i + 1) * number + offset)
        }
        result.add(value)
    }
    return result
}

fun appendAuth(accessToken: String): String {
    return "Bearer $accessToken"
}

fun getKeySend(content: String): List<String> {
    val list = mutableListOf<String>()
    runCatching {
        val split = content.split("@")
        list.add(split[0])
        list.add(split[1])
    }.onSuccess {
        return list
    }.onFailure {
        return list
    }
    return list
}

fun keySendToDomain(content: String): String {
    return "https://$content/"
}

fun formatDomain(content: String): String {
    return if (content.endsWith("/"))
        content
    else
        "${content}/"
}

fun <T> jsonArrayToList(array: JsonArray?, clazz: Class<T>?): List<T>? {
    val gson = Gson()
    val listType: Type =
        TypeToken.getParameterized(List::class.java, clazz).type
    return gson.fromJson(array, listType)
}
