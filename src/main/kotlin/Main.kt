package org.example

import java.io.File

data class Word(
    val original: String,
    val translation: String,
    var correctAnswersCount: Int,
)

fun main() {

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
            2 -> println(getStatistic())
            0 -> break
            else -> println("Введите число 1, 2 или 0")
        }

        println()
    }

}

fun loadDictionary(): MutableList<Word> {
    val dictionary: MutableList<Word> = mutableListOf()

    val wordsFile = File("words.txt")

    wordsFile.forEachLine {
        val listOfSplitWords = it.split("|")
        val word = Word(
            listOfSplitWords[0],
            listOfSplitWords[1],
            listOfSplitWords.getOrNull(2)?.toIntOrNull() ?: 0
        )
        dictionary.add(word)
    }

    return dictionary
}

fun saveDictionary(dictionary: MutableList<Word>) {
    val wordsFile = File("words.txt")

    wordsFile.writeText("")

    dictionary.forEach { wordsFile.appendText("${it.original}|${it.translation}|${it.correctAnswersCount}\n") }
}

fun getStatistic(): String {
    val dictionary = loadDictionary()

    val totalCountOfWords = dictionary.count()

    val totalListOfLearnedWords = dictionary.filter { it.correctAnswersCount >= NUMBER_THAT_IS_CONSIDERED_MEMORIZED }
    val totalCountOfLearnedWords = totalListOfLearnedWords.count()

    val percentCountOfLearnedWords = (totalCountOfLearnedWords * NUMBER_TO_PERCENTAGE) / totalCountOfWords

    return "Выучено $totalCountOfLearnedWords из $totalCountOfWords слов | ${"%.0f".format(percentCountOfLearnedWords)}%"
}


fun learnWord(): String {

    val dictionary = loadDictionary()

    val notLearnedList = dictionary.filter { it.correctAnswersCount < NUMBER_THAT_IS_CONSIDERED_MEMORIZED }

    if (notLearnedList.isEmpty()) return "Все слова в словаре выучены"

    var questionWords: List<Word> = notLearnedList.shuffled().take(4)

    val correctAnswer: Word = questionWords.first()

    questionWords = questionWords.shuffled()

    println("""
${correctAnswer.original}:
${
        questionWords.mapIndexed { index, word -> "${index + 1} - ${word.translation}" }
            .joinToString("\n ", " ", "\n--------------\n 0 - Меню")
    }
""")

    val userAnswerInput = readln().toInt()

    if (userAnswerInput == 0) return "Выход в меню"

    val correctAnswerId = questionWords.indexOf(correctAnswer)

    return if (userAnswerInput == correctAnswerId + 1) {
        dictionary[dictionary.indexOf(correctAnswer)].correctAnswersCount++
        saveDictionary(dictionary)
        "Правильно!"
    } else "Неправильно! ${correctAnswer.original} - это ${correctAnswer.translation}"
    
}

const val NUMBER_TO_PERCENTAGE = 100.0

const val NUMBER_THAT_IS_CONSIDERED_MEMORIZED = 3