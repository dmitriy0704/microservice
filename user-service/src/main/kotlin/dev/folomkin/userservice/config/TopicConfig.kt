package dev.folomkin.userservice.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TopicConfig(
    @Value("\${spring.kafka.topic.name}") private val topicName: String,
    @Value("\${spring.kafka.topic.partitions}") private val partitions: Int,
    @Value("\${spring.kafka.topic.replication-factor}") private val replicationFactor: Short
) {
    @Bean
    fun topic(): NewTopic {
        return NewTopic(topicName, partitions, replicationFactor)
    }
}
