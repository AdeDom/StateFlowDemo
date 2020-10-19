package com.adedom.stateflowdemo

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

abstract class BaseActivity : AppCompatActivity() {

    protected inline fun <reified T> LiveData<T>.observe(crossinline onNext: (T) -> Unit) {
        observe(this@BaseActivity, { onNext(it) })
    }

    protected inline fun <reified T> Flow<T>.observe(crossinline onNext: (T) -> Unit) {
        this
            .catch { e ->
                Toast.makeText(baseContext, e.message, Toast.LENGTH_SHORT).show()
            }
            .asLiveData()
            .observe(this@BaseActivity, { onNext(it) })
    }

}
