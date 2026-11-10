package app.philab.common.data.repository.mapper

import app.philab.common.data.database.entity.HistoryEntity
import app.philab.common.domain.history.model.CalcRecord

fun HistoryEntity.toDomain(): CalcRecord =
    CalcRecord(id = id, kind = kind, title = title, input = input, result = result, createdAt = createdAt)

fun CalcRecord.toEntity(): HistoryEntity =
    HistoryEntity(id = id, kind = kind, title = title, input = input, result = result, createdAt = createdAt)
