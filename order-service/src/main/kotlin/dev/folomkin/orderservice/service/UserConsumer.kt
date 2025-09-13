package dev.folomkin.orderservice.service

import dev.folomkin.shared.dto.UserDto
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class UserConsumer(val kafkaTemplate: KafkaTemplate<String, UserDto>) {
    private val logger = LoggerFactory.getLogger(UserConsumer::class.java)

    @KafkaListener(
        topics = ["user-events"],
        groupId = "order-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun consume(record: ConsumerRecord<String, UserDto>) {

        try {
//            if (record.value().name == "error") {
//                throw RuntimeException("Test error")
//            }
            val id = record.key()  // здесь ваш UUID
            val user = record.value()
            println("✅ Получено: $user with id: $id")
            // тут бизнес-логика обработки

            if(user.name == "FAIL"){
                logger.error("❌ Имитация ошибки")
                println("❌ Имитация ошибки")
                throw RuntimeException("Имитируем сбой обработки")
            }

        } catch (ex: Exception) {
            println("⚠️ Ошибка обработки, отправляем в retry-topic: ${ex.message}")
            kafkaTemplate.send("user-events-retry", record.key(), record.value())
        }
    }

    @KafkaListener(topics = ["user-events-retry"], groupId = "retry-service-group")
    fun retry(record: ConsumerRecord<String, UserDto>) {
        try {
            println("♻️ Retry обработка: ${record.value()}")
            kafkaTemplate.send("user-events", record.key(), record.value()) // снова пробуем основной топик
        } catch (ex: Exception) {
            println("❌ Ошибка в retry, помещаем в DLT: ${ex.message}")
            kafkaTemplate.send("user-events-dlt", record.key(), record.value())
        }
    }


    @KafkaListener(topics = ["user-events-dlt"], groupId = "dlt-service-group")
    fun consumeFailed(record: ConsumerRecord<String, UserDto>) {
        println("💀 Сообщение в DLT: ${record.value()}")
        // тут можно логировать, алертить, сохранять в БД
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