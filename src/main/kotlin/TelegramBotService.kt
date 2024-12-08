package org.example

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

@Serializable
data class MenuBody(
    @SerialName("chat_id")
    val chatId: Long,

    @SerialName("text")
    val text: String,

    @SerialName("reply_markup")
    val replyMarkUp: ReplyMarkup,
)

@Serializable
data class ReplyMarkup(
    @SerialName("inline_keyboard")
    val inlineKeyBoard: List<List<InlineKeyboard>>
)

@Serializable
data class InlineKeyboard(
    @SerialName("text")
    val text: String,

    @SerialName("callback_data")
    val callbackData: String,
)

class TelegramBotService(private val botToken: String) {

    private val client = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Long): String {
        val urlGetUpdates = "${Constants.URL_API_TELEGRAM}$botToken/getUpdates?offset=$updateId"

        val requestGetUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val responseGetUpdates: HttpResponse<String> =
            client.send(requestGetUpdates, HttpResponse.BodyHandlers.ofString())

        return responseGetUpdates.body()
    }

    fun sendMessage(chatId: Long, text: String) {

        val encoded = URLEncoder.encode(
            text,
            StandardCharsets.UTF_8
        )

        val urlSendMessage = "${Constants.URL_API_TELEGRAM}$botToken/sendMessage?chat_id=$chatId&text=$encoded"

        val requestSendMessage = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()

        client.send(requestSendMessage, HttpResponse.BodyHandlers.ofString())
    }

    fun sendMenu(json: Json, chatId: Long): String {

        val sendMessage = "${Constants.URL_API_TELEGRAM}$botToken/sendMessage"

        val requestBody = MenuBody(
            chatId = chatId,
            text = "Основное меню",
            replyMarkUp = ReplyMarkup(
                listOf(
                    listOf(
                        InlineKeyboard("Изучить слова", Constants.LEARN_WORDS_DATA),
                        InlineKeyboard("Статистика", Constants.STATISTICS_DATA)
                    )
                )
            )
        )
        val requestBodyString = json.encodeToString(requestBody)

        val request =
            HttpRequest.newBuilder().uri(URI.create(sendMessage))
                .header("Content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
                .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

    fun sendQuestion(
        json: Json,
        chatId: Long,
        question: Question,
    ): String {
        val sendMessage = "${Constants.URL_API_TELEGRAM}$botToken/sendMessage"

        val requestBody = MenuBody(
            chatId = chatId,
            text = question.correctAnswer.original,
            replyMarkUp = ReplyMarkup(
                listOf(
                    question.variants.mapIndexed { index, word ->
                        InlineKeyboard(
                            word.translation,
                            "${Constants.CALLBACK_DATA_ANSWER_PREFIX}$index"
                        )
                    },
                    listOf(InlineKeyboard("Меню", Constants.MENU_DATA))
                ),
            )
        )
        val requestBodyString = json.encodeToString(requestBody)

        val request =
            HttpRequest.newBuilder().uri(URI.create(sendMessage))
                .header("Content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyString))
                .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

}
