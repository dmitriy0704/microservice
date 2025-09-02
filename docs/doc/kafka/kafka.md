# Kafka. Основы технологии.

Брокер - сервер.

Топик - тема сообщений.

Топик состоит из партиций.

Партиции.<br>
Партиции распределены между брокерами внутри одного кластера.<br>
Подобная распределённость важна для горизонтального масштабирования кластера,
так как она позволяет клиентам писать и читать сообщения с нескольких
брокеров одновременно.<br>
Когда новое сообщение добавляется в топик, на самом деле оно записывается в
одну из партиций этого топика. Сообщения с одинаковыми ключами всегда
записываются в одну и ту же партицию, тем самым гарантируя очередность или
порядок записи и чтения.

Реплика.<br>
Для гарантии сохранности данных каждая партиция в Kafka может быть реплицирована
n раз, где n — replication factor. Таким образом гарантируется наличие
нескольких копий сообщения, хранящихся на разных брокерах.

У каждой партиции есть «лидер» (Leader) — брокер, который работает с клиентами.
Именно лидер работает с продюсерами и в общем случае отдаёт сообщения
консьюмерам. К лидеру осуществляют запросы фолловеры (Follower) — брокеры,
которые хранят реплику всех данных партиций. Сообщения всегда отправляются
лидеру и, в общем случае, читаются с лидера.

## Брокеры

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

## Как настроить кластер из трех брокеров

[docker-compose-3.yml](../../files/docker-compose-3.yml)

Развернуть кластер из **3 брокеров Kafka** можно двумя основными способами:

---

### ✅ **1. Через Docker Compose**

Kafka с **KRaft** (без ZooKeeper) позволяет запускать несколько брокеров в одном
`docker-compose.yml`.

Вот пример **docker-compose.yml для кластера из 3 брокеров + UI**:

```yaml
version: '3.8'

services:
  kafka-1:
    image: confluentinc/cp-kafka:7.5.0
    hostname: kafka-1
    container_name: kafka-1
    ports:
      - "9092:9092"
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://kafka-1:9092,CONTROLLER://kafka-1:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka-1:9093,2@kafka-2:9093,3@kafka-3:9093
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_LOG_DIRS: /var/lib/kafka/data
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 3

  kafka-2:
    image: confluentinc/cp-kafka:7.5.0
    hostname: kafka-2
    container_name: kafka-2
    ports:
      - "9093:9092"
    environment:
      KAFKA_NODE_ID: 2
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://kafka-2:9092,CONTROLLER://kafka-2:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9093
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka-1:9093,2@kafka-2:9093,3@kafka-3:9093
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_LOG_DIRS: /var/lib/kafka/data
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 3

  kafka-3:
    image: confluentinc/cp-kafka:7.5.0
    hostname: kafka-3
    container_name: kafka-3
    ports:
      - "9094:9092"
    environment:
      KAFKA_NODE_ID: 3
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://kafka-3:9092,CONTROLLER://kafka-3:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9094
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka-1:9093,2@kafka-2:9093,3@kafka-3:9093
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_LOG_DIRS: /var/lib/kafka/data
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 3

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: kafka-ui
    ports:
      - "8080:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: local
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka-1:9092,kafka-2:9092,kafka-3:9092
```

---

#### 🔍 **Как это работает:**

* **KRaft режим** → Kafka без ZooKeeper.
* **Каждый брокер** имеет уникальный `KAFKA_NODE_ID`.
* **KAFKA\_CONTROLLER\_QUORUM\_VOTERS** → определяет, кто голосует за
  лидерство (контроллеры).
* **Репликация** → возможна (3 копии).
* **Порты:**

    * `kafka-1` доступен на `localhost:9092`
    * `kafka-2` на `localhost:9093`
    * `kafka-3` на `localhost:9094`

---

### ✅ **2. Локально без Docker**

Можно запустить 3 брокера вручную, но:

* Нужно **3 конфигурационных файла** (`server-1.properties`,
  `server-2.properties`, `server-3.properties`).
