package com.mej.rickmorty.feature.characters

import com.mej.core.common.AppError
import com.mej.rickmorty.domain.model.Character
import com.mej.rickmorty.domain.model.CharacterFilter
import com.mej.rickmorty.domain.model.CharacterStatus
import com.mej.rickmorty.feature.characters.list.CharactersReducer
import com.mej.rickmorty.feature.characters.list.CharactersResult
import com.mej.rickmorty.feature.characters.list.CharactersState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * O reducer e uma funcao pura, entao estes testes rodam sem coroutines, sem
 * mocks e sem Android — o que torna a regra de paginacao barata de cobrir.
 */
class CharactersReducerTest {

    private fun character(id: String) = Character(
        id = id,
        name = "Rick $id",
        status = CharacterStatus.ALIVE,
        species = "Human",
        imageUrl = "",
    )

    @Test
    fun `primeira pagina substitui a lista e limpa o erro`() {
        val current = CharactersState(
            characters = listOf(character("velho")),
            error = AppError.Network,
        )

        val next = CharactersReducer.reduce(
            current,
            CharactersResult.PageLoaded(listOf(character("1")), nextPage = 2, append = false),
        )

        assertEquals(listOf("1"), next.characters.map { it.id })
        assertEquals(2, next.nextPage)
        assertNull(next.error)
        assertFalse(next.isLoading)
    }

    @Test
    fun `pagina seguinte acumula sem duplicar a anterior`() {
        val current = CharactersState(characters = listOf(character("1")), nextPage = 2)

        val next = CharactersReducer.reduce(
            current,
            CharactersResult.PageLoaded(listOf(character("2")), nextPage = 3, append = true),
        )

        assertEquals(listOf("1", "2"), next.characters.map { it.id })
        assertEquals(3, next.nextPage)
    }

    @Test
    fun `nextPage nulo encerra a paginacao`() {
        val current = CharactersState(characters = listOf(character("1")), nextPage = 2)

        val next = CharactersReducer.reduce(
            current,
            CharactersResult.PageLoaded(listOf(character("2")), nextPage = null, append = true),
        )

        assertNull(next.nextPage)
        assertFalse(next.canLoadMore)
    }

    /** Falha ao paginar nao pode apagar o que o usuario ja esta lendo. */
    @Test
    fun `falha preserva a lista carregada`() {
        val current = CharactersState(
            characters = listOf(character("1")),
            isLoadingMore = true,
        )

        val next = CharactersReducer.reduce(current, CharactersResult.Failed(AppError.Timeout))

        assertEquals(listOf("1"), next.characters.map { it.id })
        assertEquals(AppError.Timeout, next.error)
        assertFalse(next.isLoadingMore)
    }

    @Test
    fun `mudanca de filtro reinicia a paginacao`() {
        val current = CharactersState(characters = listOf(character("1")), nextPage = 7)

        val next = CharactersReducer.reduce(
            current,
            CharactersResult.FilterChanged(CharacterFilter(name = "morty")),
        )

        assertEquals(CharactersState.FIRST_PAGE, next.nextPage)
        assertEquals("morty", next.filter.name)
    }

    @Test
    fun `canLoadMore fica falso durante o carregamento`() {
        val loading = CharactersState(isLoading = true, nextPage = 2)
        val loadingMore = CharactersState(isLoading = false, isLoadingMore = true, nextPage = 2)
        val ready = CharactersState(isLoading = false, nextPage = 2)

        assertFalse(loading.canLoadMore)
        assertFalse(loadingMore.canLoadMore)
        assertTrue(ready.canLoadMore)
    }
}
