# Обработка ошибок в Kafka

Отличный вопрос 👍. Работа с **Apache Kafka** часто сопровождается типовыми
ошибками, особенно если только начинаешь внедрять её в продакшн. Я сгруппировал
их по категориям:

---

## 🔹 Ошибки при настройке брокеров

1. **Неверная конфигурация `listeners` и `advertised.listeners`**
   – клиенты не могут подключиться, особенно из Docker/Kubernetes.
   – частая ошибка: выставить `localhost:9092` вместо реального hostname или IP.

2. **Неправильный `replication.factor` и `min.insync.replicas`**
   – может привести к потере данных при сбое брокера.
   – например, выставлен `replication.factor=1` в продакшне.

3. **Недостаточно ресурсов (disk, RAM, network)**
   – Kafka очень чувствительна к диску и сетевой пропускной способности.
   – типично: маленький диск → сегменты быстро заполняются → `LogDirFailure`.

4. **Плохая конфигурация GC (Java)**
   – паузы GC вызывают лаги, задержки в продакшне.

---

## 🔹 Ошибки при работе с топиками и партициями

5. **Мало партиций**
   – ограничивает масштабируемость (только столько потребителей в группе,
   сколько партиций).

6. **Слишком много партиций**
   – нагрузка на контроллер и metadata cache, долгий ребаланс.

7. **Удаление или переименование топика**
   – потребители теряют оффсеты, начинаются ошибки
   `UnknownTopicOrPartitionException`.

---

## 🔹 Ошибки продюсеров

8. **Отсутствие подтверждений (`acks=0` или `acks=1`)**
   – данные могут теряться при сбое брокера.
   – правильнее использовать `acks=all`.

9. **Большие сообщения (>1 MB по умолчанию)**
   – выбивает `RecordTooLargeException`.
   – нужно менять `max.request.size` (producer) и `message.max.bytes` (broker).

10. **Необработанные ретраи**
    – дубликаты сообщений, если producer неидемпотентный.
    – решение: включить `enable.idempotence=true`.

---

## 🔹 Ошибки консумеров

11. **Неправильный авто-коммит оффсетов**
    – при `enable.auto.commit=true` можно потерять или дублировать сообщения.
    – часто лучше использовать **manual commit**.

12. **Ребаланс при долгой обработке**
    – `max.poll.interval.ms` слишком маленький → consumer вылетает из группы.

13. **Неправильный `auto.offset.reset`**
    – default=`latest`: новый consumer не получает старые сообщения.
    – часто нужно `earliest` в тестах и потоковой обработке.

---

## 🔹 Ошибки эксплуатации

14. **Нет мониторинга и алертинга**
    – Kafka работает "тихо" до момента сбоя.
    – нужно следить за lag, ISR, GC, disk usage.

15. **Отсутствие политики ретенции (`retention.ms`)**
    – логи растут бесконечно, пока не упадет диск.

16. **Игнорирование security**
    – не настроен TLS/SASL, все данные гоняются в plaintext.

---

✅ **Итого**:
Главные "подводные камни" — **сетевая конфигурация, неверные параметры
партиций/репликации, оффсеты у консумеров и игнорирование мониторинга**.

---

## ✅ Чек-лист Kafka для продакшена

1. **Настрой сетевые параметры правильно**

    * `listeners` = внутренний адрес брокера
    * `advertised.listeners` = адрес, доступный клиентам
    * Проверяй работу внутри Docker/K8s.

2. **Используй репликацию ≥ 3**

    * `replication.factor=3`
    * `min.insync.replicas=2`
    * Так данные сохранятся при падении брокера.

3. **Всегда включай подтверждения (`acks=all`)**

    * Это предотвращает потерю сообщений при сбое.

4. **Включи идемпотентность продюсера**

    * `enable.idempotence=true`
    * Убирает дубликаты при ретраях.

5. **Контролируй оффсеты консумеров**

    * Не полагайся только на `enable.auto.commit`.
    * Для критичных данных используй ручной commit после обработки.

6. **Следи за временем обработки у консумеров**

    * Увеличивай `max.poll.interval.ms`, если обработка долгая.
    * Иначе consumer будет выкинут из группы при ребалансе.

