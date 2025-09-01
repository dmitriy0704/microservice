package dev.folomkin.orderservice.service

import dev.folomkin.shared.dto.UserDto
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service

@Service
class UserConsumer {
    private val logger = LoggerFactory.getLogger(UserConsumer::class.java)

    @KafkaListener(topics = ["user-topic"], groupId = "order-service-group")
    fun consume(record: ConsumerRecord<String, UserDto>) {
        val id = record.key()  // здесь ваш UUID
        val user = record.value()
        println("Received user: $user with id: $id")
    }
}