import bot.WeatherBot
import data.api.RetrofitInstance
import data.repository.WeatherRepository

fun main(args: Array<String>) {
    println("Hello World!")

    val weatherApi = RetrofitInstance.api
    val weatherRepository = WeatherRepository(weatherApi)
    val weatherBot = WeatherBot(weatherRepository = weatherRepository).createBot()
    weatherBot.startPolling()

}