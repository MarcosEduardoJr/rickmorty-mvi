package com.mej.rickmorty.domain.repository

import com.mej.core.common.Outcome
import com.mej.rickmorty.domain.model.CharacterDetail
import com.mej.rickmorty.domain.model.CharacterFilter
import com.mej.rickmorty.domain.model.CharacterPage

/** Contrato no dominio, implementacao no data: a dependencia aponta para dentro. */
interface CharacterRepository {
    suspend fun loadPage(page: Int, filter: CharacterFilter): Outcome<CharacterPage>

    suspend fun loadDetail(id: String): Outcome<CharacterDetail>
}
