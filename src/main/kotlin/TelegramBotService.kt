package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class TelegramBotService(private val botToken: String) {

    fun getUpdates(updateId: Int): String {
        val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"

        val client: HttpClient = HttpClient.newBuilder().build()

        val requestGetUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
        val responseGetUpdates: HttpResponse<String> =
            client.send(requestGetUpdates, HttpResponse.BodyHandlers.ofString())

        return responseGetUpdates.body()
    }

    fun sendMessage(chatId: String, text: String) {

        val urlSendMessage = "https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=$text"

        val client = HttpClient.newBuilder().build()

        val requestSendMessage = try {
            HttpRequest.newBuilder().uri(URI.create(urlSendMessage)).build()
        } catch (e: IllegalArgumentException) {
            HttpRequest.newBuilder()
                .uri(URI.create("https://api.telegram.org/bot$botToken/sendMessage?chat_id=$chatId&text=Пиши%20по%20английски"))
                .build()
        }

        client.send(requestSendMessage, HttpResponse.BodyHandlers.ofString())
    }

}