* Уникальные `broker.id` (1,2,3).
* Уникальные `listeners` (например: `PLAINTEXT://:9092`, `:9093`, `:9094`).
* Один `KAFKA_HOME`.

Команда для запуска:

```bash
bin/kafka-server-start.sh config/server-1.properties
```

И так для всех.

---
===

## Кратко

Да, именно так! При запуске **кластера локально без Docker** нужно:

---

### ✅ **Как устроено**

Kafka — это **процесс** (Java-приложение), который можно запустить несколько раз
на одной машине, если:

* У каждого процесса **свой `broker.id`** (уникальный идентификатор брокера в
  кластере).
* Разные порты для **listeners** (например, `9092`, `9093`, `9094`).
* Разные **лог-директории** (чтобы данные не пересекались).

---

### ✅ **Пошагово**

1. Установлен один дистрибутив Kafka (например, в `/opt/kafka`).
2. В папке `config/` копируешь файл `server.properties`:

   ```
   server-1.properties
   server-2.properties
   server-3.properties
   ```
3. Для каждого файла меняешь:

* **broker.id** → 1, 2, 3
* **listeners** →

  ```
  server-1: PLAINTEXT://:9092
  server-2: PLAINTEXT://:9093
  server-3: PLAINTEXT://:9094
  ```
* **log.dirs** →

  ```
  /tmp/kafka-logs-1
  /tmp/kafka-logs-2
  /tmp/kafka-logs-3
  ```

4. Запускаешь три команды:

   ```bash
   bin/kafka-server-start.sh config/server-1.properties
   bin/kafka-server-start.sh config/server-2.properties
   bin/kafka-server-start.sh config/server-3.properties
   ```
5. **Кластер готов** → брокеры общаются между собой по ID.

---

### ✅ **А в KRaft-режиме?**

То же самое, только:

* Меняешь не `broker.id`, а `node.id`.
* Указываешь `process.roles=broker,controller`.
* Задаешь `controller.quorum.voters=1@host1:9093,2@host2:9093,3@host3:9093`.
* Для первого старта инициализируешь storage:

  ```bash
  bin/kafka-storage.sh format --config config/server-1.properties --cluster-id $(uuidgen)
  ```

---

Хочешь, я дам **полные три конфигурационных файла для кластера на одной машине (
ZooKeeper или KRaft)**, чтобы ты просто скопировал и запустил?

да:

## Отлично, вот готовый пример для **3 брокеров в KRaft режиме на одной машине

**.

---

## ✅ 1. Основная идея

* **Один дистрибутив Kafka**, три конфигурации.
* Используем **KRaft** (без ZooKeeper).
* Каждый брокер = **broker + controller** (Combined mode).
* Уникальный `node.id` для каждого.
* Общий `cluster.id` (инициализируем один раз).
* Разные порты для **broker (9092, 9093, 9094)** и **controller (29092, 29093,
  29094)**.
* Разные `log.dirs`.

---

## ✅ 2. Конфигурация

Создай три файла в `config/`:

### **server-1.properties**

```properties
process.roles=broker,controller
node.id=1
controller.quorum.voters=1@localhost:29092,2@localhost:29093,3@localhost:29094
listeners=PLAINTEXT://:9092,CONTROLLER://:29092
advertised.listeners=PLAINTEXT://localhost:9092
log.dirs=/tmp/kraft-combined-logs-1
num.partitions=3
# KRaft storage
inter.broker.listener.name=PLAINTEXT
controller.listener.names=CONTROLLER
```

### **server-2.properties**

```properties
process.roles=broker,controller
node.id=2
controller.quorum.voters=1@localhost:29092,2@localhost:29093,3@localhost:29094
listeners=PLAINTEXT://:9093,CONTROLLER://:29093
advertised.listeners=PLAINTEXT://localhost:9093
log.dirs=/tmp/kraft-combined-logs-2
num.partitions=3
inter.broker.listener.name=PLAINTEXT
controller.listener.names=CONTROLLER
```

### **server-3.properties**

