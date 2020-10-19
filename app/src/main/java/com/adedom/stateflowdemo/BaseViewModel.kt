package com.adedom.stateflowdemo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.CoroutineContext

@ExperimentalCoroutinesApi
abstract class BaseViewModel<S : Any>(initialState: S) : ViewModel(), CoroutineScope {

    private val job = SupervisorJob()
    private val exceptionHandler = CoroutineExceptionHandler { _, err ->
        setError(err)
    }

    private val _stateFlow = MutableStateFlow(initialState)
    val stateFlow: StateFlow<S>
        get() = _stateFlow

    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable>
        get() = _error

    private val _attachFirstTime = MutableLiveData<Unit>().apply { value = Unit }
    val attachFirstTime: LiveData<Unit>
        get() = _attachFirstTime

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main + exceptionHandler

    override fun onCleared() {
        coroutineContext.cancel()
        super.onCleared()
    }

    protected fun setState(state: S) {
        _stateFlow.value = state
    }

    protected fun setError(throwable: Throwable) {
        _error.value = throwable
    }

}