7. **Не перегружай количество партиций**

    * Партиций должно быть достаточно для параллелизма, но не тысячи без
      необходимости.
    * Начинай с `(#CPU * 2)` и масштабируй по метрикам.

8. **Ограничивай размер сообщений**

    * Настрой `max.request.size`, `message.max.bytes`.
    * Лучше дробить payload на батчи.

9. **Включи мониторинг и алертинг**

    * Отслеживай **consumer lag**, **ISR**, **disk usage**, **latency**.
    * Интеграция с Prometheus + Grafana = must have.

10. **Настрой политику хранения (`retention.ms`)**

* Логи не должны расти бесконечно.
* Выбирай по SLA:

    * realtime → часы
    * аналитика → дни/недели.

---

⚡ Бонус-правило: **Включи TLS/SASL** (аутентификация + шифрование). Без этого
всё передается в открытом виде.

---

## Набор конфигов (
`server.properties` + producer + consumer), который соответствует этому чек-листу

🔥 Отлично, тогда вот тебе готовый набор конфигов, собранный под чек-лист.

---

## 📌 `server.properties` (брокер Kafka)

```properties
############################
# Сеть
############################
listeners=PLAINTEXT://0.0.0.0:9092
advertised.listeners=PLAINTEXT://my-kafka-broker:9092
broker.id=1
############################
# Хранение и репликация
############################
log.dirs=/var/lib/kafka/data
num.partitions=3
default.replication.factor=3
min.insync.replicas=2
unclean.leader.election.enable=false
############################
# Лимиты
############################
message.max.bytes=10485760        # 10MB
replica.fetch.max.bytes=10485760
############################
# Ретенция
############################
log.retention.ms=604800000        # 7 дней
log.segment.bytes=1073741824      # 1GB
log.retention.check.interval.ms=300000
############################
# Мониторинг
############################
metrics.recording.level=INFO
```

---

### 📌 Конфиг **Producer** (Kotlin/Spring Boot)

```kotlin
@Bean
fun producerFactory(): ProducerFactory<String, String> {
    val config = mapOf(
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "my-kafka-broker:9092",
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        ProducerConfig.ACKS_CONFIG to "all",
        ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true,
        ProducerConfig.RETRIES_CONFIG to Int.MAX_VALUE,
        ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION to 5,
        ProducerConfig.LINGER_MS_CONFIG to 20,
        ProducerConfig.BATCH_SIZE_CONFIG to 32768,
        ProducerConfig.MAX_REQUEST_SIZE_CONFIG to 10485760 // 10MB
    )
    return DefaultKafkaProducerFactory(config)
}
```

---

### 📌 Конфиг **Consumer** (Kotlin/Spring Boot)

```kotlin
@Bean
fun consumerFactory(): ConsumerFactory<String, String> {
    val config = mapOf(
        ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to "my-kafka-broker:9092",
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.GROUP_ID_CONFIG to "my-consumer-group",
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false, // ручной commit
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        ConsumerConfig.MAX_POLL_RECORDS_CONFIG to 500,
        ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG to 300000 // 5 минут
    )
    return DefaultKafkaConsumerFactory(config)
}
```

Пример ручного коммита оффсета:

```kotlin
@KafkaListener(topics = ["my-topic"], groupId = "my-consumer-group")
fun listen(records: List<ConsumerRecord<String, String>>, ack: Acknowledgment) {
    records.forEach { record ->
        println("Received: ${record.value()}")
        // бизнес-логика
    }
    ack.acknowledge() // ручной коммит после успешной обработки
}
```

---

### 📌 Мониторинг (Prometheus JMX Exporter)

Добавь JMX Exporter в контейнер:

```yaml
KAFKA_OPTS: "-javaagent:/usr/app/jmx_prometheus_javaagent-0.16.1.jar=7071:/etc/jmx-exporter/kafka.yml"
```

✅ Этот набор даст тебе:

* гарантированную доставку (`acks=all`, `enable.idempotence=true`),
* отсутствие потери оффсетов (ручной commit),
* управляемую ретенцию,
* метрики для мониторинга.