```properties
process.roles=broker,controller
node.id=3
controller.quorum.voters=1@localhost:29092,2@localhost:29093,3@localhost:29094
listeners=PLAINTEXT://:9094,CONTROLLER://:29094
advertised.listeners=PLAINTEXT://localhost:9094
log.dirs=/tmp/kraft-combined-logs-3
num.partitions=3
inter.broker.listener.name=PLAINTEXT
controller.listener.names=CONTROLLER
```

---

## ✅ 3. Инициализация кластера

**Сначала создаем cluster.id (один раз для всех):**

```bash
CLUSTER_ID=$(bin/kafka-storage.sh random-uuid)
```

**Форматируем каждый брокер:**

```bash
bin/kafka-storage.sh format --config config/server-1.properties --cluster-id $CLUSTER_ID
bin/kafka-storage.sh format --config config/server-2.properties --cluster-id $CLUSTER_ID
bin/kafka-storage.sh format --config config/server-3.properties --cluster-id $CLUSTER_ID
```

---

## ✅ 4. Запуск брокеров

В отдельных терминалах:

```bash
bin/kafka-server-start.sh config/server-1.properties
bin/kafka-server-start.sh config/server-2.properties
bin/kafka-server-start.sh config/server-3.properties
```

---

## ✅ 5. Проверка кластера

После запуска:

```bash
bin/kafka-metadata-quorum.sh describe --bootstrap-server localhost:9092 --status
```

Ты увидишь информацию о контроллерах и брокерах.

---

Хочешь, сделаю **версию на Docker Compose для этого же кластера (3 брокера,
KRaft)**? Или пока оставляем на bare metal?

да:

Вот готовый Docker Compose для 3-брокерного кластера Kafka в KRaft режиме + UI.

Вот готовый **Docker Compose для 3-брокерного кластера Kafka в KRaft режиме + UI
**.

---

## ✅ `docker-compose.yml`

[docker-compose-5.yml](../../files/docker-compose-5.yml)

### ✅ **Как это работает**

* **3 брокера** (kafka-1, kafka-2, kafka-3).
* Каждый брокер в роли **broker + controller** (KRaft combined mode).
* Все используют **один `CLUSTER_ID`** (`kraft-cluster-id` → можно заменить на
  свой UUID).
* **UI** на `http://localhost:8080`.
* Данные хранятся в `./data/kafka-{1,2,3}`.

---

### ✅ **Как запустить**

1. Создай папку:

   ```bash
   mkdir -p data/kafka-1 data/kafka-2 data/kafka-3
   ```
2. Запусти:

   ```bash
   docker-compose up -d
   ```
3. Проверь статус:

   ```bash
   docker ps
   ```
4. Зайди в UI:

   ```
   http://localhost:8080
   ```

---

Хочешь, сделаю **версию с автоматическим созданием топика с 3 партициями и
репликацией = 3** прямо через Docker Compose?

да:

## Вот версия `docker-compose.yml`, где при старте автоматически создаётся *

*топик `user-topic` с 3 партициями и репликацией = 3**.

---

### ✅ `docker-compose.yml` с автосозданием топика

