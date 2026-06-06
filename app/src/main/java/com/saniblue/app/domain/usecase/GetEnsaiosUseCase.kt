package com.saniblue.app.domain.usecase

import com.saniblue.app.domain.model.Ensaio
import com.saniblue.app.domain.repository.EnsaioRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEnsaiosUseCase @Inject constructor(
    private val repository: EnsaioRepository
) {
    operator fun invoke(query: String = ""): Flow<List<Ensaio>> =
        if (query.isBlank()) repository.getAll() else repository.search(query)
}
