package org.example

import java.io.File

class FileUserDictionary(
    private val fileName: String = "words.txt",
    private val learningThreshold: Int = NUMBER_THAT_IS_CONSIDERED_MEMORIZED,
) : IUserDictionary {

    private val dictionary = try {
        loadDictionary()
    } catch (e: Exception) {
        throw IllegalArgumentException("Некорректный файл")
    }

    override fun getNumOfLearnedWords(): Int = dictionary.count { it.correctAnswersCount >= learningThreshold }

    override fun getSize(): Int = dictionary.size

    override fun getLearnedWords(): List<Word> {
        val listOfWords = mutableListOf<Word>()
        dictionary.forEach {
            listOfWords.add(it)
        }
        return listOfWords.toList()
    }

    override fun getUnlearnedWords(): List<Word> {
        val listOfUnlearnedWords = mutableListOf<Word>()
        dictionary.forEach {
            if (it.correctAnswersCount < learningThreshold) {
                listOfUnlearnedWords.add(it)
            }
        }
        return listOfUnlearnedWords.toList()
    }

    override fun setCorrectAnswersCount(original: String, correctAnswersCount: Int) {
        dictionary.find { it.original == original }?.correctAnswersCount = correctAnswersCount
        saveDictionary()
    }

    override fun resetUserProgress() {
        dictionary.forEach { it.correctAnswersCount = 0 }
        saveDictionary()
    }

    private fun loadDictionary(): List<Word> {
        try {
            val dictionary: MutableList<Word> = mutableListOf()

            val wordsFile = File(fileName)
            if (!wordsFile.exists()) File("words.txt").copyTo(wordsFile)

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
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("Некорректный файл")
        }
    }

    private fun saveDictionary() {
        val wordsFile = File(fileName)

        wordsFile.writeText("")

        dictionary.forEach { wordsFile.appendText("${it.original}|${it.translation}|${it.correctAnswersCount}\n") }
    }
}