package com.adedom.stateflowdemo

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.CheckResult
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.adedom.stateflowdemo.MainState.WatchState
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
@FlowPreview
class MainActivity : AppCompatActivity() {

    private val vm by viewModels<MainVM>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launchWhenStarted {
            vm.stateFlow
                    .onEach { render(it) }
                    .catch { }
                    .collect()
        }

        actionFlow()
                .onEach { vm.process(it) }
                .catch { }
                .launchIn(lifecycleScope)
    }

    private fun actionFlow(): Flow<MainAction> {
        return merge(
                buttonStart.clicks().map { MainAction.START },
                buttonPause.clicks().map { MainAction.PAUSE },
                buttonReset.clicks().map { MainAction.RESET },
        )
    }

    private fun render(state: MainState) {
        val mm = (state.seconds / 60).toString().padStart(2, '0')
        val ss = (state.seconds % 60).toString().padStart(2, '0')
        textView.text = "$mm:$ss"

        when (state.watchState) {
            WatchState.RUNNING -> {
                buttonStart.run {
                    isEnabled = false
                    text = "START"
                }
                buttonPause.isEnabled = true
                buttonReset.isEnabled = true
            }
            WatchState.PAUSED -> {
                buttonStart.run {
                    isEnabled = true
                    text = "RESUME"
                }
                buttonPause.isEnabled = false
                buttonReset.isEnabled = true
            }
            WatchState.IDLE -> {
                buttonStart.run {
                    isEnabled = true
                    text = "START"
                }
                buttonPause.isEnabled = false
                buttonReset.isEnabled = false
            }
        }
    }
}

@CheckResult
@ExperimentalCoroutinesApi
fun View.clicks(): Flow<Unit> {
    return callbackFlow {
        setOnClickListener { offer(Unit) }
        awaitClose { setOnClickListener(null) }
    }
}