```yaml
version: '3.8'

services:
  kafka-1:
    image: confluentinc/cp-kafka:7.6.1
    container_name: kafka-1
    hostname: kafka-1
    ports:
      - "9092:9092"
    environment:
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_NODE_ID: 1
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka-1:29092,2@kafka-2:29093,3@kafka-3:29094
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:29092
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka-1:9092
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_LOG_DIRS: /var/lib/kafka/data
      CLUSTER_ID: "kraft-cluster-id"
      # Автоматическое создание топика
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "false"
    volumes:
      - ./data/kafka-1:/var/lib/kafka/data

  kafka-2:
    image: confluentinc/cp-kafka:7.6.1
    container_name: kafka-2
    hostname: kafka-2
    ports:
      - "9093:9093"
    environment:
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_NODE_ID: 2
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka-1:29092,2@kafka-2:29093,3@kafka-3:29094
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9093,CONTROLLER://0.0.0.0:29093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka-2:9093
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_LOG_DIRS: /var/lib/kafka/data
      CLUSTER_ID: "kraft-cluster-id"
    volumes:
      - ./data/kafka-2:/var/lib/kafka/data

  kafka-3:
    image: confluentinc/cp-kafka:7.6.1
    container_name: kafka-3
    hostname: kafka-3
    ports:
      - "9094:9094"
    environment:
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_NODE_ID: 3
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka-1:29092,2@kafka-2:29093,3@kafka-3:29094
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9094,CONTROLLER://0.0.0.0:29094
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka-3:9094
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_LOG_DIRS: /var/lib/kafka/data
      CLUSTER_ID: "kraft-cluster-id"
    volumes:
      - ./data/kafka-3:/var/lib/kafka/data

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: kafka-ui
    ports:
      - "8080:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: kraft-cluster
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka-1:9092,kafka-2:9093,kafka-3:9094

  create-topics:
    image: confluentinc/cp-kafka:7.6.1
    depends_on:
      - kafka-1
      - kafka-2
      - kafka-3
    entrypoint: [ "/bin/bash", "-c" ]
    command:
      - |
        echo "⏳ Жду 15 секунд, чтобы кластер стартовал...";
        sleep 15;
        echo "✅ Создаю топик user-topic...";
        kafka-topics --create \
          --bootstrap-server kafka-1:9092 \
          --replication-factor 3 \
          --partitions 3 \
          --topic user-topic;
        echo "📋 Список топиков:";
        kafka-topics --list --bootstrap-server kafka-1:9092;
```

---

### ✅ Что изменилось:

* **`KAFKA_AUTO_CREATE_TOPICS_ENABLE: "false"`** → чтобы топики не создавались
  автоматически.
* Добавлен сервис **`create-topics`**, который:

    * Ждёт 15 секунд, пока брокеры запустятся.
    * Создаёт **`user-topic` с 3 партициями и фактором репликации = 3**.
    * Выводит список всех топиков.

---

### ✅ Запуск:

```bash
docker-compose up -d
```

Проверка топиков:

```bash
docker-compose logs create-topics
```

---

Хочешь, сделаю **ещё более универсальную версию**, где можно через
`docker-compose.override.yml` задавать список топиков с партициями и репликацией
без изменения основного файла? Или сразу **с параметрами в `.env` файле** (чтобы
ты просто менял значения, а конфиг не трогал)?

## Оба варианта

Да, можно сделать **гибридный вариант**:

* **Основной `docker-compose.yml`** → базовая конфигурация (Kafka кластер + UI).
* **`.env`** → переменные (список топиков, кол-во партиций, фактор репликации).
* **`docker-compose.override.yml`** → автоматическое создание топиков, используя
  `.env`.

---

### ✅ 1. `.env` (универсальный конфиг)

```env
TOPICS=user-topic,order-topic
PARTITIONS=3
REPLICATION_FACTOR=3
```

---

### ✅ 2. `docker-compose.yml` (основной)

