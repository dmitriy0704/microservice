`13-09-25`

Кастомная настройка продюсера

```kotlin
package dev.folomkin.userservice.config

import dev.folomkin.shared.dto.UserDto
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@Configuration
@EnableKafka
class ProducerConfig {

//    @Bean
//    fun producerFactory(): ProducerFactory<String, UserDto> {
//        val config = mapOf(
//            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
//            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
//            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,

    // идемпотентность
//            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true,
//            ProducerConfig.ACKS_CONFIG to "all",
//            ProducerConfig.RETRIES_CONFIG to Int.MAX_VALUE,
//            ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION to 5, // совместимо с идемпотентностью

    // транзакции
//            ProducerConfig.TRANSACTIONAL_ID_CONFIG to "user-service-tx-1"
//        )
//        val factory = DefaultKafkaProducerFactory<String, UserDto>(config)
//        factory.transactionCapable = true
//        factory.setTransactionIdPrefix("user-service-tx-")
//        return factory
//    }
//
//    @Bean
//    fun kafkaTemplate(): KafkaTemplate<String, UserDto> {
//        return KafkaTemplate(producerFactory())
//    }
}
```