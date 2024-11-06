package org.example

import java.io.File

data class Word(
    val original: String,
    val translation: String,
    val correctAnswersCount: Int,
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

fun getStatistic(): String {
    val dictionary = loadDictionary()

    val totalCountOfWords = dictionary.count()

    val totalListOfLearnedWords = dictionary.filter { it.correctAnswersCount >= 3 }
    val totalCountOfLearnedWords = totalListOfLearnedWords.count()

    val percentCountOfLearnedWords = (totalCountOfLearnedWords * NUMBER_TO_PERCENTAGE) / totalCountOfWords

    return "Выучено $totalCountOfLearnedWords из $totalCountOfWords слов | ${"%.0f".format(percentCountOfLearnedWords)}%"
}


fun learnWord(): String {
    println()

    val notLearnedList = loadDictionary().filter { it.correctAnswersCount < 3 }

    if (notLearnedList.isEmpty()) return "Все слова в словаре выучены"

    var questionWords: List<Word> = notLearnedList.shuffled().take(4)

    val correctAnswer: Word = questionWords.first()

    questionWords = questionWords.shuffled()

    return """
${correctAnswer.original}:
${questionWords.mapIndexed { index, word -> "${index + 1} - ${word.translation}" }
       .joinToString("\n ", " ")}
"""
}

const val NUMBER_TO_PERCENTAGE = 100.0