package org.example

import java.io.File
import java.sql.*

fun main() {
    updateDictionary(File("words.txt"))
}

fun updateDictionary(wordsFile: File) {
    var i = 0
    wordsFile.forEachLine {
        val listOfSplitWords = it.split("|")
        DriverManager.getConnection("jdbc:sqlite:data.db")
            .use { connection ->
                val statement = connection.createStatement()
                statement.executeUpdate("insert or ignore into words values(${++i}, '${listOfSplitWords[0]}', '${listOfSplitWords[1]}')")
            }
    }
}