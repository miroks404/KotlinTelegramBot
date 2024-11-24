package org.example

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

class TelegramBotService(private val botToken: String) {

    private val client = HttpClient.newBuilder().build()

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "$URL_API_TELEGRAM$botToken/getUpdates?offset=$updateId"

        val requestGetUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val responseGetUpdates: HttpResponse<String> =
            client.send(requestGetUpdates, HttpResponse.BodyHandlers.ofString())

        return responseGetUpdates.body()
    }

    fun sendMessage(chatId: String, text: String) {

        val encoded = URLEncoder.encode(
            text,
            StandardCharsets.UTF_8
        )

        println(encoded)

        val urlSendMessage = "$URL_API_TELEGRAM$botToken/sendMessage?chat_id=$chatId&text=$encoded"

        val requestSendMessage = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()

        client.send(requestSendMessage, HttpResponse.BodyHandlers.ofString())
    }

    fun sendMenu(chatId: String) : String {

        val sendMessage = "$URL_API_TELEGRAM$botToken/sendMessage"

        val sendMenuBody = """
            {
                "chat_id" : $chatId,
                "text" : "Основное меню",
                "reply_markup" : {
                    "inline_keyboard" : [
                        [
                            {
                                "text" : "Изучить слова",
                                "callback_data" : "$LEARN_WORDS_DATA"
                            },
                            {
                                "text" : "Статистика",
                                "callback_data" : "$STATISTICS_DATA"
                            }
                        ]
                    ]
                }
            }
        """.trimIndent()

        val request =
            HttpRequest.newBuilder().uri(URI.create(sendMessage))
                .header("Content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(sendMenuBody))
                .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        return response.body()
    }

}

private const val URL_API_TELEGRAM = "https://api.telegram.org/bot"
private const val LEARN_WORDS_DATA = "learn_words_clicked"
private const val STATISTICS_DATA = "statistics_clicked"