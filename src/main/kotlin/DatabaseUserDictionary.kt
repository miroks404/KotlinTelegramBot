package org.example

import java.io.File
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Timestamp

class DatabaseUserDictionary(
    private val username: String,
    private val chatId: Long,
    private val fileName: String = "words.txt",
    private val learningThreshold: Int = NUMBER_THAT_IS_CONSIDERED_MEMORIZED,
) : IUserDictionary {

    init {
        initDB()

        DriverManager.getConnection("jdbc:sqlite:data.db")
            .use { connection ->
                val checkExistUser = connection.prepareStatement("SELECT username FROM users WHERE username = ?")
                checkExistUser.setString(1, username)

                val resultCheckExistUser = checkExistUser.executeQuery()

                if (resultCheckExistUser.next()) {
                    return@use
                } else {
                    val addNewUser = connection.prepareStatement("INSERT INTO users VALUES(?, ?, ?, ?)")
                    addNewUser.setString(2, username)
                    addNewUser.setTimestamp(3, Timestamp(System.currentTimeMillis()))
                    addNewUser.setLong(4, chatId)

                    addNewUser.executeUpdate()

                    val foundNewUserId = connection.prepareStatement("SELECT id FROM users WHERE username = ?")
                    foundNewUserId.setString(1, username)
                    val newUserId = foundNewUserId.executeQuery()

                    if (newUserId.next()) {
                        val userId = newUserId.getLong("id")

                        val foundAllWords = connection.createStatement().executeQuery("SELECT id FROM words")

                        while (foundAllWords.next()) {
                            val id = foundAllWords.getLong("id")

                            val addWord = connection.prepareStatement("INSERT INTO user_answers VALUES(?, ${id}, 0, ?)")
                            addWord.setLong(1, userId)
                            addWord.setTimestamp(2, Timestamp(System.currentTimeMillis()))
                            addWord.executeUpdate()
                        }
                    }
                }
            }
    }

    override fun getNumOfLearnedWords(): Int {
        DriverManager.getConnection("jdbc:sqlite:data.db")
            .use { connection ->
                val foundNumOfLearnedWords = connection.prepareStatement(
                    "SELECT COUNT(user_answers.word_id) FROM user_answers " +
                            "JOIN users ON user_answers.user_id = users.id " +
                            "WHERE users.username = ? AND user_answers.correct_answer_count >= $learningThreshold"
                )
                foundNumOfLearnedWords.setString(1, username)
                val resultFoundNumOfLearnedWords = foundNumOfLearnedWords.executeQuery()

                if (resultFoundNumOfLearnedWords.next()) {
                    return resultFoundNumOfLearnedWords.getInt("COUNT(user_answers.word_id)")
                } else return 0
            }
    }

    override fun getSize(): Int {
        DriverManager.getConnection("jdbc:sqlite:data.db")
            .use { connection ->
                val statement = connection.createStatement()
                val resultSet = statement.executeQuery("SELECT COUNT(*) FROM 'words'")

                if (resultSet.next()) {
                    return resultSet.getInt("COUNT(*)")
                } else {
                    return 0
                }
            }
    }

    override fun getLearnedWords(): List<Word> {
        val listOfWords = mutableListOf<Word>()

        DriverManager.getConnection("jdbc:sqlite:data.db")
            .use { connection ->
                val userId = getUserIdByUsername()

                val foundLearnedWords =
                    connection.prepareStatement(
                        "SELECT * FROM user_answers " +
                                "JOIN words ON user_answers.word_id = words.id " +
                                "WHERE user_id = ? AND correct_answer_count >= $learningThreshold"
                    )
                foundLearnedWords.setLong(1, userId)
                val learnedWords = foundLearnedWords.executeQuery()

                if (learnedWords.next()) {
                    val original = learnedWords.getString("text")
                    val translation = learnedWords.getString("translate")
                    val correctAnswersCount = learnedWords.getInt("correct_answer_count")

                    listOfWords.add(
                        Word(
                            original = original,
                            translation = translation,
                            correctAnswersCount = correctAnswersCount
                        )
                    )
                }

                return listOfWords
            }
    }

    override fun getUnlearnedWords(): List<Word> {
        val listOfUnlearnedWords = mutableListOf<Word>()

        DriverManager.getConnection("jdbc:sqlite:data.db")
            .use { connection ->
                val userId = getUserIdByUsername()

                val foundUnlearnedWords =
                    connection.prepareStatement(
                        "SELECT * FROM user_answers " +
                                "JOIN words ON user_answers.word_id = words.id " +
                                "WHERE user_id = ? AND correct_answer_count < $learningThreshold"
                    )
                foundUnlearnedWords.setLong(1, userId)
                val resultFoundUnlearnedWords = foundUnlearnedWords.executeQuery()

                while (resultFoundUnlearnedWords.next()) {
                    val original = resultFoundUnlearnedWords.getString("text")
                    val translation = resultFoundUnlearnedWords.getString("translate")
                    val correctAnswersCount = resultFoundUnlearnedWords.getInt("correct_answer_count")

                    listOfUnlearnedWords.add(
                        Word(
                            original = original,
                            translation = translation,
                            correctAnswersCount = correctAnswersCount
                        )
                    )
                }

                return listOfUnlearnedWords
            }
    }

    override fun setCorrectAnswersCount(original: String, correctAnswersCount: Int) {
        DriverManager.getConnection("jdbc:sqlite:data.db")
            .use { connection ->
                val foundWordId =
                    connection.prepareStatement("SELECT words.id FROM words WHERE words.text = '$original'")
                        .executeQuery()

                if (foundWordId.next()) {
                    val wordId = foundWordId.getInt("id")

                    val userId = getUserIdByUsername()

                    val updateRow = connection.prepareStatement(
                        "UPDATE user_answers " +
                                "SET correct_answer_count = ? WHERE user_id = ? AND word_id = ? "
                    )
                    updateRow.setInt(1, correctAnswersCount)
                    updateRow.setLong(2, userId)
                    updateRow.setInt(3, wordId)

                    updateRow.executeUpdate()
                }
            }
    }

    override fun resetUserProgress() {
        DriverManager.getConnection("jdbc:sqlite:data.db")
            .use { connection ->
                val userId = getUserIdByUsername()

                val resetProgression =
                    connection.prepareStatement("UPDATE user_answers SET correct_answer_count = 0 WHERE user_id = ?")
                resetProgression.setLong(1, userId)
                resetProgression.executeUpdate()
            }
    }

    private fun initDB() {
        DriverManager.getConnection("jdbc:sqlite:data.db")
            .use { connection ->
                val statement = connection.createStatement()
                statement.executeUpdate(
                    """
                          CREATE TABLE IF NOT EXISTS 'user_answers' (
                            'user_id' integer,
                            'word_id' integer,
                            'correct_answer_count' integer,
                            'updated_at' timestamp
                          );
                          CREATE TABLE IF NOT EXISTS 'users' (
                            'id' integer PRIMARY KEY,
                            'username' varchar,
                            'created_at' timestamp,
                            'chat_id' integer
                          );
    
                          CREATE TABLE IF NOT EXISTS 'words' (
                            'id' integer PRIMARY KEY AUTOINCREMENT,
                            'text' varchar,
                            'translate' varchar
                          );
                    """.trimIndent()
                )
                updateDictionary(File(fileName))
            }
    }

    private fun getUserIdByUsername(): Long {
        DriverManager.getConnection("jdbc:sqlite:data.db")
            .use { connection ->
                val foundUserId = connection.prepareStatement("SELECT id FROM users WHERE username = ?")
                foundUserId.setString(1, username)
                val resultFoundUserId = foundUserId.executeQuery()

                return if (resultFoundUserId.next()) {
                    resultFoundUserId.getLong("id")
                } else {
                    throw SQLException("Пользователь не найден")
                }
            }
    }
}