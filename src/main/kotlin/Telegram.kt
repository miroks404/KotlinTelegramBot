package org.example

fun main(args: Array<String>) {

    val trainer = LearnWordTrainer()

    val telegramService = TelegramBotService(args[0])

    var updateId = 0

    val updateIdRegex = "\"update_id\":(.+?),".toRegex()

    val messageTextRegex = "\"text\":\"(.+?)\"".toRegex()

    val chatIdRegex = "\"chat\":\\{\"id\":(.+?),".toRegex()

    val dataRegex = "\"data\":\"(.+?)\"".toRegex()

    while (true) {
        Thread.sleep(2000)
        val updates = telegramService.getUpdates(updateId)
        println(updates)

        val updateIdResult: MatchResult? = updateIdRegex.find(updates)
        updateId = updateIdResult?.groups?.get(1)?.value?.toInt() ?: -1

        if (updateId == -1) continue

        println(updateId)

        updateId++

        val messageText = messageTextRegex.find(updates)?.groups?.get(1)?.value ?: " "
        val chatId = chatIdRegex.find(updates)?.groups?.get(1)?.value ?: " "
        val data = dataRegex.find(updates)?.groups?.get(1)?.value ?: " "

        if (messageText.lowercase() == "/start") telegramService.sendMenu(chatId)

        if (data.lowercase() == "statistics_clicked")
            telegramService.sendMessage(chatId, "Выучено 10 из 10 слов | 100%")
    }

}



