package com.mej.rickmorty.feature.characters.list

import com.mej.core.mvi.Reducer
import com.mej.rickmorty.domain.model.CharacterStatus

/**
 * Reducer puro: mesma entrada, mesma saida, sem IO nem coroutine.
 *
 * Toda a regra de paginacao vive aqui — quando acumular pagina, quando
 * substituir a lista e quando parar de pedir mais. Isso a torna testavel sem
 * ViewModel, sem dispatcher e sem mock de repositorio.
 */
object CharactersReducer : Reducer<CharactersState, CharactersResult> {

    override fun reduce(current: CharactersState, result: CharactersResult): CharactersState =
        when (result) {
            CharactersResult.Loading -> current.copy(
                isLoading = true,
                isLoadingMore = false,
                error = null,
            )

            CharactersResult.LoadingMore -> current.copy(isLoadingMore = true, error = null)

            is CharactersResult.PageLoaded -> current.copy(
                isLoading = false,
                isLoadingMore = false,
                error = null,
                // Append acumula a pagina; caso contrario a lista e substituida,
                // que e o comportamento correto quando o filtro muda.
                characters = if (result.append) {
                    current.characters + result.characters
                } else {
                    result.characters
                },
                nextPage = result.nextPage,
            )

            is CharactersResult.Failed -> current.copy(
                isLoading = false,
                isLoadingMore = false,
                error = result.error,
            )

            is CharactersResult.FilterChanged -> current.copy(
                filter = result.filter,
                // Filtro novo reinicia a paginacao do zero.
                nextPage = CharactersState.FIRST_PAGE,
            )
        }

    /** Rotulo do status usado tanto pelo chip quanto pelo leitor de tela. */
    fun statusOptions(): List<CharacterStatus?> = listOf(null) + CharacterStatus.entries
}
