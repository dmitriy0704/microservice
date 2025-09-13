`13-09-25`

Отправка сообщений о неполадках в Kafka
```kotlin
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
    fun sendUser(user: UserDto, timeoutMs: Long = 5000L) : Boolean {
        val id = UUID.randomUUID().toString()  //val id = "100"
        return try {

            kafkaTemplate.send("user-events", id, user)
                .get(timeoutMs, TimeUnit.MILLISECONDS)
            true
//           .whenComplete { result, ex ->
//                if (ex == null) {
//                    println("✅ Сообщение отправлено: ${result?.producerRecord?.value()}")
//                } else {
//                    println("❌ Ошибка отправки: ${ex.message}")
                //    сюда можно положить сообщение в retry-topic или логировать
//                }
//            }

        } catch (ex: Exception) {
            println("❌ Ошибка отправки в Kafka: ${ex.message}")
            false // брокер недоступен или таймаут
            retryKafkaTemplate.send("user-events-retry", id, user)

        }


//            .thenApply { result ->
//                println("✅ Доставлено: ${result.producerRecord}")
//                result
//            }
//            .thenAccept { result ->
//                println("📊 Метрики: offset=${result.recordMetadata.offset()}")
//            }
//            .exceptionally { ex ->
//                println("❌ Ошибка при отправке: ${ex.message}")
//                null
//            }
    }
    //  }


    /**
     * -> Отправка в несколько топиков:
     * Полезно, если нужно дублировать событие для логирования, аудита или разных микросервисов.
     * key (ID) одинаковый для всех топиков — удобно для корреляции.
     *
     */
    fun sendToMultipleTopics(user: UserDto) {
        val id = UUID.randomUUID().toString()
        val topics = listOf("user-topic", "audit-topic")

        topics.forEach { topic ->
            kafkaTemplate.send(topic, id, user)
            println("Sent user=$user with id=$id to topic $topic")
        }
    }
}
```