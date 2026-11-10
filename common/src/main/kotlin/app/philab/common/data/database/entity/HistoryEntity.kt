package app.philab.common.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import app.philab.common.domain.history.model.CalcKind

@Entity(
    tableName = "calc_history",
    indices = [
        Index(value = ["created_at"]),
        Index(value = ["kind", "created_at"]),
    ]
)
data class HistoryEntity(
    @PrimaryKey
    val id: String,

    val kind: CalcKind,

    val title: String,

    val input: String,

    val result: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
