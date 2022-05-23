package com.linktech.saihub.mvvm.base

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

/**
 * Created by tromo on 2021/8/29.
 */
@MainThread
inline fun <T> LiveData<T>.observe(
        owner: LifecycleOwner,
        crossinline onChanged: (T) -> Unit
): Observer<T> {
    val wrappedObserver = Observer<T> { t -> onChanged.invoke(t) }
    observe(owner, wrappedObserver)
    return wrappedObserver
}
typealias VmLiveData<T> = MutableLiveData<VmState<T>>