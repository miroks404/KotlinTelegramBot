package org.example

import java.io.File

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

class LearnWordTrainer(private val countOfQuestionWords: Int = 4) {

    private var question: Question? = null
    private val dictionary = loadDictionary()

    fun getStatistic(): String {
        val dictionary = loadDictionary()

        val totalCountOfWords = dictionary.count()

        val totalListOfLearnedWords =
            dictionary.filter { it.correctAnswersCount >= NUMBER_THAT_IS_CONSIDERED_MEMORIZED }
        val totalCountOfLearnedWords = totalListOfLearnedWords.count()

        val percentCountOfLearnedWords = (totalCountOfLearnedWords * NUMBER_TO_PERCENTAGE) / totalCountOfWords

        return "Выучено $totalCountOfLearnedWords из $totalCountOfWords слов | ${
            "%.0f".format(
                percentCountOfLearnedWords
            )
        }%"
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.filter { it.correctAnswersCount < NUMBER_THAT_IS_CONSIDERED_MEMORIZED }
        if (notLearnedList.isEmpty()) return null

        val questionWords = if (notLearnedList.size < countOfQuestionWords) {
            val learnedList =
                dictionary.filter { it.correctAnswersCount >= NUMBER_THAT_IS_CONSIDERED_MEMORIZED }.shuffled()
            notLearnedList.shuffled()
                .take(countOfQuestionWords) + learnedList.take(countOfQuestionWords - notLearnedList.size)
        } else {
            notLearnedList.shuffled().take(countOfQuestionWords)
        }.shuffled()

        val correctAnswer: Word = questionWords.first()

        question = Question(
            variants = questionWords.shuffled(),
            correctAnswer = correctAnswer,
        )

        return question
    }

    fun checkAnswer(userAnswerIndex: Int): Boolean {
        return question?.let {
            val correctAnswerIndex = it.variants.indexOf(it.correctAnswer)

            if (correctAnswerIndex == userAnswerIndex) {
                it.variants[it.variants.indexOf(it.correctAnswer)].correctAnswersCount++
                saveDictionary(dictionary)
                true
            } else false
        } ?: false

    }

    private fun loadDictionary(): MutableList<Word> {
        try {
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
        } catch (e: IndexOutOfBoundsException) {
            throw IllegalStateException("Некорректный файл")
        }
    }

    private fun saveDictionary(dictionary: MutableList<Word>) {
        val wordsFile = File("words.txt")

        wordsFile.writeText("")

        dictionary.forEach { wordsFile.appendText("${it.original}|${it.translation}|${it.correctAnswersCount}\n") }
    }

}