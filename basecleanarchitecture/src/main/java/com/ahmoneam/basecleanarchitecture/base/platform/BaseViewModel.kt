package com.ahmoneam.basecleanarchitecture.base.platform

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ahmoneam.basecleanarchitecture.Result
import com.ahmoneam.basecleanarchitecture.utils.ApplicationException
import com.ahmoneam.basecleanarchitecture.utils.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class BaseViewModel : ViewModel() {
    val error = MutableLiveData<Event<Result.Error>>()
    val loading = MutableLiveData<Event<Result.Loading>>()
    val nextScreen = MutableLiveData<Event<BaseNavigationDestination<*, *>>>()

    inline fun wrapBlockingOperation(
        showLoading: Boolean = true,
        crossinline function: suspend CoroutineScope.() -> Unit
    ) {
        loading.value = Event(Result.Loading(showLoading))
        Timber.e("show loading")
        viewModelScope.launch {
            try {
                function()
                Timber.e("function")
            } catch (throwable: Throwable) {
                handelError(throwable)
                Timber.e(throwable)
            } finally {
                loading.value = Event(Result.Loading(false))
                Timber.e("hide loading")
            }
        }
    }

    fun <T> handleResult(result: Result<T>, onSuccess: (Result.Success<T>) -> Unit) {
        when (result) {
            is Result.Success<T> -> {
                onSuccess(result)
            }
            is Result.Error -> {
                throw result.exception
            }
        }
    }

    fun handelError(throwable: Throwable) {
        if (throwable is ApplicationException) {
            error.postValue(Event(Result.Error(throwable)))

//            when (throwable.type) {
//                ErrorType.Network.Unauthorized -> TODO()
//                ErrorType.Network.ResourceNotFound -> TODO()
//                ErrorType.Network.Unexpected -> TODO()
//                ErrorType.Network.NoInternetConnection -> TODO()
//                else -> {
//                }
//            }
        }
//        TODO()
    }
}