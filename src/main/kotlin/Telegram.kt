package org.example

fun main(args: Array<String>) {

    val telegramService = TelegramBotService(args[0])

    var updateId = 0

    val updateIdRegex = "\"update_id\":(.+?),".toRegex()

    val messageTextRegex = "\"text\":\"(.+?)\"".toRegex()

    val chatIdRegex = "\"id\":(.+?),".toRegex()

    while (true) {
        Thread.sleep(2000)
        val updates = telegramService.getUpdates(updateId)
        println(updates)

        val updateIdResult: MatchResult? = updateIdRegex.find(updates)
        updateId = updateIdResult?.groups?.get(1)?.value?.toInt() ?: -1

        if (updateId == -1) continue

        println(updateId)

        updateId++

        val messageTextResult = messageTextRegex.find(updates)
        val messageText = messageTextResult?.groups?.get(1)?.value ?: " "

        println(messageText)

        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value ?: " "

        telegramService.sendMessage(chatId, messageText)
    }

}



