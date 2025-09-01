package dev.folomkin.userservice.service

import dev.folomkin.shared.dto.UserDto
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserProducer(private val kafkaTemplate: KafkaTemplate<String, UserDto>) {
    fun sendUser(user: UserDto) {
        val id = UUID.randomUUID().toString()
        kafkaTemplate.send("user-topic", id, user)
        println("Sent user=$user with id=$id")
    }


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