```yaml
version: '3.8'

services:
  kafka-1:
    image: confluentinc/cp-kafka:7.6.1
    container_name: kafka-1
    hostname: kafka-1
    ports:
      - "9092:9092"
    environment:
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_NODE_ID: 1
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka-1:29092,2@kafka-2:29093,3@kafka-3:29094
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:29092
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka-1:9092
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_LOG_DIRS: /var/lib/kafka/data
      CLUSTER_ID: "kraft-cluster-id"
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "false"
    volumes:
      - ./data/kafka-1:/var/lib/kafka/data

  kafka-2:
    image: confluentinc/cp-kafka:7.6.1
    container_name: kafka-2
    hostname: kafka-2
    ports:
      - "9093:9093"
    environment:
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_NODE_ID: 2
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka-1:29092,2@kafka-2:29093,3@kafka-3:29094
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9093,CONTROLLER://0.0.0.0:29093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka-2:9093
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_LOG_DIRS: /var/lib/kafka/data
      CLUSTER_ID: "kraft-cluster-id"
    volumes:
      - ./data/kafka-2:/var/lib/kafka/data

  kafka-3:
    image: confluentinc/cp-kafka:7.6.1
    container_name: kafka-3
    hostname: kafka-3
    ports:
      - "9094:9094"
    environment:
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_NODE_ID: 3
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka-1:29092,2@kafka-2:29093,3@kafka-3:29094
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9094,CONTROLLER://0.0.0.0:29094
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka-3:9094
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_LOG_DIRS: /var/lib/kafka/data
      CLUSTER_ID: "kraft-cluster-id"
    volumes:
      - ./data/kafka-3:/var/lib/kafka/data

  kafka-ui:
    image: provectuslabs/kafka-ui:latest
    container_name: kafka-ui
    ports:
      - "8080:8080"
    environment:
      KAFKA_CLUSTERS_0_NAME: kraft-cluster
      KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS: kafka-1:9092,kafka-2:9093,kafka-3:9094
```

---

### ✅ 3. `docker-compose.override.yml` (логика автосоздания топиков)

```yaml
version: '3.8'

services:
  create-topics:
    image: confluentinc/cp-kafka:7.6.1
    depends_on:
      - kafka-1
      - kafka-2
      - kafka-3
    entrypoint: [ "/bin/bash", "-c" ]
    command: >
      bash -c "
        echo '⏳ Жду 15 секунд для запуска брокеров...';
        sleep 15;
        for topic in ${TOPICS//,/ }
        do
          echo '✅ Создаю топик: '$topic;
          kafka-topics --create \
            --if-not-exists \
            --bootstrap-server kafka-1:9092 \
            --replication-factor ${REPLICATION_FACTOR} \
            --partitions ${PARTITIONS} \
            --topic $topic;
        done;
        echo '📋 Список топиков:';
        kafka-topics --list --bootstrap-server kafka-1:9092;
      "
```

---

### ✅ Как это работает:

* В `.env` указываешь:

  ```
  TOPICS=user-topic,order-topic
  PARTITIONS=3
  REPLICATION_FACTOR=3
  ```
* `create-topics` подхватывает эти переменные и создаёт все топики.
* Менять топики или настройки → только `.env`, файлы не трогаешь.

---

Хочешь, я сделаю **ещё один вариант с дополнительной гибкостью**, где можно
будет для каждого топика задать **свои партиции и репликацию**? Например:

```
TOPICS=user-topic:3:3,order-topic:5:2
```

и чтобы скрипт парсил это и создавал по параметрам?

ДА:

Тогда сделаем продвинутый вариант, где можно для каждого топика задать **свои
партиции** и **свой replication factor** в формате:

```
TOPICS=user-topic:3:3,order-topic:5:2
```

---

### ✅ 1. `.env`

```env
TOPICS=user-topic:3:3,order-topic:5:2
```

---

### ✅ 2. `docker-compose.override.yml` (новая версия)

```yaml
version: '3.8'

services:
  create-topics:
    image: confluentinc/cp-kafka:7.6.1
    depends_on:
      - kafka-1
      - kafka-2
      - kafka-3
    entrypoint: [ "/bin/bash", "-c" ]
    command: >
      bash -c "
        echo '⏳ Жду 15 секунд для запуска брокеров...';
        sleep 15;

        IFS=',' read -ra TOPIC_ARRAY <<< '${TOPICS}';
        for t in \"${TOPIC_ARRAY[@]}\"; do
          IFS=':' read -ra PARAMS <<< \"$t\";
          TOPIC_NAME=${PARAMS[0]};
          PARTITIONS=${PARAMS[1]};
          REPL=${PARAMS[2]};
          echo \"✅ Создаю топик: $TOPIC_NAME (партиций: $PARTITIONS, репликация: $REPL)\";
          kafka-topics --create \
            --if-not-exists \
            --bootstrap-server kafka-1:9092 \
            --partitions $PARTITIONS \
            --replication-factor $REPL \
            --topic $TOPIC_NAME;
        done;

        echo '📋 Список топиков:';
        kafka-topics --list --bootstrap-server kafka-1:9092;
      "
```

