package com.mej.rickmorty.feature.characters.list

import com.mej.core.common.AppError
import com.mej.core.mvi.MviEffect
import com.mej.core.mvi.MviIntent
import com.mej.core.mvi.MviState
import com.mej.rickmorty.domain.model.Character
import com.mej.rickmorty.domain.model.CharacterFilter
import com.mej.rickmorty.domain.model.CharacterStatus

/** Tudo que a tela pode receber do usuario. */
sealed interface CharactersIntent : MviIntent {
    data object Started : CharactersIntent
    data object Retry : CharactersIntent
    data object LoadMore : CharactersIntent
    data class QueryChanged(val value: String) : CharactersIntent
    data class StatusSelected(val status: CharacterStatus?) : CharactersIntent
    data class CharacterClicked(val id: String) : CharactersIntent
}

/**
 * Resultado interno de um intent. Existe para que a transicao de estado fique
 * numa funcao pura, separada do efeito colateral que a produziu.
 */
sealed interface CharactersResult {
    data object Loading : CharactersResult
    data object LoadingMore : CharactersResult
    data class PageLoaded(
        val characters: List<Character>,
        val nextPage: Int?,
        val append: Boolean,
    ) : CharactersResult

    data class Failed(val error: AppError) : CharactersResult
    data class FilterChanged(val filter: CharacterFilter) : CharactersResult
}

data class CharactersState(
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val characters: List<Character> = emptyList(),
    val filter: CharacterFilter = CharacterFilter(),
    val nextPage: Int? = FIRST_PAGE,
    val error: AppError? = null,
) : MviState {

    val isEmpty: Boolean get() = !isLoading && error == null && characters.isEmpty()

    val canLoadMore: Boolean get() = nextPage != null && !isLoading && !isLoadingMore && error == null

    companion object {
        const val FIRST_PAGE = 1
    }
}

/** Eventos de uma vez so, entregues pelo canal do runtime MVI. */
sealed interface CharactersEffect : MviEffect {
    data class OpenDetail(val id: String) : CharactersEffect
    data class ShowError(val error: AppError) : CharactersEffect
}
