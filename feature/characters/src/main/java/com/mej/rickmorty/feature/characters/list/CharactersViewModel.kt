package com.mej.rickmorty.feature.characters.list

import androidx.lifecycle.viewModelScope
import com.mej.core.common.Outcome
import com.mej.core.mvi.MviViewModel
import com.mej.rickmorty.domain.model.CharacterFilter
import com.mej.rickmorty.domain.usecase.LoadCharactersUseCase
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel MVI da lista.
 *
 * O ciclo e sempre intent -> efeito colateral -> [CharactersResult] -> reducer.
 * Nenhum ponto do arquivo altera o estado diretamente: a mudanca passa sempre
 * por [CharactersReducer], o que mantem a transicao inspecionavel e testavel.
 */
@OptIn(FlowPreview::class)
class CharactersViewModel(
    private val loadCharacters: LoadCharactersUseCase,
) : MviViewModel<CharactersIntent, CharactersState, CharactersEffect>(CharactersState()) {

    /**
     * A busca vive num flow separado para poder ser debounced. Disparar uma
     * query por tecla digitada geraria uma requisicao por caractere.
     */
    private val queryFlow = MutableStateFlow("")

    init {
        queryFlow
            .drop(1)
            .debounce(SEARCH_DEBOUNCE_MS)
            .distinctUntilChanged()
            .onEach { query -> applyFilter(currentState.filter.copy(name = query)) }
            .launchIn(viewModelScope)
    }

    override suspend fun handleIntent(intent: CharactersIntent) {
        when (intent) {
            CharactersIntent.Started -> if (currentState.characters.isEmpty()) loadFirstPage()

            CharactersIntent.Retry -> loadFirstPage()

            CharactersIntent.LoadMore -> loadNextPage()

            is CharactersIntent.QueryChanged -> {
                // O texto entra no estado na hora para o campo nao "travar";
                // a requisicao so sai depois do debounce.
                reduce(CharactersResult.FilterChanged(currentState.filter.copy(name = intent.value)))
                queryFlow.value = intent.value
            }

            is CharactersIntent.StatusSelected ->
                applyFilter(currentState.filter.copy(status = intent.status))

            is CharactersIntent.CharacterClicked ->
                sendEffect(CharactersEffect.OpenDetail(intent.id))
        }
    }

    private fun applyFilter(filter: CharacterFilter) {
        reduce(CharactersResult.FilterChanged(filter))
        loadFirstPage()
    }

    private fun loadFirstPage() {
        reduce(CharactersResult.Loading)
        fetch(page = CharactersState.FIRST_PAGE, append = false)
    }

    private fun loadNextPage() {
        val page = currentState.nextPage ?: return
        if (!currentState.canLoadMore) return

        reduce(CharactersResult.LoadingMore)
        fetch(page = page, append = true)
    }

    private fun fetch(page: Int, append: Boolean) {
        viewModelScope.launch {
            when (val outcome = loadCharacters(page, currentState.filter)) {
                is Outcome.Success -> reduce(
                    CharactersResult.PageLoaded(
                        characters = outcome.data.characters,
                        nextPage = outcome.data.nextPage,
                        append = append,
                    ),
                )

                is Outcome.Failure -> {
                    reduce(CharactersResult.Failed(outcome.error))
                    // Falha ao paginar nao pode limpar a lista ja exibida, entao
                    // alem do estado o usuario recebe um aviso pontual.
                    if (append) sendEffect(CharactersEffect.ShowError(outcome.error))
                }
            }
        }
    }

    private fun reduce(result: CharactersResult) {
        setState { CharactersReducer.reduce(this, result) }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 350L
    }
}
