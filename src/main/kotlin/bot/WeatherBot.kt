package bot

import data.repository.WeatherRepository
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ChatAction
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.extensions.filters.Filter
import com.github.kotlintelegrambot.logging.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import utils.API_KEY
import utils.BOT_ANSWER_TIMEOUT
import utils.BOT_TOKEN
import utils.GIF_WAITING_URL

class WeatherBot(private val weatherRepository: WeatherRepository) {

    private lateinit var country: String
    private var messageId: Long = 0
    private var _chatId: ChatId? = null
    private val chatId: ChatId by lazy { requireNotNull(_chatId) }

    fun createBot() : Bot {
        return bot {
            timeout = BOT_ANSWER_TIMEOUT
            token = BOT_TOKEN
            logLevel = LogLevel.Error

            dispatch {
                setUpCommands()
                setUpCallbacks()
            }
        }
    }

    private fun Dispatcher.setUpCallbacks() {

        callbackQuery(callbackData = "enterManually") {
            bot.sendMessage(
                chatId = chatId,
                text = "Окей, напиши назву свого міста."
            )



            message(Filter.Text) {
                country = message.text.toString()

                val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                    listOf(
                        InlineKeyboardButton.CallbackData(
                            text = "Да, верно.",
                            callbackData = "yes_label"
                        )
                    )
                )

                bot.sendMessage(
                    chatId = chatId,
                    text = "Твой город - $country , верно? \n Если неверно, введи свой город ещё раз",
                    replyMarkup = inlineKeyboardMarkup
                )
            }.toString()

        }

        callbackQuery(callbackData = "yes_label") {
            bot.apply {
                sendAnimation(chatId = chatId, animation = TelegramFile.ByUrl(GIF_WAITING_URL))
                sendMessage(chatId = chatId, text = "Узнаём вашу погоду...")
                sendChatAction(chatId = chatId, action = ChatAction.TYPING)
            }

            CoroutineScope(Dispatchers.IO).launch {
                val currentWeather = weatherRepository.getCurrentWeather(
                    cityName = country,
                    apiKey = API_KEY
                )

                val temperature = currentWeather.main.temp - 273.15


                bot.sendMessage(
                    chatId = chatId,
                    text = """
                        ☁ Хмарність: ${currentWeather.clouds.all}%
                        🌡 Температура: ${(currentWeather.main.temp - 273.15).toInt()}
                        🙎 Відчувається як: ${(currentWeather.main.feels_like - 273.15).toInt()}
                        💧 Вологість: ${currentWeather.main.humidity}%
                        🌪 Швидкість вітру: ${currentWeather.wind.speed} км/год
                        🧭 Тиск: ${currentWeather.main.pressure} 
                    """.trimIndent()
                )
            }
        }

    }

    private fun Dispatcher.setUpCommands() {

        command("start") {
            _chatId = ChatId.fromId(message.chat.id)
            bot.sendMessage(
                chatId = chatId,
                text = "Привіт, Я бот, який вміє показувати погоду! \n Для запуску натисни /weather"
            )
        }

        command("weather") {
            val inlineKeyboardButton = InlineKeyboardMarkup.createSingleButton(
                InlineKeyboardButton.CallbackData(
                    text = "Ввести назву міста",
                    callbackData = "enterManually"
                )
            )

            bot.sendMessage(
                chatId = chatId,
                text = "Мені потрібно знати твоє місто, \n щоб надіслати тобі погоду",
                replyMarkup = inlineKeyboardButton
            )
        }

    }

}