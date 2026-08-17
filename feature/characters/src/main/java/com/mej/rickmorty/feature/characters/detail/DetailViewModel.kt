package com.mej.rickmorty.feature.characters.detail

import androidx.lifecycle.viewModelScope
import com.mej.core.common.Outcome
import com.mej.core.mvi.MviViewModel
import com.mej.rickmorty.domain.usecase.LoadCharacterDetailUseCase
import kotlinx.coroutines.launch

class DetailViewModel(
    private val characterId: String,
    private val loadDetail: LoadCharacterDetailUseCase,
) : MviViewModel<DetailIntent, DetailState, DetailEffect>(DetailState()) {

    override suspend fun handleIntent(intent: DetailIntent) = when (intent) {
        DetailIntent.Started -> if (currentState.detail == null) load() else Unit
        DetailIntent.Retry -> load()
    }

    private fun load() {
        setState { copy(isLoading = true, error = null) }

        viewModelScope.launch {
            when (val outcome = loadDetail(characterId)) {
                is Outcome.Success -> setState {
                    copy(isLoading = false, detail = outcome.data, error = null)
                }

                is Outcome.Failure -> setState {
                    copy(isLoading = false, error = outcome.error)
                }
            }
        }
    }
}
