package app.philab.common.domain.history.model

enum class CalcKind(val title: String) {
    PHI("Φ(z)"),
    INVERSE_PHI("z по Φ"),
    NORMAL("Нормальная СВ"),
    RECEPTION("Приём"),
    REQUIRED_SNR("Требуемое ОСШ"),
    BLOCK("Пакет / команда"),
    DPSK("ОФТ"),
    MATCHED_FILTER("СФ / коррелятор"),
    CONVERTER("Конвертер ОСШ"),
}

/** A saved calculation shown on the "История" tab. */
data class CalcRecord(
    val id: String,
    val kind: CalcKind,
    val title: String,
    val input: String,
    val result: String,
    val createdAt: Long,
)
