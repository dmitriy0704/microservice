package dev.folomkin.orderservice.service

import dev.folomkin.shared.dto.UserDto
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class UserConsumer {

    private val logger = LoggerFactory.getLogger(UserConsumer::class.java)

    @KafkaListener(topics = ["user-topic"], groupId = "order-group")
    fun consumeUser(userDto: UserDto) {
        logger.info("Получен UserDto: {}", userDto)
    }
}