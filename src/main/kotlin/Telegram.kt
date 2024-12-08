package org.example

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Response(
    @SerialName("result")
    val result: List<Update>,
)

@Serializable
data class Update(
    @SerialName("update_id")
    val updateId: Long,

    @SerialName("message")
    val message: Message? = null,

    @SerialName("callback_query")
    val callbackQuery: CallbackQuery? = null,
)

@Serializable
data class Message(
    @SerialName("text")
    val text: String,

    @SerialName("chat")
    val chat: Chat,
)

@Serializable
data class CallbackQuery(
    @SerialName("data")
    val data: String? = null,

    @SerialName("message")
    val message: Message? = null,
)

@Serializable
data class Chat(
    @SerialName("id")
    val chatId: Long,
)

fun main(args: Array<String>) {

    val jsonWithIgnoreKeys = Json { ignoreUnknownKeys = true }

    val trainer = LearnWordTrainer()

    val telegramService = TelegramBotService(args[0])

    var updateId = 0L

    var currentQuestion: Question? = null

    while (true) {
        Thread.sleep(2000)
        val updates = telegramService.getUpdates(updateId)
        println(updates)

        val update = jsonWithIgnoreKeys.decodeFromString<Response>(updates).result.firstOrNull() ?: continue
        updateId = update.updateId

        println(updateId)

        updateId++

        val messageText = update.message?.text
        val chatId = update.message?.chat?.chatId ?: update.callbackQuery?.message?.chat?.chatId ?: continue
        val data = update.callbackQuery?.data ?: " "

        println(messageText)

        println(chatId)

        println(data)

        if (messageText?.lowercase() == "/start")
            telegramService.sendMenu(jsonWithIgnoreKeys ,chatId)

        when (data) {
            Constants.STATISTICS_DATA -> telegramService.sendMessage(chatId, trainer.getStatistic())
            Constants.LEARN_WORDS_DATA -> {
                currentQuestion = checkNextQuestionAndSend(jsonWithIgnoreKeys ,telegramService, trainer, chatId)
            }

            Constants.MENU_DATA -> telegramService.sendMenu(jsonWithIgnoreKeys ,chatId)
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

            currentQuestion = checkNextQuestionAndSend(jsonWithIgnoreKeys ,telegramService, trainer, chatId)

        }
    }

}


fun checkNextQuestionAndSend(
    json: Json,
    telegramService: TelegramBotService,
    trainer: LearnWordTrainer,
    chatId: Long
): Question? {

    val question = trainer.getNextQuestion()
    if (question == null) telegramService.sendMessage(chatId, "Вы выучили все слова в базе")
    else telegramService.sendQuestion(json ,chatId, question)

    return question
}







