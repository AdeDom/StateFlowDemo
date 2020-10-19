package com.adedom.stateflowdemo

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@FlowPreview
@ExperimentalCoroutinesApi
class MainViewModel : BaseViewModel<MainState>(MainState()) {

    private val channel = BroadcastChannel<MainAction>(Channel.BUFFERED)

    fun process(action: MainAction) {
        launch {
            channel.send(action)
        }
    }

    init {
        // keep resume seconds
        var resume = 0L

        channel
            .asFlow()
            .onEach { action ->
                when (action) {
                    MainAction.START -> Unit
                    MainAction.PAUSE -> resume = stateFlow.value.seconds
                    MainAction.RESET -> resume = 0
                }
            }
            .flatMapLatest { action ->
                when (action) {
                    MainAction.START -> {
                        generateSequence(resume + 1) { it + 1 }
                            .asFlow()
                            .onEach { delay(1_000) }
                            .onStart { emit(resume) }
                            .takeWhile { it <= MAX_SECONDS }
                            .map {
                                MainState(
                                    watchState = MainState.WatchState.RUNNING,
                                    seconds = it,
                                )
                            }
                            .onCompletion { emit(MainState()) }
                    }
                    MainAction.PAUSE -> {
                        flowOf(
                            MainState(
                                watchState = MainState.WatchState.PAUSED,
                                seconds = resume,
                            )
                        )
                    }
                    MainAction.RESET -> {
                        flowOf(MainState())
                    }
                }
            }
            .onEach { setState(it) }
            .onEach { Log.d("###", "Main state: $it") }
            .catch { setError(it) }
            .launchIn(viewModelScope)
    }

    private companion object {
        const val MAX_SECONDS = 10
    }

}
