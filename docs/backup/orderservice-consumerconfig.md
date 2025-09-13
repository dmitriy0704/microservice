`13-09-25`

Настройка консьюмера

```kotlin
package dev.folomkin.orderservice.config

import dev.folomkin.shared.dto.UserDto
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.util.backoff.FixedBackOff

@EnableKafka
@Configuration
class ConsumerConfig {

    @Bean
    fun errorHandler(kafkaTemplate: KafkaTemplate<String, Any>): DefaultErrorHandler {
        val recoverer =
            DeadLetterPublishingRecoverer(kafkaTemplate) { record, ex ->
                // Сообщение уходит в топик user-topic.DLT
                TopicPartition(record.topic() + ".DLT", record.partition())
            }

        val backoff = FixedBackOff(1000L, 3) // 3 попытки, каждая через 1 сек
        return DefaultErrorHandler(recoverer, backoff)
    }

    //-> Для кастомного использования
//    @Bean
//    fun consumerFactory(): ConsumerFactory<String, UserDto> {
//        val props = mapOf(
//            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
//            ConsumerConfig.GROUP_ID_CONFIG to "order-service-group",
//            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
//            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to JsonDeserializer::class.java,
//            JsonDeserializer.TRUSTED_PACKAGES to "*",

            // читать только зафиксированные транзакции
//            ConsumerConfig.ISOLATION_LEVEL_CONFIG to "read_committed"
//
//        )
//        return DefaultKafkaConsumerFactory(
//            props,
//            StringDeserializer(),
//            JsonDeserializer(UserDto::class.java)
//        )
//    }

//    @Bean
//    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, UserDto> {
//        val factory = ConcurrentKafkaListenerContainerFactory<String, UserDto>()
//        factory.consumerFactory = consumerFactory()
//        return factory
//    }
}
```