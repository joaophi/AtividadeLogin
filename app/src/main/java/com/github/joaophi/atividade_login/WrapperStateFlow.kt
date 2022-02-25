package com.github.joaophi.atividade_login

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

fun <T> SavedStateHandle.getStateFlow(initialValue: T) = PropertyDelegateProvider { _: Any?, prop ->
    val stateFlow: MutableStateFlow<T> = WrapperStateFlow(getLiveData(prop.name, initialValue))
    ReadOnlyProperty { _: Any?, _ -> stateFlow }
}

private class WrapperStateFlow<T>(private val liveData: MutableLiveData<T>) : MutableStateFlow<T> {
    override val subscriptionCount: StateFlow<Int>
        get() = TODO("Not yet implemented")

    override suspend fun emit(value: T) {
        this.value = value
    }

    @ExperimentalCoroutinesApi
    override fun resetReplayCache() {
        TODO("Not yet implemented")
    }

    override fun tryEmit(value: T): Boolean {
        this.value = value
        return true
    }

    override var value: T
        @Suppress("UNCHECKED_CAST")
        get() = liveData.value as T
        @SuppressLint("NullSafeMutableLiveData")
        set(value) = liveData.postValue(value)

    override fun compareAndSet(expect: T, update: T): Boolean {
        val equal = expect == value
        if (equal) value = update
        return equal
    }

    override val replayCache: List<T>
        get() = listOf(value)

    override suspend fun collect(collector: FlowCollector<T>): Nothing {
        liveData.asFlow().distinctUntilChanged().collect(collector)
        awaitCancellation()
    }
}