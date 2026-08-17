package com.mej.rickmorty

import com.mej.rickmorty.data.di.dataModule
import com.mej.rickmorty.domain.repository.CharacterRepository
import com.mej.rickmorty.domain.usecase.LoadCharacterDetailUseCase
import com.mej.rickmorty.domain.usecase.LoadCharactersUseCase
import com.mej.rickmorty.feature.characters.charactersModule
import com.mej.rickmorty.feature.characters.detail.DetailViewModel
import com.mej.rickmorty.feature.characters.list.CharactersViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.koin.core.context.stopKoin
import org.koin.core.parameter.parametersOf
import org.koin.dsl.koinApplication

/**
 * Koin monta o grafo em runtime, entao um `get()` sem binding so falharia
 * quando a tela abrisse. Este teste sobe o container com os modulos reais e
 * resolve cada ponto de entrada — o contrapeso de nao ter a verificacao em
 * tempo de compilacao que um container gerado ofereceria.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class KoinModulesTest {

    private val koin = koinApplication {
        modules(dataModule, charactersModule)
    }.koin

    // `viewModelScope` resolve Dispatchers.Main, ausente na JVM de teste.
    @Before fun setUp() = Dispatchers.setMain(StandardTestDispatcher())

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun `grafo resolve repositorio e use cases`() {
        assertNotNull(koin.get<CharacterRepository>())
        assertNotNull(koin.get<LoadCharactersUseCase>())
        assertNotNull(koin.get<LoadCharacterDetailUseCase>())
    }

    @Test
    fun `grafo resolve os view models da feature`() {
        assertNotNull(koin.get<CharactersViewModel>())
        // O id do personagem chega como parametro em runtime.
        assertNotNull(koin.get<DetailViewModel> { parametersOf("1") })
    }
}
