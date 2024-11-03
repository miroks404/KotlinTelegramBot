package org.example

import java.io.File

data class Word(
    val original: String,
    val translation: String,
    val correctAnswersCount: Int,
)

fun main() {

    val wordsFile: File = File("words.txt")

    val dictionary: MutableList<Word> = mutableListOf()

    wordsFile.forEachLine {
        val listOfSplitWords = it.split("|")
        val word = Word(
            listOfSplitWords[0],
            listOfSplitWords[1],
            listOfSplitWords.getOrNull(2)?.toIntOrNull() ?: 0
        )
        dictionary.add(word)
    }

    println(dictionary)
}