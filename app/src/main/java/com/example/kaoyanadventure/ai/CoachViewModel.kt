package com.example.kaoyanadventure.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kaoyanadventure.data.AppContainer
import com.example.kaoyanadventure.ui.util.buildDaySummaries
import com.example.kaoyanadventure.ui.util.computeStreak
import com.example.kaoyanadventure.ui.util.dayStartEpochMs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

data class CoachUiState(
    val modelPath: String = "",
    val ready: Boolean = false,
    val generating: Boolean = false,
    val output: String = ""
)

class CoachViewModel(private val container: AppContainer) : ViewModel() {

    private val engine = CoachLlmEngine()
    var state: CoachUiState = CoachUiState()
        private set

    fun refreshModelPathAndLoad() {
        viewModelScope.launch {
            val path = container.settings.llmModelPath.first()
            state = state.copy(modelPath = path)

            val ok = withContext(Dispatchers.Default) {
                try {
                    engine.load(path)
                    engine.isReady()
                } catch (_: Throwable) {
                    false
                }
            }
            state = state.copy(ready = ok)
        }
    }

    fun generateCoachText() {
        viewModelScope.launch {
            if (!state.ready) {
                state = state.copy(output = "教练还没上岗：去设置选择本地模型（.gguf）。")
                return@launch
            }
            state = state.copy(generating = true, output = "")

            val now = System.currentTimeMillis()
            val rangeStart = run {
                val c = Calendar.getInstance().apply { timeInMillis = now }
                c.add(Calendar.DAY_OF_YEAR, -29)
                dayStartEpochMs(c.timeInMillis)
            }
            val history = container.repo.observeSessionsBetween(rangeStart, now).first()
            val daySummaries = buildDaySummaries(history, rangeStart, 30, now)
            val streak = computeStreak(daySummaries)

            val summaryJson = StudySummaryBuilder.build(container.repo, now, streak)
            val prompt = PromptFactory.buildCoachPrompt(summaryJson)

            val text = withContext(Dispatchers.Default) {
                try {
                    engine.generate(prompt)
                } catch (e: Throwable) {
                    "教练翻车了：${e.message ?: "未知错误"}"
                }
            }

            state = state.copy(generating = false, output = text)
        }
    }

    override fun onCleared() {
        engine.close()
    }
}
