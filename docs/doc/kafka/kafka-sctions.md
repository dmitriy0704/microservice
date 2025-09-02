# Изучение kafka

## ЛОКАЛЬНО

### Использование KRaft в режиме server.

Работа с kafka:
Три режима работы:

- broker;
- controller;
- server;

### Конфигурация kafka-server:

Файлы конфигурации находятся в
`kafka-[bin]/config/kraft/server.properties`

**Параметры конфигурации:**

Для конфигурации нескольких серверов номер порта увеличивается на 1

- _**node.id=1**_ - у каждого нового сервера должен быть уникальный номер
- _**listeners=PLAINTEXT://:9092,CONTROLLER://:9093**_ - порты для брокера и
  контроллера
- _**controller.quorum.voters=1@localhost:9093**_ - определяется кто будет
  следующим лидером после падения первого
- _**advertised.listeners=PLAINTEXT://localhost:9092**_ - порт на котором брокер
  будет прослушивать клиентов
- _**log.dirs=/tmp/server-1/kraft-combined-logs**_ - директория логов

#### Запуск Kafka Server

Из каталога kafka-[version]-src

```shell
  # Сгенерировать id для kafka кластера:
./bin/kafka-storage.sh random-uui

# Форматирование логов для совместимости с kraft режимом:
./bin/kafka-storage.sh format -t <uuid> -c ./config/kraft/server.properties

# Запуск kafka
./bin/kafka-server-start.sh ./config/kraft/server.properties

# docker:
docker run -d -p 9092:9092 apache/kafka:latest

# Статус кластера
bin/kafka-metadata-quorum.sh --bootstrap-server localhost:9092 describe --status
```

#### Топики

```shell
  # Создание нового топика:
./bin/kafka-topics.sh --create --topic demo-topic --partitions 3 --replication-factor 3 --bootstrap-server localhost:9092,localhost:9094
./bin/kafka-topics.sh --create --topic demo-topic --bootstrap-server localhost:9092

# Информация о топике
bin/kafka-topics.sh --bootstrap-server localhost:29092 --topic demo-topic --describe

# Список топиков:
./bin/kafka-topics.sh --list --bootstrap-server localhost:9092,localhost:9094

# Удаление топика
./bin/kafka-topics.sh --delete --topic <topic-name> --bootstrap-server localhost:9092,localhost:9094
```

#### Сообщения:

```shell
  # Отправка сообщения:
./bin/kafka-console-producer.sh --bootstrap-server localhost:9092,localhost:9094 --topic <topic-name>

# Отрправка сообений с параметрами: отправка сообщений с ключом через ":"
kafka-console-producer.sh --broker-list localhost:9092 --topic topic-name --property "parse.key=true" --property "key.separator=:"


# Прослушивание сообщений консюмером:
# Параметры: получение сообщений с ключом через ":"
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092,localhost:9094 --topic demo-topic --property "print.key=true" --property print.value=true --property key.separator=:

# Прочитать сообщения из топика с начала:
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092,localhost:9094 --topic test-topic --from-beginning
```

## DOCKER

[docker-compose-0.yaml](../../files/docker-compose-0.yaml)

KRaft:<br>

Используем Bitnami Kafka (без Zookeeper).<br>
Настроены PROCESS_ROLES → broker,controller (Kafka в режиме KRaft).<br>
Контроллер слушает на 9093.<br>
Брокер слушает на 9092 (для приложений) и 9094 (для доступа с хоста).<br>

Kafka UI:<br>

Provectus Kafka UI на порту 8080.<br>
Подключается напрямую к kafka:9092 (в Docker-сети).<br>

Автосоздание топиков:<br>

KAFKA_CFG_AUTO_CREATE_TOPICS_ENABLE=true<br>
KAFKA_CFG_NUM_PARTITIONS=3 (дефолт для новых топиков).<br>
Kafka доступна на localhost:9094 (для сервисов).<br>
Kafka UI → http://localhost:8080<br>

### Проверить запущенный образ

Есть несколько способов проверить, что Kafka из `docker-compose` работает и
доступна:

#### 1. **Проверить контейнеры**

Выполни:

```bash
docker ps
```

Убедись, что контейнеры Kafka (и Zookeeper, если не KRaft) запущены. Если есть
UI (например, `provectuslabs/kafka-ui`), открой его по адресу:

```
http://localhost:8080
```

