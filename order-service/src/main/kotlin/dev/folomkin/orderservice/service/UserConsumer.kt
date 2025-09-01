package dev.folomkin.orderservice.service

import dev.folomkin.shared.dto.UserDto
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service

@Service
class UserConsumer(val kafkaTemplate: KafkaTemplate<String, UserDto>) {
    private val logger = LoggerFactory.getLogger(UserConsumer::class.java)

    @KafkaListener(
        topics = ["user-topic"],
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun consume(record: ConsumerRecord<String, UserDto>) {

        try {
            if (record.value().name == "error") {
                throw RuntimeException("Test error")
            }
            val id = record.key()  // здесь ваш UUID
            val user = record.value()
            println("Received user: $user with id: $id")
            println("Processed user: ${record.value()}")
        } catch (e: Exception) {
            logger.error(e.message, e)

            println("⚠ Error: ${e.message}, sending to DLQ...")
            // просто отправляем в DLQ вручную
            kafkaTemplate.send(
                "${record.topic()}.DLT",
                record.key(),
                record.value()
            )
        }
    }


    /**
     * Фильтрация сообщений прямо в @KafkaListener
     *
     * Можно фильтровать по любому полю, либо по ключу (record.key()).
     * Альтернатива — использовать RecordFilterStrategy в
     * ConcurrentKafkaListenerContainerFactory для автоматического отбрасывания
     * сообщений до вызова listener.
     *
     * Пример фильтра на уровне фабрики:
     * factory.setRecordFilterStrategy { record: ConsumerRecord<String, UserDto> ->
     *     !record.value().name.startsWith("A") // true → сообщение отбросится
     * }
     *
     */

//    @KafkaListener(topics = ["user-topic"], groupId = "order-service-group")
    fun consumeFiltered(record: ConsumerRecord<String, UserDto>) {
        val user = record.value()
        if (user.name.startsWith("A")) {
            println("Processing user starting with A: $user")
        } else {
            println("Skipping user: $user")
        }
    }
}