---

### ✅ Как пользоваться:

* В `.env`:

  ```
  TOPICS=user-topic:3:3,order-topic:5:2,analytics:6:3
  ```
* **Разделитель**:

    * `,` → разные топики
    * `:` → имя:партиции:репликация

---

#### ✅ Преимущества:

✔ Все топики создаются автоматически при старте контейнеров.
✔ Гибкость: для каждого топика задаёшь **свои параметры**.
✔ Меняешь только `.env`, не лезешь в YAML.

---

Хочешь, я сделаю **финальную версию `docker-compose.yml` с полным кластером
KRaft + UI + автоматическое создание топиков с гибкой конфигурацией** (объединим
всё в один файл)?

да

## ---

Вот **финальная версия `docker-compose.yml`** для кластера **Kafka KRaft** (3
брокера) + **UI (Kafdrop)** + **автоматическое создание топиков с гибкими
настройками**:

---

### ✅ 1. Файл `.env`

```env
CLUSTER_ID=my-cluster
TOPICS=user-topic:3:3,order-topic:5:2,analytics:6:3
```

---

### ✅ 2. `docker-compose.yml`

```yaml
version: '3.8'

services:
  # === Брокер 1 ===
  kafka-1:
    image: confluentinc/cp-kafka:7.6.1
    container_name: kafka-1
    environment:
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka-1:9092
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka-1:9093,2@kafka-2:9093,3@kafka-3:9093
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_CLUSTER_ID: ${CLUSTER_ID}
      KAFKA_LOG_DIRS: /var/lib/kafka/data
    volumes:
      - kafka1-data:/var/lib/kafka/data
    ports:
      - "9092:9092"

  # === Брокер 2 ===
  kafka-2:
    image: confluentinc/cp-kafka:7.6.1
    container_name: kafka-2
    environment:
      KAFKA_NODE_ID: 2
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka-2:9092
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka-1:9093,2@kafka-2:9093,3@kafka-3:9093
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_CLUSTER_ID: ${CLUSTER_ID}
      KAFKA_LOG_DIRS: /var/lib/kafka/data
    volumes:
      - kafka2-data:/var/lib/kafka/data
    ports:
      - "9093:9092"

  # === Брокер 3 ===
  kafka-3:
    image: confluentinc/cp-kafka:7.6.1
    container_name: kafka-3
    environment:
      KAFKA_NODE_ID: 3
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka-3:9092
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka-1:9093,2@kafka-2:9093,3@kafka-3:9093
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_CLUSTER_ID: ${CLUSTER_ID}
      KAFKA_LOG_DIRS: /var/lib/kafka/data
    volumes:
      - kafka3-data:/var/lib/kafka/data
    ports:
      - "9094:9092"

  # === UI (Kafdrop) ===
  kafdrop:
    image: obsidiandynamics/kafdrop
    container_name: kafdrop
    ports:
      - "9000:9000"
    environment:
      KAFKA_BROKERCONNECT: kafka-1:9092,kafka-2:9092,kafka-3:9092
    depends_on:
      - kafka-1
      - kafka-2
      - kafka-3

  # === Автосоздание топиков ===
  create-topics:
    image: confluentinc/cp-kafka:7.6.1
    depends_on:
      - kafka-1
      - kafka-2
      - kafka-3
    entrypoint: [ "/bin/bash", "-c" ]
    command: >
      bash -c "
        echo '⏳ Жду 15 секунд для запуска брокеров...';
        sleep 15;

        IFS=',' read -ra TOPIC_ARRAY <<< '${TOPICS}';
        for t in \"${TOPIC_ARRAY[@]}\"; do
          IFS=':' read -ra PARAMS <<< \"$t\";
          TOPIC_NAME=${PARAMS[0]};
          PARTITIONS=${PARAMS[1]};
          REPL=${PARAMS[2]};
          echo \"✅ Создаю топик: $TOPIC_NAME (партиций: $PARTITIONS, репликация: $REPL)\";
          kafka-topics --create \
            --if-not-exists \
            --bootstrap-server kafka-1:9092 \
            --partitions $PARTITIONS \
            --replication-factor $REPL \
            --topic $TOPIC_NAME;
        done;

        echo '📋 Список топиков:';
        kafka-topics --list --bootstrap-server kafka-1:9092;
      "

volumes:
  kafka1-data:
  kafka2-data:
  kafka3-data:
```

