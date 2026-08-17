package com.mej.rickmorty.feature.characters.detail

import com.mej.core.common.AppError
import com.mej.core.mvi.MviEffect
import com.mej.core.mvi.MviIntent
import com.mej.core.mvi.MviState
import com.mej.rickmorty.domain.model.CharacterDetail

sealed interface DetailIntent : MviIntent {
    data object Started : DetailIntent
    data object Retry : DetailIntent
}

data class DetailState(
    val isLoading: Boolean = true,
    val detail: CharacterDetail? = null,
    val error: AppError? = null,
) : MviState

sealed interface DetailEffect : MviEffect
