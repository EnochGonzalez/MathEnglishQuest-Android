package com.upchiapas.mathenglishquest.domain.dado

import com.upchiapas.mathenglishquest.data.model.AnswerOption
import kotlin.random.Random

/** Arma 3 opciones distintas (la correcta y 2 cercanas) y las mezcla (renderAnswers del prototipo). */
class AnswerOptionsGenerator(private val random: Random = Random.Default) {

    fun generate(correct: Double, allowNegatives: Boolean): List<AnswerOption> {
        val values = linkedSetOf(correct)
        while (values.size < 3) {
            var offset = random.nextInt(1, 6).toDouble()                 // 1..5
            if (correct % 1.0 != 0.0) offset /= 4.0                      // respuestas con decimales: saltos más chicos
            if (random.nextBoolean()) offset = -offset
            var fake = round2(correct + offset)
            if (!allowNegatives && fake < 0) fake = Math.abs(fake)
            values += fake
        }
        return values.shuffled(random).map { AnswerOption(it, NumberFormatter.format(it)) }
    }
}
