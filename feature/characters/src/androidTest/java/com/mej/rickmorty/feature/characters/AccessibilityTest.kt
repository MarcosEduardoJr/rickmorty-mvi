package com.mej.rickmorty.feature.characters

import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import com.mej.core.common.AppError
import com.mej.core.designsystem.AppTheme
import com.mej.rickmorty.domain.model.Character
import com.mej.rickmorty.domain.model.CharacterStatus
import com.mej.rickmorty.feature.characters.list.CharactersScreen
import com.mej.rickmorty.feature.characters.list.CharactersState
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Auditoria sobre a arvore semantica mesclada, que e a mesma que o TalkBack
 * consome. Assertar sobre ela vale mais do que inspecionar o layout.
 *
 * Numa `LazyColumn` itens fora da viewport podem seguir na arvore com bounds
 * zerados, entao apenas nos renderizados sao medidos.
 */
class AccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val characters = List(size = 6) { index ->
        Character(
            id = "$index",
            name = "Personagem $index",
            status = CharacterStatus.entries[index % CharacterStatus.entries.size],
            species = "Human",
            imageUrl = "",
        )
    }

    private fun render(state: CharactersState) {
        composeTestRule.setContent {
            AppTheme {
                CharactersScreen(
                    state = state,
                    effects = emptyFlow(),
                    onIntent = {},
                    onOpenDetail = {},
                )
            }
        }
    }

    private fun SemanticsNode.accessibleName(): String {
        val description = config.getOrNull(SemanticsProperties.ContentDescription)
            ?.joinToString(" ")
            .orEmpty()
        val text = config.getOrNull(SemanticsProperties.Text)
            ?.joinToString(" ") { it.text }
            .orEmpty()
        return "$description $text".trim()
    }

    private fun renderedInteractiveNodes(): List<SemanticsNode> = composeTestRule
        .onAllNodes(hasClickAction())
        .fetchSemanticsNodes()
        .filter { it.layoutInfo.isPlaced && !it.touchBoundsInRoot.isEmpty }

    /** WCAG 1.1.1 Non-text Content e 4.1.2 Name, Role, Value. */
    @Test
    fun todoElementoAcionavelTemNomeAcessivel() {
        render(CharactersState(isLoading = false, characters = characters))

        val semNome = renderedInteractiveNodes().filter { it.accessibleName().isBlank() }

        assertTrue(
            "Elementos acionaveis sem nome acessivel: ${semNome.map { it.touchBoundsInRoot }}",
            semNome.isEmpty(),
        )
    }

    /**
     * WCAG 2.5.8 exige 24dp; o design system adota 48dp, alinhado ao
     * WCAG2Mobile. A medida usa `touchBoundsInRoot`, que inclui a area de
     * toque estendida — o que o dedo alcanca, nao so o pixel desenhado.
     */
    @Test
    fun todoAlvoDeToqueTemPeloMenos48dp() {
        render(CharactersState(isLoading = false, characters = characters))

        val nodes = renderedInteractiveNodes()
        val pequenos = nodes.mapNotNull { node ->
            val largura = with(composeTestRule.density) { node.touchBoundsInRoot.width.toDp() }
            val altura = with(composeTestRule.density) { node.touchBoundsInRoot.height.toDp() }
            val nome = node.accessibleName().ifBlank { "sem rotulo" }

            if (largura < MIN_TARGET || altura < MIN_TARGET) "$nome ($largura x $altura)" else null
        }

        assertTrue("Nenhum alvo medido", nodes.size >= MIN_MEASURED)
        assertTrue("Alvos menores que 48dp: $pequenos", pequenos.isEmpty())
    }

    /** WCAG 1.3.1 e 2.4.6: titulo e secoes navegaveis por cabecalho. */
    @Test
    fun tituloDaTelaEMarcadoComoCabecalho() {
        render(CharactersState(isLoading = false, characters = characters))

        val headings = composeTestRule
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
            .fetchSemanticsNodes()
            .mapNotNull { it.config.getOrNull(SemanticsProperties.Text)?.joinToString { t -> t.text } }

        assertTrue("Esperava cabecalho de titulo, veio: $headings", headings.isNotEmpty())
    }

    /** WCAG 4.1.3 Status Messages: carregamento anunciado sem exigir foco. */
    @Test
    fun carregamentoUsaLiveRegion() {
        render(CharactersState(isLoading = true))

        val liveRegions = composeTestRule
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion))
            .fetchSemanticsNodes()

        assertTrue("Estado de carregamento deveria ser live region", liveRegions.isNotEmpty())
    }

    /** WCAG 3.3.1: a falha e anunciada e oferece caminho de recuperacao. */
    @Test
    fun erroAnunciaMensagemEOfereceRetry() {
        render(CharactersState(isLoading = false, error = AppError.Network))

        val liveRegions = composeTestRule
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion))
            .fetchSemanticsNodes()

        assertTrue("Erro deveria ser anunciado por live region", liveRegions.isNotEmpty())

        val temRetry = renderedInteractiveNodes().any {
            it.accessibleName().contains("Tentar", ignoreCase = true)
        }
        assertTrue("Estado de erro deveria oferecer acao de retry", temRetry)
    }

    private companion object {
        val MIN_TARGET = 48.dp
        const val MIN_MEASURED = 5
    }
}
