package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

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

        val urlSendMessage = "$URL_API_TELEGRAM$botToken/sendMessage?chat_id=$chatId&text=$text"

        val requestSendMessage = try {
            HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        } catch (e: IllegalArgumentException) {
            HttpRequest.newBuilder()
                .uri(URI.create("$URL_API_TELEGRAM/sendMessage?chat_id=$chatId&text=Пиши%20по%20английски"))
                .build()
        }

        client.send(requestSendMessage, HttpResponse.BodyHandlers.ofString())
    }

}

private const val URL_API_TELEGRAM = "https://api.telegram.org/bot"