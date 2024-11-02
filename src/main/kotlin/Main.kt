package org.example

import java.io.File

data class Word(
    val original: String,
    val translate: String,
    val correctAnswersCount: Int = 0,
)

fun main() {

    val wordsFile: File = File("words.txt")

    val dictionary: MutableList<Word> = mutableListOf()

    wordsFile.forEachLine {
        val splitListOfWords = it.split("|")
        val word = Word(
            splitListOfWords[0],
            splitListOfWords[1],
            if (splitListOfWords[2] == "") 0 else splitListOfWords[2].toInt()
        )
        dictionary.add(word)
    }

    println(dictionary)
}