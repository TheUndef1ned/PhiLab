package app.philab.common.domain.shared.parse

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Tiny calculator for input fields, so tasks can be typed as given:
 * "16", "0,25", "1e-3", "10^-4", "1/4", "3/7", "√5", "sqrt(5)", "2*8", "π/3", "ln(0.1/0.9)".
 *
 * Grammar: expr := term (('+'|'−') term)* ; term := power (('*'|'/') power)* ;
 *          power := unary ('^' power)? ; unary := ('+'|'−'|'√') unary | primary
 */
object ExpressionParser {

    fun parse(text: String): Double? {
        val source = normalize(text)
        if (source.isEmpty()) return null
        return try {
            val parser = Parser(source)
            val value = parser.expression()
            if (parser.pos != source.length) null else value.takeIf { it.isFinite() }
        } catch (_: RuntimeException) {
            null
        }
    }

    private fun normalize(text: String): String = text.trim()
        .replace(',', '.')
        .replace('−', '-')
        .replace('–', '-')
        .replace('×', '*')
        .replace('·', '*')
        .replace(':', '/')
        .replace(" ", "")
        .lowercase()

    private class Parser(private val s: String) {
        var pos = 0

        fun expression(): Double {
            var v = term()
            while (pos < s.length) {
                when (s[pos]) {
                    '+' -> { pos++; v += term() }
                    '-' -> { pos++; v -= term() }
                    else -> return v
                }
            }
            return v
        }

        private fun term(): Double {
            var v = power()
            while (pos < s.length) {
                when (s[pos]) {
                    '*' -> { pos++; v *= power() }
                    '/' -> { pos++; v /= power() }
                    else -> return v
                }
            }
            return v
        }

        private fun power(): Double {
            val base = unary()
            if (pos < s.length && s[pos] == '^') {
                pos++
                return base.pow(power())
            }
            return base
        }

        private fun unary(): Double {
            if (pos >= s.length) error("unexpected end")
            return when (s[pos]) {
                '-' -> { pos++; -unary() }
                '+' -> { pos++; unary() }
                '√' -> { pos++; sqrt(unary()) }
                else -> primary()
            }
        }

        private fun primary(): Double {
            val c = s[pos]
            if (c == '(') {
                pos++
                val v = expression()
                expect(')')
                return v
            }
            if (c == 'π') { pos++; return PI }
            if (c.isDigit() || c == '.') return number()
            if (c.isLetter()) {
                val start = pos
                while (pos < s.length && s[pos].isLetter()) pos++
                val name = s.substring(start, pos)
                if (name == "pi") return PI
                if (name == "e" && (pos >= s.length || s[pos] != '(')) return kotlin.math.E
                expect('(')
                val arg = expression()
                expect(')')
                return when (name) {
                    "sqrt" -> sqrt(arg)
                    "ln" -> ln(arg)
                    "lg", "log" -> log10(arg)
                    "exp" -> exp(arg)
                    else -> error("unknown function $name")
                }
            }
            error("unexpected '$c'")
        }

        private fun number(): Double {
            val start = pos
            while (pos < s.length && (s[pos].isDigit() || s[pos] == '.')) pos++
            if (pos < s.length && s[pos] == 'e') {
                val save = pos
                pos++
                if (pos < s.length && (s[pos] == '+' || s[pos] == '-')) pos++
                if (pos < s.length && s[pos].isDigit()) {
                    while (pos < s.length && s[pos].isDigit()) pos++
                } else {
                    pos = save
                }
            }
            return s.substring(start, pos).toDouble()
        }

        private fun expect(c: Char) {
            if (pos >= s.length || s[pos] != c) error("expected '$c'")
            pos++
        }
    }
}
