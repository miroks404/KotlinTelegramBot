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
        val urlGetUpdates = "${Constants.URL_API_TELEGRAM}$botToken/getUpdates?offset=$updateId"

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

        val urlSendMessage = "${Constants.URL_API_TELEGRAM}$botToken/sendMessage?chat_id=$chatId&text=$encoded"

        val requestSendMessage = HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()

        client.send(requestSendMessage, HttpResponse.BodyHandlers.ofString())
    }

    fun sendMenu(chatId: String) : String {

        val sendMessage = "${Constants.URL_API_TELEGRAM}$botToken/sendMessage"

        val sendMenuBody = """
            {
                "chat_id" : $chatId,
                "text" : "Основное меню",
                "reply_markup" : {
                    "inline_keyboard" : [
                        [
                            {
                                "text" : "Изучить слова",
                                "callback_data" : "${Constants.LEARN_WORDS_DATA}"
                            },
                            {
                                "text" : "Статистика",
                                "callback_data" : "${Constants.STATISTICS_DATA}"
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

    fun sendQuestion(
        chatId: String,
        question: Question
    ) : String {
        val sendMessage = "${Constants.URL_API_TELEGRAM}$botToken/sendMessage"

        val variantsString = question.variants
            .mapIndexed { index, word ->
                """
            {
                "text": "${word.translation}",
                "callback_data": "${Constants.CALLBACK_DATA_ANSWER_PREFIX}${index}" 
            }
        """.trimIndent()
            }
            .joinToString(separator = ",")

        val sendMenuBody = """
            {
                "chat_id" : $chatId,
                "text" : "${question.correctAnswer.original}",
                "reply_markup" : {
                    "inline_keyboard" : [
                        [
                            $variantsString
                        ],
                        [
                            {
                                "text": "Меню",
                                "callback_data": "${Constants.MENU_DATA}" 
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
