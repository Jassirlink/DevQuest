package com.devquest.app

import androidx.lifecycle.*
import com.devquest.domain.*
import com.devquest.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class UiState(
    val loading: Boolean = true,
    val data: Snapshot = Snapshot(),
    val preferences: Preferences = Preferences(),
    val error: String? = null,
    val pendingQuiz: String? = null,
)

sealed interface Action {
    data class Settings(val value: Preferences) : Action

    data class Complete(val id: String) : Action

    data class Bookmark(val id: String) : Action

    data class Position(val id: String, val value: Int) : Action

    data class StartQuiz(val ids: List<String>) : Action

    data class Answer(val attempt: String, val question: String, val selected: Int) : Action

    data class FinishQuiz(val id: String) : Action

    data class Draft(val id: String, val code: String, val reviewed: Boolean) : Action

    data class Interview(
        val id: String,
        val question: String,
        val confidence: Int,
        val seconds: Int,
    ) : Action

    data class Study(val seconds: Int, val category: String) : Action

    data object Reset : Action

    data object DismissError : Action

    data object Navigated : Action

    data object RetryLoad : Action
}

@HiltViewModel
class QuestViewModel
@Inject
constructor(
    private val repository: QuestRepository,
    private val settings: SettingsRepository,
    private val complete: CompleteLesson,
    private val saved: SavedStateHandle,
) : ViewModel() {
    private val mutable = MutableStateFlow(UiState(pendingQuiz = saved["pendingQuiz"]))
    val state: StateFlow<UiState> = mutable.asStateFlow()
    private var loadingJob: Job? = null

    init {
        load()
    }

    private fun load() {
        loadingJob?.cancel()
        loadingJob =
            viewModelScope.launch {
                try {
                    repository.seed()
                    combine(repository.snapshot, settings.preferences) { s, p -> s to p }
                        .collect { (s, p) ->
                            mutable.update { it.copy(loading = false, data = s, preferences = p) }
                        }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    mutable.update {
                        it.copy(loading = false, error = e.message ?: "Unable to load local data")
                    }
                }
            }
    }

    fun act(action: Action) {
        viewModelScope.launch {
            try {
                when (action) {
                    is Action.Settings -> settings.update(action.value)
                    is Action.Complete -> complete(action.id)
                    is Action.Bookmark -> repository.bookmark(action.id)
                    is Action.Position -> repository.readingPosition(action.id, action.value)
                    is Action.StartQuiz -> {
                        val id = repository.startQuiz(action.ids)
                        saved["pendingQuiz"] = id
                        mutable.update { it.copy(pendingQuiz = id) }
                    }
                    is Action.Answer ->
                        repository.answer(action.attempt, action.question, action.selected)
                    is Action.FinishQuiz -> repository.finishQuiz(action.id)
                    is Action.Draft -> repository.saveDraft(action.id, action.code, action.reviewed)
                    is Action.Interview ->
                        repository.interview(
                            action.id,
                            action.question,
                            action.confidence,
                            action.seconds,
                        )
                    is Action.Study ->
                        repository.study(
                            UUID.randomUUID().toString(),
                            action.seconds,
                            action.category,
                        )
                    Action.Reset -> repository.reset()
                    Action.DismissError -> mutable.update { it.copy(error = null) }
                    Action.Navigated -> {
                        saved["pendingQuiz"] = null
                        mutable.update { it.copy(pendingQuiz = null) }
                    }
                    Action.RetryLoad -> load()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                mutable.update { it.copy(error = e.message ?: "Could not save. Please retry.") }
            }
        }
    }
}
