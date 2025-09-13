package dev.folomkin.userservice.service

import dev.folomkin.shared.dto.UserDto
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.TimeUnit


@Service
class UserProducer(
    private val kafkaTemplate: KafkaTemplate<String, UserDto>,
    private val retryKafkaTemplate: KafkaTemplate<String, Any> // отдельный топик для retry

) {
    fun sendUser(user: UserDto) {
        val id = UUID.randomUUID().toString()
        kafkaTemplate.send("user-events", id, user)
            .whenComplete { result, ex ->
                if (ex == null) {
                    println("✅ Отправлено: ${result?.producerRecord?.value()}")
                } else {
                    println("❌ Ошибка при отправке в user-events: ${ex.message}")
                    // fallback в retry-topic
                    kafkaTemplate.send("user-events-retry", id, result?.producerRecord?.value())
                        .whenComplete { retryResult, retryEx ->
                            if (retryEx == null) {
                                println("🔁 Ушло в user-events-retry: $user")
                            } else {
                                println("💥 Не удалось отправить даже в retry: ${retryEx.message}")
                            }
                        }
                }
            }
    }


//        return try {
//            kafkaTemplate.send("user-events", id, user)
//                .get(5, TimeUnit.SECONDS) // максимуqм ждём 5 секунд
//            println("✅ Сообщение отправлено: ${user} с id: ${id}")
//            true
//        } catch (ex: Exception) {
//            println("❌ Ошибка Kafka: ${ex.message}")
//            false
//        }

}