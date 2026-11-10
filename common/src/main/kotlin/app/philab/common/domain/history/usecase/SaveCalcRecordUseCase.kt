package app.philab.common.domain.history.usecase

import app.philab.common.domain.history.model.CalcKind
import app.philab.common.domain.history.model.CalcRecord
import app.philab.common.domain.history.repository.HistoryRepository
import java.util.UUID
import javax.inject.Inject

class SaveCalcRecordUseCase @Inject constructor(
    private val repository: HistoryRepository,
) {
    suspend operator fun invoke(
        kind: CalcKind,
        title: String,
        input: String,
        result: String,
    ): Result<Unit> = repository.add(
        CalcRecord(
            id = UUID.randomUUID().toString(),
            kind = kind,
            title = title,
            input = input,
            result = result,
            createdAt = System.currentTimeMillis(),
        )
    )
}
