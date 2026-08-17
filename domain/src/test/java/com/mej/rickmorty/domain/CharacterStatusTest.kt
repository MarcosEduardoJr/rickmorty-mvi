package com.mej.rickmorty.domain

import com.mej.rickmorty.domain.model.CharacterStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterStatusTest {

    @Test
    fun `converte os valores conhecidos da api ignorando caixa`() {
        assertEquals(CharacterStatus.ALIVE, CharacterStatus.fromApi("Alive"))
        assertEquals(CharacterStatus.DEAD, CharacterStatus.fromApi("dead"))
    }

    @Test
    fun `valor ausente ou desconhecido vira UNKNOWN em vez de quebrar`() {
        assertEquals(CharacterStatus.UNKNOWN, CharacterStatus.fromApi(null))
        assertEquals(CharacterStatus.UNKNOWN, CharacterStatus.fromApi("teleported"))
    }
}
