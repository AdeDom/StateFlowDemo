package com.adedom.stateflowdemo

import android.os.Bundle
import androidx.activity.viewModels
import com.adedom.stateflowdemo.MainState.WatchState
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

@ExperimentalCoroutinesApi
@FlowPreview
class MainActivity : BaseActivity() {

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel.stateFlow.observe { render(it) }

        actionFlow().observe { viewModel.process(it) }
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
                buttonStart.apply {
                    isEnabled = false
                    text = "START"
                }
                buttonPause.isEnabled = true
                buttonReset.isEnabled = true
            }
            WatchState.PAUSED -> {
                buttonStart.apply {
                    isEnabled = true
                    text = "RESUME"
                }
                buttonPause.isEnabled = false
                buttonReset.isEnabled = true
            }
            WatchState.IDLE -> {
                buttonStart.apply {
                    isEnabled = true
                    text = "START"
                }
                buttonPause.isEnabled = false
                buttonReset.isEnabled = false
            }
        }
    }

}
