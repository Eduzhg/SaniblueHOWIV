package com.saniblue.app.domain.usecase

import com.saniblue.app.domain.model.DashboardStats
import com.saniblue.app.domain.repository.EnsaioRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDashboardStatsUseCase @Inject constructor(
    private val repository: EnsaioRepository
) {
    operator fun invoke(): Flow<DashboardStats> = repository.getDashboardStats()
}
