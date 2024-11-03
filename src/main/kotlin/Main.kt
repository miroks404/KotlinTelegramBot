package org.example

import java.io.File

data class Word(
    val original: String,
    val translation: String,
    val correctAnswersCount: Int,
)

fun main() {

    val dictionary = loadDictionary()

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
            1 -> println("Учить слова")
            2 -> println("Статистика")
            0 -> break
            else -> println("Введите число 1, 2 или 0")
        }
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