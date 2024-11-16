package org.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun main(args: Array<String>) {

    val botToken = args[0]

    var updateId = 0

    val updateIdRegex = "\"update_id\":(.+?),".toRegex()

    val messageTextRegex = "\"text\":\"(.+?)\"".toRegex()

    while (true) {
        Thread.sleep(2000)
        val updates = getUpdates(botToken, updateId)
        println(updates)

        val updateIdResult: MatchResult? = updateIdRegex.find(updates)
        updateId = updateIdResult?.groups?.get(1)?.value?.toInt()?:-1

        if (updateId == -1) continue

        println(updateId)

        updateId++

        val messageTextResult = messageTextRegex.find(updates)
        val messageText = messageTextResult?.groups?.get(1)?.value

        println(messageText)
    }

}

fun getUpdates(botToken: String, updateId: Int) : String {
    val urlGetUpdates = "https://api.telegram.org/bot$botToken/getUpdates?offset=$updateId"

    val client: HttpClient = HttpClient.newBuilder().build()

    val requestGetUpdates: HttpRequest = HttpRequest.newBuilder().uri(URI.create(urlGetUpdates)).build()
    val responseGetUpdates: HttpResponse<String> = client.send(requestGetUpdates, HttpResponse.BodyHandlers.ofString())

    return responseGetUpdates.body()
}