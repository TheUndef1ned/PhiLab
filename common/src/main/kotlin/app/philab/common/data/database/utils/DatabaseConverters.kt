package app.philab.common.data.database.utils

import androidx.room.TypeConverter
import app.philab.common.domain.history.model.CalcKind

class DatabaseConverters {

    @TypeConverter
    fun fromCalcKind(kind: CalcKind): String = kind.name

    @TypeConverter
    fun toCalcKind(value: String): CalcKind =
        CalcKind.entries.firstOrNull { it.name == value } ?: CalcKind.PHI
}
