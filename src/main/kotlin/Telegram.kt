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

    val trainers = HashMap<Long, LearnWordTrainer>()

    val telegramService = TelegramBotService(args[0])

    var updateId = 0L

    val currentQuestion = HashMap<Long, Question?>()

    while (true) {
        Thread.sleep(2000)
        val updates = telegramService.getUpdates(updateId)
        println(updates)

        val update = jsonWithIgnoreKeys.decodeFromString<Response>(updates)

        if (update.result.isEmpty()) continue

        val sortedUpdates = update.result.sortedBy { it.updateId }

        sortedUpdates.forEach {
            handleUpdate(jsonWithIgnoreKeys, it, trainers, telegramService, currentQuestion)
        }
        updateId = sortedUpdates.last().updateId + 1

    }

}

fun handleUpdate(
    json: Json,
    update: Update,
    trainers: HashMap<Long, LearnWordTrainer>,
    telegramService: TelegramBotService,
    currentQuestion: HashMap<Long, Question?>
) {
    val messageText = update.message?.text
    val chatId = update.message?.chat?.chatId ?: update.callbackQuery?.message?.chat?.chatId ?: return
    val data = update.callbackQuery?.data ?: " "

    val trainer = trainers.getOrPut(chatId) { LearnWordTrainer("$chatId.txt") }

    println(messageText)

    println(chatId)

    println(data)

    if (messageText?.lowercase() == "/start")
        telegramService.sendMenu(json, chatId)

    when (data) {
        Constants.STATISTICS_DATA -> telegramService.sendMessage(chatId, trainer.getStatistic())
        Constants.LEARN_WORDS_DATA -> {
            currentQuestion[chatId] = checkNextQuestionAndSend(json, telegramService, trainer, chatId) ?: return
        }

        Constants.MENU_DATA -> telegramService.sendMenu(json, chatId)
        Constants.RESET_DATA -> {
            trainer.resetProgress()
            telegramService.sendMessage(chatId, "Прогресс сброшен!")
        }
    }

    if (data.startsWith(Constants.CALLBACK_DATA_ANSWER_PREFIX)) {
        val userAnswerIndex = data.substringAfter(Constants.CALLBACK_DATA_ANSWER_PREFIX).toInt()
        when (trainer.checkAnswer(userAnswerIndex)) {
            true -> telegramService.sendMessage(chatId, "Правильно!")
            false -> telegramService.sendMessage(
                chatId,
                "Неправильно! ${currentQuestion[chatId]?.correctAnswer?.original} - это ${currentQuestion[chatId]?.correctAnswer?.translation}"
            )
        }

        currentQuestion[chatId] = checkNextQuestionAndSend(json, telegramService, trainer, chatId) ?: return

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
    else telegramService.sendQuestion(json, chatId, question)

    return question
}







