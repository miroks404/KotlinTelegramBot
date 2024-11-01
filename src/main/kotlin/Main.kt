package org.example

import java.io.File

fun main() {

    val wordsFile: File = File("words.txt")
    wordsFile.createNewFile()
    wordsFile.writeText("hello привет")
    wordsFile.appendText("\ndog собака")
    wordsFile.appendText("\ncat кошка")

    for (i in wordsFile.readLines()) {
        println(i)
    }
}