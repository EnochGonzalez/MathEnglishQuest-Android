package com.upchiapas.mathenglishquest.domain.dado

import java.math.BigDecimal

/** Redondeo a 2 decimales (round2 del prototipo). */
fun round2(value: Double): Double = Math.round(value * 100) / 100.0

object NumberFormatter {
    /** 5.0 -> "5", 0.75 -> "0.75", 2.5 -> "2.5", -3.0 -> "-3" */
    fun format(value: Double): String {
        val rounded = round2(value)
        return if (rounded % 1.0 == 0.0) {
            rounded.toLong().toString()
        } else {
            BigDecimal.valueOf(rounded).stripTrailingZeros().toPlainString()
        }
    }
}
