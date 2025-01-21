package org.example

data class Word(
    val original: String,
    val translation: String,
    var correctAnswersCount: Int,
)

data class Question(
    val variants: List<Word>,
    val correctAnswer: Word,
)

interface IUserDictionary {
    fun getNumOfLearnedWords(): Int
    fun getSize(): Int
    fun getLearnedWords(): List<Word>
    fun getUnlearnedWords(): List<Word>
    fun setCorrectAnswersCount(original: String, correctAnswersCount: Int)
    fun resetUserProgress()
}

class LearnWordTrainer(
    private val dictionary: DatabaseUserDictionary,
    private val countOfQuestionWords: Int = 4
) {

    private var question: Question? = null

    fun getStatistic(): String {
        val totalCountOfWords = dictionary.getSize()

        val totalCountOfLearnedWords = dictionary.getNumOfLearnedWords()

        val percentCountOfLearnedWords = (totalCountOfLearnedWords * NUMBER_TO_PERCENTAGE) / totalCountOfWords

        return "Выучено $totalCountOfLearnedWords из $totalCountOfWords слов | ${
            "%.0f".format(
                percentCountOfLearnedWords
            )
        }%"
    }

    fun getNextQuestion(): Question? {
        val notLearnedList = dictionary.getUnlearnedWords()
        if (notLearnedList.isEmpty()) return null

        val questionWords = if (notLearnedList.size < countOfQuestionWords) {
            val learnedList =
                dictionary.getLearnedWords().shuffled()
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
                dictionary.setCorrectAnswersCount(
                    it.correctAnswer.original,
                    it.variants[it.variants.indexOf(it.correctAnswer)].correctAnswersCount + 1
                )
                true
            } else false
        } ?: false

    }

}



