package com.ahmoneam.basecleanarchitecture.base.platform

import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ahmoneam.basecleanarchitecture.Result
import com.ahmoneam.basecleanarchitecture.utils.EventObserver
import org.koin.androidx.viewmodel.ext.android.getViewModel
import timber.log.Timber
import java.lang.reflect.ParameterizedType
import kotlin.reflect.KClass

abstract class BaseFragment<ViewModel : BaseViewModel>
    : Fragment() {

    val viewModel: ViewModel by lazy { getViewModel(viewModelClass()) }

    @Suppress("UNCHECKED_CAST")
    private fun viewModelClass(): KClass<ViewModel> {
        // dirty hack to get generic type https://stackoverflow.com/a/1901275/719212
        return ((javaClass.genericSuperclass as ParameterizedType)
            .actualTypeArguments[0] as Class<ViewModel>).kotlin
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.loading.observe(this, EventObserver {
            if (it.loading) showLoading()
            else hideLoading()
        })

        viewModel.error.observe(this, EventObserver {
            hideLoading()
            showError(it)
        })

        viewModel.nextScreen.observe(this, EventObserver {
            when (it) {
                is BaseNavigationDestination.Activities -> it.start(activity!!)
                is BaseNavigationDestination.Fragments -> it.start(this)
                else -> {
                    Timber.e("Unsupported type ")
                }
            }
        })
    }

    open fun showError(error: Result.Error) {
        val errorMessage = error.exception.errorMessage ?: kotlin.run {
            return@run if (error.exception.errorMessageRes != null) {
                getString(error.exception.errorMessageRes)
            } else null
        } ?: "unexpected error"

        Toast.makeText(context!!, errorMessage, Toast.LENGTH_LONG).show()
    }

    open fun hideLoading() {
        Toast.makeText(context!!, "loaded", Toast.LENGTH_SHORT).show()
    }

    open fun showLoading() {
        Toast.makeText(context!!, "loading", Toast.LENGTH_SHORT).show()
    }

}