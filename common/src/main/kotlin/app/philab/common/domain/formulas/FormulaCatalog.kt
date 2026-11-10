package app.philab.common.domain.formulas

/** Cheat-sheet of the formulas used in the course (lectures 9–17, practice 1–9). */
object FormulaCatalog {

    data class Formula(val title: String, val body: String, val note: String? = null)

    data class Section(val title: String, val source: String, val formulas: List<Formula>)

    val sections: List<Section> = listOf(
        Section(
            title = "Интеграл вероятности",
            source = "Лекция 10",
            formulas = listOf(
                Formula("Определение", "Φ(z) = 1/√(2π) ∫₋∞ᶻ e^(−x²/2) dx"),
                Formula("Симметрия", "Φ(−z) = 1 − Φ(z),  Φ(0) = 0,5"),
                Formula("Аппроксимация Осипова (z ≥ 0)", "Φ(z) ≈ 1 − ½·10^(−z(0,34 + 0,16z))", "погрешность ≤ 0,2 %"),
                Formula("Запись таблицы", "0,9³8215 = 0,9998215", "верхний индекс — число девяток"),
                Formula("Нормальная СВ N(m, D)", "P(X > a) = 1 − Φ((a − m)/√D),  P(X < a) = Φ((a − m)/√D)"),
            ),
        ),
        Section(
            title = "Оптимальный приёмник (общий случай)",
            source = "Лекции 9–10",
            formulas = listOf(
                Formula("Решающая статистика", "l = (2/N₀)∫₀ᵀ r(t)[s₁(t) − s₀(t)]dt  ≷  γ"),
                Formula("Порог", "γ = (E₁ − E₀)/N₀ + ln[p₀(C₁₀ − C₀₀) / p₁(C₀₁ − C₁₁)]"),
                Formula("Коэффициент корреляции", "ρ = 1/√(E₁E₀) ∫₀ᵀ s₁(t)s₀(t)dt"),
                Formula("Среднее при λ₁", "m₁ = 2E₁/N₀·(1 − √(E₀/E₁)·ρ)"),
                Formula("Среднее при λ₀", "m₀ = −2E₀/N₀·(1 − √(E₁/E₀)·ρ)"),
                Formula("Дисперсия", "σ² = 2(E₁ + E₀)/N₀ − 4√(E₁E₀)·ρ/N₀"),
                Formula("Ошибки 1-го и 2-го рода", "α = 1 − Φ((γ − m₀)/σ),  β = Φ((γ − m₁)/σ)"),
                Formula("Полная вероятность ошибки", "Pe = p₀α + p₁β"),
                Formula("Идеальный наблюдатель, p₀ = p₁", "Pe = 1 − Φ(√((E₁ + E₀ − 2√(E₁E₀)ρ)/(2N₀)))"),
            ),
        ),
        Section(
            title = "Когерентный приём АТ / ЧТ / ФТ",
            source = "Лекции 10–11",
            formulas = listOf(
                Formula("АТ", "Pe = 1 − Φ(√(E/2N₀)),  γ = E/N₀ (p₀ = p₁)"),
                Formula("ЧТ, |ω₁ − ω₀|T ≫ 1", "Pe = 1 − Φ(√(E/N₀)),  γ = 0;  ρ = sin(ΔT)/ΔT"),
                Formula("ФТ (Δφ = π)", "Pe = 1 − Φ(√(2E/N₀)),  γ = 0,  ρ = −1"),
                Formula("ФТ с девиацией Δφ", "ρ = cos Δφ;  проигрыш к ФТ = 10·lg(2/(1 − cos Δφ)) дБ"),
            ),
        ),
        Section(
            title = "Нейман–Пирсон",
            source = "Лекция 11",
            formulas = listOf(
                Formula("Ложная тревога", "P_F = α = 1 − Φ(γ/√(2E/N₀))"),
                Formula("Правильное обнаружение", "P_D = 1 − β = 1 − Φ(γ/√(2E/N₀) − √(2E/N₀))"),
                Formula("Порядок решения", "по α находим γ, затем P_D (или Pe при известных p₀, p₁)"),
            ),
        ),
        Section(
            title = "Некогерентный приём",
            source = "Лекции 13–14",
            formulas = listOf(
                Formula("АТ, точно (p₀ = p₁)", "Pe = ½[1 + e^(−γ*²/2) − Q(√(2E/N₀), γ*)]"),
                Formula("Порог АТ", "γ* = Arg ln I₀(2E/N₀ + ln p₀/p₁)·N₀/(2A)", "в нормированных единицах: Arg ln I₀(E/N₀ + ln p₀/p₁)/√(2E/N₀)"),
                Formula("Функция Маркума", "Q(v, u) = ∫ᵤ^∞ x·e^(−(x² + v²)/2)·I₀(xv) dx"),
                Formula("АТ, большое ОСШ", "Pe ≈ ½·e^(−(2E/N₀)/8)"),
                Formula("ЧТ", "Pe = ½·e^(−(2E/N₀)/4)"),
                Formula("ФТ", "оптимальный некогерентный приём невозможен → ОФТ", "лекция 15"),
            ),
        ),
        Section(
            title = "ОФТ",
            source = "Лекции 15–16",
            formulas = listOf(
                Formula("Кодирование", "λ = 1 — фаза как на предыдущем такте; λ = 0 — смена фазы на π"),
                Formula("Декодирование", "Φₖ − Φₖ₋₁ = 0 ⇒ λ = 1;  = π ⇒ λ = 0"),
                Formula("Сравнение фаз", "U_ФД = cos[ω₀T − (Φₖ − Φₖ₋₁)],  нужно ω₀T = nπ"),
                Formula("Особенность", "спаривание ошибок; первый символ теряется"),
            ),
        ),
        Section(
            title = "Согласованный фильтр",
            source = "Лекции 8–9, практика 1, 8",
            formulas = listOf(
                Formula("Максимум ОСШ на выходе СФ", "q_max = 2E/N₀"),
                Formula("Рассогласование по длительности", "q(T) = 2E₀/N₀ · min(T, T₀)²/(T·T₀)"),
                Formula("Расстройка фазы коррелятора", "q(φ) = q_max·cos²φ"),
            ),
        ),
        Section(
            title = "Поэлементный приём слов",
            source = "Практика 7–8",
            formulas = listOf(
                Formula("Не более k ошибок из N", "P = Σᵢ₌₀ᵏ C(N, i)·pⁱ·(1 − p)^(N−i)"),
                Formula("Все N верно", "P = (1 − p)^N"),
                Formula("Повторы команды", "1 − (1 − P)ⁿ ≥ P_треб ⇒ n = ⌈ln(1 − P_треб)/ln(1 − P)⌉"),
                Formula("Срабатывание по шумам (ЧТ)", "каждый элемент угадывается с вероятностью ½ ⇒ (½)^N"),
            ),
        ),
        Section(
            title = "ОСШ и децибелы",
            source = "",
            formulas = listOf(
                Formula("Перевод", "q[дБ] = 10·lg q,  q = 10^(q[дБ]/10)"),
                Formula("Связь величин", "2E/N₀ = 2·(E/N₀),  амплитудное отношение √(2E/N₀)"),
            ),
        ),
    )
}
