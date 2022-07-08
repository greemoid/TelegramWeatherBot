import bot.WeatherBot
import data.api.RetrofitInstance
import data.repository.WeatherRepository

fun main(args: Array<String>) {
    
    val weatherApi = RetrofitInstance.api
    val weatherRepository = WeatherRepository(weatherApi)
    val weatherBot = WeatherBot(weatherRepository = weatherRepository).createBot()
    weatherBot.startPolling()

}
