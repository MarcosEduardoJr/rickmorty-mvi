package com.mej.rickmorty.domain.usecase

import com.mej.core.common.Outcome
import com.mej.rickmorty.domain.model.CharacterDetail
import com.mej.rickmorty.domain.model.CharacterFilter
import com.mej.rickmorty.domain.model.CharacterPage
import com.mej.rickmorty.domain.repository.CharacterRepository

class LoadCharactersUseCase(
    private val repository: CharacterRepository,
) {
    suspend operator fun invoke(page: Int, filter: CharacterFilter): Outcome<CharacterPage> =
        repository.loadPage(page, filter)
}

class LoadCharacterDetailUseCase(
    private val repository: CharacterRepository,
) {
    suspend operator fun invoke(id: String): Outcome<CharacterDetail> = repository.loadDetail(id)
}