#### 2. **Подключиться к Kafka через CLI (в контейнере)**

Выполни:

```bash
  docker exec -it <kafka-container-name> bash
```

Дальше проверим доступность брокера:

```bash
  kafka-topics.sh --bootstrap-server localhost:9092 --list
```

Если видишь список топиков (или пустой, если нет топиков) — брокер работает.

#### 3. **Создать топик и проверить**

Внутри контейнера:

```bash
  kafka-topics.sh --create --topic test-topic --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

Проверить:

```bash
  kafka-topics.sh --describe --topic test-topic --bootstrap-server localhost:9092
```

#### 4. **Отправить и получить сообщение**

В контейнере:

```bash
# Запуск producer
  kafka-console-producer.sh --topic test-topic --bootstrap-server localhost:9092
```

Введи пару сообщений.

В другом терминале (или вкладке в контейнере):

```bash
  kafka-console-consumer.sh --topic test-topic --bootstrap-server localhost:9092 --from-beginning
```

## Проверка Kafka из хоста без захода в контейнер через Docker + CLI и через Kafka UI

С помощью команды `docker compose exec`.

### **Как попасть в контейнер Kafka через Docker Compose**

Предположим, что в твоём `docker-compose.yml` сервис называется `kafka`. Тогда:

```bash
docker compose exec kafka bash
```

или, если в образе нет `bash` (часто бывает в lightweight образах):

```bash
docker compose exec kafka sh
```

### **Запустить команды Kafka прямо из контейнера**

После входа можно использовать встроенные скрипты:

* **Список топиков:**

```bash
  kafka-topics.sh --bootstrap-server localhost:9092 --list
 ```

* **Создать топик:**

```bash
  kafka-topics.sh --create --topic test-topic --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
 ```

* **Отправить сообщение:**

```bash
  kafka-console-producer.sh --topic test-topic --bootstrap-server localhost:9092
```

* **Читать сообщения:**

```bash
  kafka-console-consumer.sh --topic test-topic --bootstrap-server localhost:9092 --from-beginning
```

### **А можно без входа внутрь контейнера?**

Да, можно выполнить команду **напрямую** из хоста, через `exec`:

Например:

```bash
docker compose exec kafka kafka-topics.sh --bootstrap-server localhost:9092 --list
```

или создать топик:

```bash
docker compose exec kafka kafka-topics.sh --create --topic test-topic --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```

---
===

## Полный список полезных команд Kafka через `docker compose exec`

Вот **полный набор полезных команд для проверки работы Kafka и управления
топиками через `docker compose exec`**:

#### **1. Проверить список топиков**

```bash
docker compose exec kafka kafka-topics.sh --bootstrap-server localhost:9092 --list
```

#### **2. Создать топик**

```bash
 docker compose exec kafka kafka-topics.sh \
  --create \
  --topic test-topic \
  --bootstrap-server localhost:9092 \
  --partitions 3 \--replication-factor 1
```

#### ✅ **3. Проверить детали топика**

```bash
docker compose exec kafka kafka-topics.sh \
  --describe \
  --topic test-topic \
  --bootstrap-server localhost:9092
```

#### ✅ **4. Отправить сообщение в топик**

```bash
docker compose exec -it kafka kafka-console-producer.sh \
  --topic test-topic \
  --bootstrap-server localhost:9092
```

После запуска просто вводишь сообщения в консоли и жмёшь **Enter**.

#### **5. Прочитать сообщения из топика**

```bash
docker compose exec kafka kafka-console-consumer.sh \
  --topic test-topic \
  --bootstrap-server localhost:9092 \
  --from-beginning
```

#### **6. Удалить топик**

```bash
docker compose exec kafka kafka-topics.sh \
  --delete \
  --topic test-topic \
  --bootstrap-server localhost:9092
