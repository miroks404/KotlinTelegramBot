package org.example

fun main(args: Array<String>) {

    val trainer = LearnWordTrainer()

    val telegramService = TelegramBotService(args[0])

    var updateId = 0

    val updateIdRegex = "\"update_id\":(.+?),".toRegex()

    val messageTextRegex = "\"text\":\"(.+?)\"".toRegex()

    val chatIdRegex = "\"chat\":\\{\"id\":(.+?),".toRegex()

    val dataRegex = "\"data\":\"(.+?)\"".toRegex()

    var currentQuestion: Question? = null

    while (true) {
        Thread.sleep(2000)
        val updates = telegramService.getUpdates(updateId)
        println(updates)

        val updateIdResult: MatchResult? = updateIdRegex.find(updates)
        updateId = updateIdResult?.groups?.get(1)?.value?.toInt() ?: -1

        if (updateId == -1) continue

        println(updateId)

        updateId++

        val messageText = messageTextRegex.find(updates)?.groups?.get(1)?.value ?: continue
        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value ?: continue
        val data = dataRegex.find(updates)?.groups?.get(1)?.value ?: " "

        if (messageText.lowercase() == "/start") telegramService.sendMenu(chatId)

        when (data) {
            Constants.STATISTICS_DATA -> telegramService.sendMessage(chatId, trainer.getStatistic())
            Constants.LEARN_WORDS_DATA -> {
                currentQuestion = checkNextQuestionAndSend(args[0], trainer, chatId)
            }
            Constants.MENU_DATA -> telegramService.sendMenu(chatId)
        }

        if (data.startsWith(Constants.CALLBACK_DATA_ANSWER_PREFIX)) {
            val userAnswerIndex = data.substringAfter(Constants.CALLBACK_DATA_ANSWER_PREFIX).toInt()
            when (trainer.checkAnswer(userAnswerIndex)) {
                true -> telegramService.sendMessage(chatId, "Правильно!")
                false -> telegramService.sendMessage(
                    chatId,
                    "Неправильно! ${currentQuestion?.correctAnswer?.original} - это ${currentQuestion?.correctAnswer?.translation}"
                )
            }

            currentQuestion = checkNextQuestionAndSend(args[0], trainer, chatId)

        }

    }

}

fun checkNextQuestionAndSend(
    botToken: String,
    trainer: LearnWordTrainer,
    chatId: String
): Question? {

    val telegramService = TelegramBotService(botToken)

    val question = trainer.getNextQuestion()
    if (question == null) telegramService.sendMessage(chatId, "Вы выучили все слова в базе")
    else telegramService.sendQuestion(chatId, question)

    return question
}







