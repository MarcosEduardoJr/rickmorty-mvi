package com.mej.rickmorty.feature.characters

import app.cash.turbine.test
import com.mej.core.common.AppError
import com.mej.core.common.Outcome
import com.mej.rickmorty.domain.model.Character
import com.mej.rickmorty.domain.model.CharacterFilter
import com.mej.rickmorty.domain.model.CharacterPage
import com.mej.rickmorty.domain.model.CharacterStatus
import com.mej.rickmorty.domain.repository.CharacterRepository
import com.mej.rickmorty.domain.usecase.LoadCharactersUseCase
import com.mej.rickmorty.feature.characters.list.CharactersEffect
import com.mej.rickmorty.feature.characters.list.CharactersIntent
import com.mej.rickmorty.feature.characters.list.CharactersViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharactersViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    /** Fake com estado real: o teste valida resultado, nao chamada de metodo. */
    private class FakeRepository : CharacterRepository {
        var failNext = false
        val requestedPages = mutableListOf<Int>()
        var lastFilter: CharacterFilter? = null

        override suspend fun loadPage(page: Int, filter: CharacterFilter): Outcome<CharacterPage> {
            requestedPages += page
            lastFilter = filter
            if (failNext) return Outcome.Failure(AppError.Network)

            return Outcome.Success(
                CharacterPage(
                    characters = listOf(
                        Character("$page", "Rick $page", CharacterStatus.ALIVE, "Human", ""),
                    ),
                    nextPage = if (page < LAST_PAGE) page + 1 else null,
                ),
            )
        }

        override suspend fun loadDetail(id: String) = error("nao usado")

        private companion object {
            const val LAST_PAGE = 3
        }
    }

    private val repository = FakeRepository()
    private fun viewModel() = CharactersViewModel(LoadCharactersUseCase(repository))

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `Started carrega a primeira pagina`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.state.test {
            assertTrue(awaitItem().isLoading)

            vm.dispatch(CharactersIntent.Started)

            var loaded = awaitItem()
            while (loaded.characters.isEmpty()) loaded = awaitItem()

            assertEquals(listOf("1"), loaded.characters.map { it.id })
            assertEquals(listOf(1), repository.requestedPages)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `LoadMore acumula a proxima pagina`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.state.test {
            awaitItem()
            vm.dispatch(CharactersIntent.Started)
            var loaded = awaitItem()
            while (loaded.characters.isEmpty()) loaded = awaitItem()

            vm.dispatch(CharactersIntent.LoadMore)
            var paged = awaitItem()
            while (paged.characters.size < 2) paged = awaitItem()

            assertEquals(listOf("1", "2"), paged.characters.map { it.id })
            assertEquals(listOf(1, 2), repository.requestedPages)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `clique no personagem emite efeito de navegacao`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.effects.test {
            vm.dispatch(CharactersIntent.CharacterClicked("42"))

            assertEquals(CharactersEffect.OpenDetail("42"), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `falha na primeira pagina vira erro no estado`() = runTest(dispatcher) {
        repository.failNext = true
        val vm = viewModel()

        vm.state.test {
            awaitItem()
            vm.dispatch(CharactersIntent.Started)

            var failed = awaitItem()
            while (failed.error == null) failed = awaitItem()

            assertEquals(AppError.Network, failed.error)
            assertTrue(failed.characters.isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `filtro de status reinicia a busca na primeira pagina`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.state.test {
            awaitItem()
            vm.dispatch(CharactersIntent.Started)
            var loaded = awaitItem()
            while (loaded.characters.isEmpty()) loaded = awaitItem()

            vm.dispatch(CharactersIntent.LoadMore)
            while (awaitItem().characters.size < 2) Unit

            vm.dispatch(CharactersIntent.StatusSelected(CharacterStatus.DEAD))
            var filtered = awaitItem()
            while (filtered.filter.status != CharacterStatus.DEAD || filtered.characters.size != 1) {
                filtered = awaitItem()
            }

            assertEquals(CharacterStatus.DEAD, repository.lastFilter?.status)
            assertEquals(1, filtered.characters.size)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