```

#### **7. Проверить доступность брокера**

Можно просто вызвать **метаданные**:

```bash
docker compose exec kafka kafka-broker-api-versions.sh --bootstrap-server localhost:9092
```

## Работа с брокерами

В Apache Kafka количество брокеров (узлов кластера) не задается напрямую через
какой-то единый параметр в конфигурации топика или команды, так как оно
определяется архитектурой вашего кластера. Количество брокеров зависит от того,
сколько серверов (брокеров) вы развернули в кластере Kafka. Однако есть
связанные аспекты, которые влияют на то, как брокеры используются, включая
настройку топиков и их репликацию.

Вот подробное объяснение, как задается и контролируется количество брокеров в
Kafka:

### 1. **Развертывание брокеров**

- Количество брокеров определяется тем, сколько экземпляров Kafka вы запустили в
  кластере. Каждый брокер — это отдельный процесс Kafka, работающий на сервере
  или контейнере.
- Для запуска брокера вы настраиваете его конфигурационный файл (
  `server.properties`) и указываете уникальный идентификатор (`broker.id`) для
  каждого брокера.
- Например, если вы хотите 3 брокера, вы запускаете 3 процесса Kafka с разными
  `broker.id` (например, 0, 1, 2) на одном или разных серверах.

**Пример настройки в `server.properties`**:

   ```properties
   broker.id=0
listeners=PLAINTEXT://:9092
zookeeper.connect=localhost:2181
   ```

Для каждого брокера меняется `broker.id` и, при необходимости, порт или хост в
`listeners`.

После запуска всех брокеров они подключаются к ZooKeeper, который управляет
координацией кластера.

### 2. **Связь с топиками и replication factor**

- Количество брокеров напрямую влияет на максимальный **replication factor** для
  топиков. Например, если у вас 3 брокера, то максимальный replication factor
  для топика равен 3 (каждая партиция может иметь до 3 реплик, по одной на
  каждом брокере).
- При создании топика вы указываете replication factor, но он не может превышать
  количество доступных брокеров. Например:
  ```bash
  kafka-topics.sh --create --topic my-topic --replication-factor 3 --partitions 4 --bootstrap-server localhost:9092
  ```
  Здесь Kafka распределит реплики партиций по доступным брокерам.

Если брокеров меньше, чем указанный replication factor, команда выдаст ошибку.

### 3. **Добавление или удаление брокеров**

- **Добавление брокеров**:
    1. Настройте новый сервер с уникальным `broker.id` в `server.properties`.
    2. Запустите процесс Kafka, указав подключение к ZooKeeper.
    3. Kafka автоматически интегрирует новый брокер в кластер, и вы можете
       переназначить партиции на новый брокер с помощью команды
       `kafka-reassign-partitions.sh`.

- **Удаление брокеров**:
    1. Используйте инструмент `kafka-reassign-partitions.sh`, чтобы переместить
       все партиции с удаляемого брокера на другие.
    2. Остановите процесс Kafka на этом брокере.
    3. Обновите конфигурацию ZooKeeper, если необходимо.

**Пример команды для переназначения партиций**:

   ```bash
   kafka-reassign-partitions.sh --zookeeper localhost:2181 --reassignment-json-file reassignment.json --execute
   ```

В файле `reassignment.json` вы указываете, как перераспределить партиции между
брокерами.

### 4. **Проверка количества брокеров**

- Чтобы узнать, сколько брокеров активно в кластере, можно использовать команду:
  ```bash
  kafka-topics.sh --describe --topic my-topic --bootstrap-server localhost:9092
  ```
  В выводе будут указаны брокеры, на которых размещены лидеры и реплики
  партиций.
- Также можно проверить через ZooKeeper или инструменты мониторинга, такие как
  Kafka Manager или Confluent Control Center.

### 5. **Автоматическое распределение**

- Kafka автоматически распределяет партиции и их реплики по доступным брокерам,
  если не указано иное. Это поведение можно настроить с помощью параметра
  `auto.leader.rebalance.enable` или инструмента `kafka-reassign-partitions.sh`
  для ручного управления.

### 6. **Ограничения и рекомендации**

- Количество брокеров должно быть достаточным для обеспечения желаемого уровня
  отказоустойчивости. Например, для replication factor = 3 требуется минимум 3
  брокера.
- Убедитесь, что каждый брокер имеет уникальный `broker.id` и правильно настроен
  для связи с ZooKeeper.
- Для продакшн-систем рекомендуется минимум 3 брокера, чтобы обеспечить
  отказоустойчивость и балансировку нагрузки.

### Итог

Количество брокеров задается физически — через запуск отдельных процессов Kafka
с уникальными `broker.id`. Вы не указываете это число в одной настройке, а
определяете его, развертывая кластер. После этого параметры, такие как
replication factor и количество партиций, зависят от числа доступных брокеров.

Если вам нужно больше деталей, например, как настроить кластер или
оптимизировать распределение партиций, дайте знать!