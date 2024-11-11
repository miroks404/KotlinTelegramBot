package org.example

data class Word(
    val original: String,
    val translation: String,
    var correctAnswersCount: Int,
)

fun Question.asConsoleString(): String {
    val variants = this.variants

    return variants.mapIndexed { index, word -> "${index + 1} - ${word.translation}" }
        .joinToString("\n ", " ", "\n--------------\n 0 - Меню")
}

fun main() {

    val trainer = LearnWordTrainer()

    while (true) {
        println(
            """
        Меню:
        1 - Учить слова
        2 - Статистика
        0 - Выход
    """.trimIndent()
        )

        val userChoice = readln().toInt()

        when (userChoice) {
            1 -> println(learnWord())
            2 -> println(trainer.getStatistic())
            0 -> break
            else -> println("Введите число 1, 2 или 0")
        }

        println()
    }

}

fun learnWord(): String {

    val trainer = LearnWordTrainer()

    val question = trainer.getNextQuestion() ?: return "Все слова в словаре выучены"

    println("""
${question.correctAnswer.original}:
${question.asConsoleString()} 
""")

    val userAnswerInput = readln().toInt()

    if (userAnswerInput == 0) return "Выход в меню"

    return if (trainer.checkAnswer(userAnswerInput.minus(1))) {

        "Правильно!"
    } else "Неправильно! ${question.correctAnswer.original} - это ${question.correctAnswer.translation}"
    
}

const val NUMBER_TO_PERCENTAGE = 100.0

const val NUMBER_THAT_IS_CONSIDERED_MEMORIZED = 3