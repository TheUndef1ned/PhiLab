package app.philab.common.data.datasource

import app.philab.common.data.datasource.dto.PhiTableDto
import app.philab.common.domain.phi.model.PhiTable
import kotlinx.serialization.json.Json

object PhiTableJsonParser {

    private val json = Json { ignoreUnknownKeys = true }

    fun parse(text: String): PhiTable {
        val dto = json.decodeFromString(PhiTableDto.serializer(), text)
        require(dto.values.size == dto.count) { "phi_table.json: count != values.size" }
        val values = DoubleArray(dto.count)
        val decimals = IntArray(dto.count)
        dto.values.forEachIndexed { i, raw ->
            values[i] = raw.toDouble()
            decimals[i] = raw.substringAfter('.', "").length
        }
        for (i in 1 until values.size) {
            require(values[i] >= values[i - 1]) { "phi_table.json: values must be non-decreasing (index $i)" }
        }
        return PhiTable(
            title = dto.title,
            source = dto.source,
            zMin = dto.zMin,
            step = dto.zStep,
            values = values,
            decimals = decimals,
        )
    }
}