---

### ✅ Как запустить

```bash
docker compose up -d
```

* UI → `http://localhost:9000`
* Брокеры → `localhost:9092, localhost:9093, localhost:9094`

---

### ✅ Что происходит:

✔ **3 брокера в KRaft режиме** (контролеры внутри них же).
✔ **Kafdrop UI** для мониторинга.
✔ **Автосоздание топиков с параметрами из `.env`** (гибко, для каждого свой
`partitions:replication`).
✔ **Данные сохраняются в volumes** → перезапуск не теряет кластера.

---

## Или сразу дам **Spring Boot конфиг для подключения к этому кластеру (
Producer + Consumer)**?

Вот пример **полной конфигурации Spring Boot** для подключения к твоему кластеру
**Kafka KRaft (3 брокера)**:

---

## ✅ 1. `application.yml`

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092,localhost:9093,localhost:9094
    consumer:
      group-id: order-service-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "*"
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    listener:
      ack-mode: record
```

---

## ✅ 2. Конфиг-класс `KafkaConfig.kt`

```kotlin
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.admin.AdminClientConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.kafka.support.serializer.JsonDeserializer

@Configuration
@EnableKafka
class KafkaConfig {

    @Bean
    fun producerFactory(): ProducerFactory<String, Any> =
        DefaultKafkaProducerFactory(
            mapOf(
                "bootstrap.servers" to "localhost:9092,localhost:9093,localhost:9094",
                "key.serializer" to org.apache.kafka.common.serialization.StringSerializer::class.java,
                "value.serializer" to JsonSerializer::class.java
            )
        )

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Any> =
        KafkaTemplate(producerFactory())

    @Bean
    fun consumerFactory(): ConsumerFactory<String, Any> =
        DefaultKafkaConsumerFactory(
            mapOf(
                "bootstrap.servers" to "localhost:9092,localhost:9093,localhost:9094",
                "group.id" to "order-service-group",
                "key.deserializer" to org.apache.kafka.common.serialization.StringDeserializer::class.java,
                "value.deserializer" to JsonDeserializer::class.java,
                "spring.json.trusted.packages" to "*"
            )
        )

    // ✅ Пример автосоздания топика при старте Spring Boot
    @Bean
    fun userTopic(): NewTopic = TopicBuilder.name("user-topic")
        .partitions(3)
        .replicas(3)
        .build()
}
```

---

## ✅ 3. Producer (отправка)

```kotlin
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserProducer(private val kafkaTemplate: KafkaTemplate<String, Any>) {

    fun sendUser(user: UserDto) {
        val id = UUID.randomUUID().toString()
        kafkaTemplate.send("user-topic", id, user)
        println("✅ Sent user=$user with id=$id")
    }
}
```

---

## ✅ 4. Consumer (получение)

```kotlin
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class UserConsumer {

    @KafkaListener(topics = ["user-topic"], groupId = "order-service-group")
    fun consume(record: ConsumerRecord<String, UserDto>) {
        println("📥 Received user=${record.value()} with key=${record.key()}")
    }
}
```

---

### **Как работает:**

✔ `bootstrap-servers` → подключение к **3 брокерам** (`9092,9093,9094`) для
отказоустойчивости.
✔ Автосоздание топика `user-topic` с `3` партициями и `3` репликами (через
`@Bean`).
✔ Producer → отправка **с ключом (UUID)**.
✔ Consumer → принимает `ConsumerRecord`, можно достать **ключ** (тот самый
`id`).
