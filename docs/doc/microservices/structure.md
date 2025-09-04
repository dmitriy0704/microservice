# Микросервисная архитектура для приложения на Kotlin и Spring Boot представляет собой модульную структуру, где приложение разбивается на небольшие, независимые сервисы, каждый из которых выполняет определённую функцию и взаимодействует с другими через чётко определённые интерфейсы (обычно REST API, gRPC или сообщения через брокеры). Вот общая структура микросервисной архитектуры для такого приложения:

### 1. **Общая структура**

Микросервисная архитектура состоит из нескольких ключевых компонентов, которые
взаимодействуют для обеспечения масштабируемости, отказоустойчивости и гибкости.
Основные элементы:

- **Микросервисы**: Независимые модули, каждый из которых реализует конкретную
  бизнес-функцию (например, управление пользователями, заказы, платежи).
- **API Gateway**: Единая точка входа для клиентских запросов, которая
  маршрутизирует их к соответствующим сервисам.
- **Сервис обнаружения (Service Discovery)**: Инструмент для отслеживания
  доступных микросервисов (например, Eureka, Consul).
- **Конфигурация**: Централизованное управление конфигурациями (Spring Cloud
  Config).
- **Обмен сообщениями**: Асинхронное взаимодействие через брокеры сообщений (
  Kafka, RabbitMQ).
- **Базы данных**: Каждый микросервис обычно имеет собственную базу данных (
  Database-per-Service).
- **Мониторинг и логирование**: Инструменты для отслеживания состояния системы (
  Prometheus, Grafana, ELK Stack).
- **Контейнеризация и оркестрация**: Использование Docker и Kubernetes для
  развертывания и управления сервисами.

### 2. **Структура одного микросервиса (Kotlin + Spring Boot)**

Каждый микросервис — это отдельное приложение на Kotlin/Spring Boot с типичной
структурой:

#### a) **Структура проекта**

Пример структуры директорий микросервиса:

```
microservice-name/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   ├── com/example/microservice/
│   │   │   │   ├── config/          # Конфигурации (Spring, Security, etc.)
│   │   │   │   ├── controller/      # REST-контроллеры для API
│   │   │   │   ├── service/         # Бизнес-логика
│   │   │   │   ├── repository/      # Интерфейсы для работы с базой данных
│   │   │   │   ├── model/           # Модели данных (Entity/DTO)
│   │   │   │   ├── Application.kt   # Главный класс для запуска Spring Boot
│   │   ├── resources/
│   │       ├── application.yml      # Конфигурация приложения
│   │       ├── application-dev.yml  # Конфигурация для dev-окружения
│   │       ├── application-prod.yml # Конфигурация для продакшн
├── Dockerfile                       # Файл для контейнеризации
├── build.gradle.kts                # Скрипт сборки Gradle
└── settings.gradle.kts             # Настройки Gradle
```

#### b) **Ключевые компоненты микросервиса**

- **Application.kt**: Главный класс с аннотацией `@SpringBootApplication` для
  запуска приложения.
- **Контроллеры**: REST API endpoints (например, `@RestController`) для
  обработки HTTP-запросов.
- **Сервисы**: Классы с аннотацией `@Service`, содержащие бизнес-логику.
- **Репозитории**: Интерфейсы с аннотацией `@Repository` для работы с базой
  данных через Spring Data JPA.
- **Модели**: Entity-классы для работы с базой данных и DTO для передачи данных.
- **Конфигурация**: Файлы `application.yml` для настройки окружения, подключения
  к базе данных, внешним сервисам и т.д.

#### c) **Пример Dockerfile**

См. предыдущий ответ с примером Dockerfile для контейнеризации микросервиса.

### 3. **Взаимодействие между микросервисами**

- **Синхронное взаимодействие**:
    - **REST API**: Используется Spring Web (RestTemplate или WebClient) для
      HTTP-запросов между сервисами.
    - **OpenFeign**: Упрощает вызовы REST API с помощью декларативных клиентов.
- **Асинхронное взаимодействие**:
    - **Брокеры сообщений**: Kafka, RabbitMQ или ActiveMQ для передачи событий (
      например, с помощью Spring Cloud Stream).
    - **Event-Driven подход**: Микросервисы публикуют события, на которые
      подписываются другие сервисы.
- **API Gateway**:
    - Реализуется с помощью Spring Cloud Gateway или Netflix Zuul.
    - Обеспечивает маршрутизацию, балансировку нагрузки, аутентификацию и
      кэширование.

### 4. **Инфраструктура**

- **Сервис обнаружения**:
    - Используется Eureka (Spring Cloud Netflix) или Consul для регистрации и
      обнаружения микросервисов.
    - Позволяет сервисам находить друг друга динамически.
- **Конфигурация**:
    - Spring Cloud Config Server для централизованного управления
      конфигурациями.
    - Хранилище конфигураций (Git, Consul, Vault).
- **Базы данных**:
    - Каждый микросервис имеет свою базу данных (например, PostgreSQL, MongoDB).
    - Используется Spring Data JPA или Spring Data MongoDB для работы с базами.
- **Контейнеризация**:
    - Каждый микросервис упаковывается в Docker-контейнер.
    - Kubernetes используется для оркестрации, масштабирования и управления
      контейнерами.
- **Мониторинг**:
    - **Логирование**: Spring Boot Actuator + ELK Stack (Elasticsearch,
      Logstash, Kibana).
    - **Метрики**: Prometheus + Grafana для мониторинга производительности.
    - **Трассировка**: Spring Cloud Sleuth + Zipkin для отслеживания запросов
      между сервисами.

### 5. **Пример архитектуры**

Предположим, у вас есть интернет-магазин с тремя микросервисами:

- **User Service**: Управление пользователями (регистрация, аутентификация).
- **Order Service**: Управление заказами.
- **Payment Service**: Обработка платежей.

#### Взаимодействие:

1. Клиент отправляет запрос через API Gateway (например, `/api/orders`).
2. API Gateway маршрутизирует запрос к Order Service.
3. Order Service запрашивает данные пользователя у User Service через REST API
   или Feign-клиент.
4. Order Service отправляет событие о новом заказе в Kafka.
5. Payment Service подписывается на это событие и обрабатывает платеж.
6. Все сервисы регистрируются в Eureka для обнаружения.
7. Конфигурации хранятся в Spring Cloud Config Server.
8. Логи и метрики собираются в Prometheus и ELK.

### 6. **Рекомендации по реализации**

- **Kotlin**: Используйте возможности Kotlin, такие как корутины для асинхронной
  обработки, data-классы для DTO, и null-safety для безопасной работы с данными.
- **Spring Boot**: Используйте Spring Boot Starters (например,
  `spring-boot-starter-web`, `spring-boot-starter-data-jpa`) для упрощения
  конфигурации.
- **Spring Cloud**: Используйте модули Spring Cloud (Eureka, Config, Gateway,
  Sleuth) для упрощения работы с микросервисами.
- **Тестирование**: Реализуйте модульные тесты (`@SpringBootTest`, JUnit) и
  интеграционные тесты с Testcontainers для проверки взаимодействия с базой
  данных и другими сервисами.
- **CI/CD**: Настройте Jenkins, GitHub Actions или GitLab CI для автоматизации
  сборки, тестирования и деплоя.

### 7. **Пример конфигурации (application.yml)**

```yaml
spring:
  application:
    name: user-service
  datasource:
    url: jdbc:postgresql://localhost:5432/userdb
    username: user
    password: password
  jpa:
    hibernate:
      ddl-auto: update
eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
server:
  port: 8081
```

### 8. **Преимущества и вызовы**

- **Преимущества**:
    - Независимая разработка, деплой и масштабирование сервисов.
    - Гибкость в выборе технологий для каждого сервиса.
    - Устойчивость к сбоям (один сервис не ломает всю систему).
- **Вызовы**:
    - Сложность управления распределённой системой.
    - Необходимость в хорошем мониторинге и трассировке.
    - Усложнение тестирования из-за распределённой природы.

Эта структура обеспечивает гибкость, масштабируемость и устойчивость приложения,
но требует тщательного проектирования и мониторинга для успешной реализации.

## Простейшее взаимодействие между сервисами

Да, именно так! В предложенной архитектуре с **User Service** и **Order Service
**, **Spring Security** распределяет ответственность следующим образом:

- **User Service** использует Spring Security для аутентификации пользователей и
  выдачи **JWT-токена** (JSON Web Token) при успешном логине или регистрации.
- **Order Service** использует Spring Security для проверки этого токена, чтобы
  убедиться, что запросы от клиентов или других сервисов (например, User
  Service) авторизованы.

Давайте разберем это подробнее, чтобы прояснить процесс и показать, как это
реализуется в Kotlin с использованием Spring Boot и Spring Security.

### 1. **User Service: Выдача JWT**

**User Service** отвечает за аутентификацию пользователей (логин/пароль, OAuth2,
если нужно) и генерацию JWT-токена. Spring Security предоставляет удобные
механизмы для этого через конфигурацию и фильтры.

#### Основные шаги в User Service:

- Пользователь отправляет POST-запрос на `/api/auth/login` с логином и паролем.
- Spring Security проверяет учетные данные (через `AuthenticationManager`).
- При успешной аутентификации генерируется JWT, который возвращается клиенту.
- JWT содержит информацию о пользователе (например, `userId`, `roles`) и
  подписан секретным ключом.

**Пример реализации в User Service (Kotlin):**

```kotlin
// Модель для запроса логина
data class LoginRequest(val email: String, val password: String)
data class LoginResponse(val token: String)

// Контроллер для аутентификации
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val jwtUtil: JwtUtil // Утилита для работы с JWT
) {
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        // Аутентификация через Spring Security
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.email, request.password)
        )
        // Генерация JWT
        val token =
            jwtUtil.generateToken(authentication.principal as UserDetails)
        return ResponseEntity.ok(LoginResponse(token))
    }
}

// Утилита для генерации и проверки JWT
@Component
class JwtUtil {
    private val secret: String = "your-256-bit-secret" // Хранить в конфигурации
    private val expirationMs: Long = 86400000 // 1 день

    fun generateToken(userDetails: UserDetails): String {
        val claims = Jwts.claims().setSubject(userDetails.username)
        claims["roles"] = userDetails.authorities.map { it.authority }
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expirationMs))
            .signWith(SignatureAlgorithm.HS256, secret)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun getUsernameFromToken(token: String): String {
        return Jwts.parser().setSigningKey(secret)
            .parseClaimsJws(token).body.subject
    }
}
```

**Конфигурация Spring Security (User Service):**

```kotlin
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtUtil: JwtUtil
) : WebSecurityConfigurerAdapter() {

    @Bean
    override fun authenticationManagerBean(): AuthenticationManager {
        return super.authenticationManagerBean()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    override fun configure(http: HttpSecurity) {
        http
            .csrf().disable()
            .authorizeRequests()
            .antMatchers("/api/auth/**")
            .permitAll() // Логин доступен без авторизации
            .anyRequest().authenticated()
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }
}
```

**application.yml (User Service):**

```yaml
jwt:
  secret: your-256-bit-secret
  expiration: 86400000
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/userdb
    username: user
    password: password
  jpa:
    hibernate:
      ddl-auto: update
```

**Что происходит:**

- Пользователь отправляет логин/пароль на `/api/auth/login`.
- Spring Security проверяет учетные данные (через `UserDetailsService`, который
  загружает пользователя из базы).
- Если данные верны, создается JWT с информацией о пользователе (email, роли) и
  возвращается клиенту.
- Клиент (или другой сервис) использует этот токен в заголовке
  `Authorization: Bearer <token>` для доступа к защищенным ресурсам.

---

### 2. **Order Service: Проверка JWT**

**Order Service** использует Spring Security для проверки JWT, полученного от
клиента или User Service. Он выступает как **Resource Server**, который
валидирует токен и извлекает данные о пользователе (например, `userId` или
роли).

#### Основные шаги в Order Service:

- Клиент (или User Service) отправляет запрос с заголовком
  `Authorization: Bearer <token>`.
- Spring Security перехватывает запрос, проверяет токен (подпись, срок действия)
  и извлекает `Principal` (данные пользователя).
- Order Service использует эти данные для авторизации (например, проверяет, что
  пользователь имеет право создавать заказ).

**Пример реализации в Order Service (Kotlin):**

```kotlin
// Контроллер для заказов
@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService
) {
    @PostMapping
    @PreAuthorize("hasRole('USER')") // Требуется роль USER
    fun createOrder(
        @RequestBody orderRequest: OrderRequestDTO,
        @AuthenticationPrincipal jwt: Jwt
    ): OrderDTO {
        val userId = jwt.subject.toLong() // Извлекаем userId из JWT
        return orderService.createOrder(userId, orderRequest)
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') and #userId == principal.subject")
    fun getOrdersByUserId(
        @PathVariable userId: Long,
        @AuthenticationPrincipal jwt: Jwt
    ): List<OrderDTO> {
        return orderService.getOrdersByUserId(userId)
    }
}

// Сервис заказов
@Service
class OrderService(
    private val orderRepository: OrderRepository
) {
    fun createOrder(userId: Long, request: OrderRequestDTO): OrderDTO {
        val order = Order(userId = userId, totalAmount = request.totalAmount)
        return orderRepository.save(order).toDTO()
    }

    fun getOrdersByUserId(userId: Long): List<OrderDTO> {
        return orderRepository.findByUserId(userId).map { it.toDTO() }
    }
}
```

**Конфигурация Spring Security (Order Service):**

```kotlin
@Configuration
@EnableWebSecurity
class SecurityConfig : WebSecurityConfigurerAdapter() {

    @Bean
    fun jwtDecoder(): JwtDecoder {
        val secret = "your-256-bit-secret" // Тот же секрет, что в User Service
        return NimbusJwtDecoder.withSecretKey(
            SecretKeySpec(
                secret.toByteArray(),
                "HmacSHA256"
            )
        ).build()
    }

    override fun configure(http: HttpSecurity) {
        http
            .csrf().disable()
            .authorizeRequests()
            .antMatchers("/api/orders/**").authenticated()
            .and()
            .oauth2ResourceServer()
            .jwt()
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }
}
```

**application.yml (Order Service):**

```yaml
jwt:
  secret: your-256-bit-secret
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/orderdb
    username: user
    password: password
  jpa:
    hibernate:
      ddl-auto: update
```

**Что происходит:**

- Запросы к Order Service (например, POST `/api/orders`) содержат заголовок
  `Authorization: Bearer <token>`.
- Spring Security в Order Service использует `JwtDecoder` для проверки подписи
  токена и его срока действия.
- Если токен валиден, Spring Security извлекает `Jwt` объект, который содержит
  `subject` (например, `userId`) и `claims` (например, роли).
- Аннотация `@PreAuthorize` проверяет роли или соответствие `userId` из токена
  запрошенному ресурсу.
- Если токен недействителен или отсутствует, возвращается 401 Unauthorized.

---

### 3. **Ключевые моменты**

- **Секретный ключ:** User Service и Order Service должны использовать **один и
  тот же секрет** для подписи и проверки JWT. В реальной системе храните секрет
  в безопасном месте (например, HashiCorp Vault или Spring Cloud Config).
- **Роли и доступ:** JWT может содержать роли (`ROLE_USER`, `ROLE_ADMIN`),
  которые проверяются в Order Service через аннотации `@PreAuthorize` или
  `@Secured`.
- **Синхронизация:** Если User Service и Order Service работают в распределенной
  среде, используйте **Eureka** или **Consul** для обнаружения сервисов и *
  *Spring Cloud Config** для централизованного хранения секретов.
- **Асинхронное взаимодействие:** Как вы упомянули, Order Service может
  отправлять события в Kafka (например, "order_created"), а User Service
  подписывается на них для обновления данных. Это не связано с JWT, но дополняет
  архитектуру.

**Пример сценария:**

1. Пользователь отправляет логин/пароль на `/api/auth/login` (User Service).
2. User Service проверяет данные, генерирует JWT и возвращает его клиенту.
3. Клиент отправляет запрос на `/api/orders` (Order Service) с заголовком
   `Authorization: Bearer <token>`.
4. Order Service проверяет токен через Spring Security, извлекает `userId` и
   создает заказ.
5. Order Service публикует событие "order_created" в Kafka.
6. User Service получает событие через Kafka и обновляет, например, историю
   заказов пользователя.

---

### 4. **Дополнительные рекомендации**

- **Безопасность:** Используйте HTTPS для всех запросов, чтобы защитить JWT от
  перехвата. Храните секреты в безопасном хранилище (Vault, AWS Secrets
  Manager).
- **Токены с коротким сроком жизни:** Настройте короткий срок действия JWT (
  например, 1 час) и используйте refresh-токены для продления сессий.
- **Circuit Breaker:** При вызове Order Service из User Service (по REST)
  используйте Resilience4j, чтобы обработать сбои.
- **Мониторинг:** Используйте Spring Cloud Sleuth + Zipkin для трассировки
  запросов через сервисы, чтобы видеть, как JWT передается и проверяется.

### 5. **Ответ на ваш вопрос**

Да, **Spring Security в User Service выдает JWT**, а **Spring Security в Order
Service проверяет его**. Это стандартный подход в микросервисной архитектуре,
где один сервис (обычно Auth Service или User Service) выступает как эмиттер
токенов, а другие сервисы — как Resource Servers, проверяющие токены. Такой
подход обеспечивает централизованную аутентификацию и распределенную
авторизацию, сохраняя независимость сервисов.

## Проверка токена на сервисе B полученного от сервиса A

В **Order Service**, проверка **JWT-токенов**, выданных **User Service**,
осуществляется с использованием **Spring Security** в режиме **OAuth2 Resource
Server**. Это позволяет Order Service выступать как защищенный ресурс, который
валидирует токены, отправленные в заголовке `Authorization: Bearer <token>`, и
извлекает из них информацию о пользователе (например, `userId`, роли). Я
подробно объясню, как это работает, включая конфигурацию, код на Kotlin и
ключевые аспекты проверки токенов.

### 1. **Как Order Service проверяет JWT-токены**

JWT (JSON Web Token) состоит из трех частей: **Header**, **Payload** и *
*Signature**, закодированных в Base64 и разделенных точками (`.`). Проверка
токена в Order Service включает следующие шаги:

1. **Извлечение токена**: Spring Security перехватывает HTTP-запросы и извлекает
   токен из заголовка `Authorization` (формата `Bearer <token>`).
2. **Валидация подписи**: Проверяется, что токен подписан правильным секретным
   ключом (или публичным ключом в случае асимметричного шифрования), чтобы
   убедиться, что токен не подделан.
3. **Проверка срока действия**: Проверяется поле `exp` (expiration) в Payload,
   чтобы убедиться, что токен не истек.
4. **Извлечение данных**: Если токен валиден, из Payload извлекаются данные,
   такие как `sub` (subject, обычно `userId` или `email`) и `claims` (например,
   `roles`).
5. **Авторизация**: На основе данных токена (например, роли) Spring Security
   проверяет, имеет ли пользователь доступ к запрошенному ресурсу (через
   аннотации `@PreAuthorize` или конфигурацию).

Spring Security предоставляет готовые механизмы для этих шагов через модуль
`spring-security-oauth2-resource-server`, который поддерживает JWT.

---

### 2. **Конфигурация Spring Security в Order Service**

Для проверки JWT в Order Service нужно настроить Spring Security как **OAuth2
Resource Server**. В случае симметричного шифрования (когда используется общий
секретный ключ), Order Service должен знать тот же секрет, который использовался
в User Service для подписи токена.

**Пример конфигурации (Kotlin):**

```kotlin
@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun jwtDecoder(): JwtDecoder {
        // Тот же секрет, что в User Service
        val secret = "your-256-bit-secret" // Хранить в конфигурации или Vault
        val key = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
        return NimbusJwtDecoder.withSecretKey(key).build()
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() } // Отключаем CSRF для REST API
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/orders/**")
                    .authenticated() // Все запросы требуют аутентификации
                    .anyRequest()
                    .permitAll() // Другие эндпоинты открыты (если нужно)
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.decoder(jwtDecoder()) // Указываем декодер для JWT
                }
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Без сессий
            }
        return http.build()
    }
}
```

**Объяснение конфигурации:**

- **`JwtDecoder`**: Используется библиотека Nimbus для проверки JWT. В примере
  применяется симметричное шифрование (общий секрет). Для асимметричного
  шифрования (RSA) используется публичный ключ.
- **`oauth2ResourceServer`**: Настраивает Order Service как Resource Server,
  который ожидает JWT в заголовке `Authorization`.
- **`authorizeHttpRequests`**: Указывает, что эндпоинты `/api/orders/**` требуют
  аутентификации. Можно добавить проверки ролей, например, `hasRole('USER')`.
- **`sessionManagement`**: Устанавливает `STATELESS`, так как JWT не требует
  хранения сессий на сервере.

**application.yml (Order Service):**

```yaml
jwt:
  secret: your-256-bit-secret # Должен совпадать с User Service
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/orderdb
    username: user
    password: password
  jpa:
    hibernate:
      ddl-auto: update
```

---

### 3. **Пример обработки запросов в Order Service**

Когда клиент (или User Service) отправляет запрос к Order Service, Spring
Security автоматически проверяет токен. Если токен валиден, данные пользователя
доступны через объект `Jwt` в контроллере.

**Пример контроллера (Kotlin):**

```kotlin
@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService
) {
    @PostMapping
    @PreAuthorize("hasRole('USER')") // Требуется роль USER
    fun createOrder(
        @RequestBody orderRequest: OrderRequestDTO,
        @AuthenticationPrincipal jwt: Jwt // Извлекаем JWT
    ): OrderDTO {
        val userId = jwt.subject.toLong() // Извлекаем userId из токена
        return orderService.createOrder(userId, orderRequest)
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') and #userId == principal.subject") // Проверяем, что userId совпадает с JWT
    fun getOrdersByUserId(
        @PathVariable userId: Long,
        @AuthenticationPrincipal jwt: Jwt
    ): List<OrderDTO> {
        return orderService.getOrdersByUserId(userId)
    }
}

// Сервис заказов
@Service
class OrderService(
    private val orderRepository: OrderRepository
) {
    fun createOrder(userId: Long, request: OrderRequestDTO): OrderDTO {
        val order = Order(userId = userId, totalAmount = request.totalAmount)
        return orderRepository.save(order).toDTO()
    }

    fun getOrdersByUserId(userId: Long): List<OrderDTO> {
        return orderRepository.findByUserId(userId).map { it.toDTO() }
    }
}

// Модель данных
data class OrderRequestDTO(val totalAmount: BigDecimal)
data class OrderDTO(val id: Long, val userId: Long, val totalAmount: BigDecimal)

@Entity
data class Order(
    @Id @GeneratedValue val id: Long = 0,
    val userId: Long,
    val totalAmount: BigDecimal,
    val status: String = "PENDING"
) {
    fun toDTO() = OrderDTO(id, userId, totalAmount)
}
```

**Что происходит в коде:**

- **Извлечение JWT**: Аннотация `@AuthenticationPrincipal` позволяет получить
  объект `Jwt`, содержащий данные токена (`subject`, `claims`).
- **Проверка ролей**: Аннотация `@PreAuthorize` проверяет, есть ли у
  пользователя роль `USER` (из `claims` токена) и совпадает ли `userId` с
  `subject` в токене.
- **Логика**: Если токен валиден, Order Service выполняет запрос (например,
  создает заказ или возвращает список заказов).

---

### 4. **Как токен проверяется на уровне Spring Security**

1. **Фильтр `JwtAuthenticationFilter`**:

- Spring Security автоматически добавляет фильтр для обработки заголовка
  `Authorization`.
- Если заголовок содержит `Bearer <token>`, фильтр передает токен в `JwtDecoder`
  для проверки.

2. **Валидация подписи**:

- `JwtDecoder` использует секретный ключ (или публичный ключ для RSA) для
  проверки подписи токена.
- Если подпись неверна, выбрасывается исключение `BadJwtException` → HTTP 401.

3. **Проверка срока действия**:

- Проверяется поле `exp` в Payload. Если токен истек, выбрасывается
  `JwtExpiredException` → HTTP 401.

4. **Извлечение данных**:

- Если токен валиден, создается объект `JwtAuthenticationToken`, содержащий
  `Jwt` (с `subject` и `claims`) и список `GrantedAuthority` (ролей, извлеченных
  из `claims`, например, `ROLE_USER`).

5. **Авторизация**:

- Spring Security применяет правила доступа (из `@PreAuthorize` или
  конфигурации) на основе `JwtAuthenticationToken`.

---

### 5. **Ключевые аспекты и рекомендации**

- **Общий секрет**: Order Service и User Service должны использовать один и тот
  же секрет для подписи/проверки JWT. Храните его безопасно (например, в
  HashiCorp Vault или Spring Cloud Config).
- **Асимметричное шифрование**: Для большей безопасности можно использовать
  RSA (User Service подписывает токен приватным ключом, Order Service проверяет
  публичным). Это требует настройки `JwtDecoder` с публичным ключом:
  ```kotlin
  @Bean
  fun jwtDecoder(): JwtDecoder {
      return NimbusJwtDecoder.withPublicKey(rsaPublicKey).build()
  }
  ```
- **Токены с коротким сроком жизни**: Установите короткий срок действия (
  например, 15 минут) и используйте refresh-токены, чтобы минимизировать риски
  при утечке.
- **HTTPS**: Все запросы должны идти через HTTPS, чтобы защитить токен от
  перехвата.
- **Мониторинг**: Используйте Spring Cloud Sleuth + Zipkin для трассировки
  запросов с JWT через сервисы. Это помогает отследить, где токен не прошел
  проверку.
- **Circuit Breaker**: Если Order Service запрашивает данные у User Service (по
  REST), используйте Resilience4j, чтобы обработать сбои.
- **Ошибки**: Spring Security автоматически возвращает:
    - HTTP 401 (Unauthorized) — если токен отсутствует или недействителен.
    - HTTP 403 (Forbidden) — если токен валиден, но пользователь не имеет нужных
      прав (например, роль не соответствует).

---

### 6. **Пример сценария**

1. Пользователь логинится через User Service (`POST /api/auth/login`), получает
   JWT: `eyJhbGciOiJIUzI1NiIs...`.
2. Пользователь отправляет запрос в Order Service (`POST /api/orders`) с
   заголовком `Authorization: Bearer eyJhbGciOiJIUzI1NiIs...`.
3. Spring Security в Order Service:

- Извлекает токен.
- Проверяет подпись с помощью секрета `your-256-bit-secret`.
- Проверяет, что токен не истек.
- Извлекает `subject` (например, `userId=123`) и `claims` (например,
  `roles=["ROLE_USER"]`).
- Проверяет `@PreAuthorize("hasRole('USER')")`.

4. Если все проверки пройдены, запрос выполняется, и создается заказ для
   `userId=123`.

---

### 7. **Ответ на ваш вопрос**

**Order Service проверяет JWT-токены** с помощью Spring Security, настроенного
как OAuth2 Resource Server. Он использует тот же секретный ключ (или публичный
ключ для RSA), что и User Service, чтобы валидировать подпись и срок действия
токена. Данные из токена (например, `userId`, роли) извлекаются через объект
`Jwt` и используются для авторизации. Конфигурация с `JwtDecoder` и фильтрами
Spring Security автоматизирует этот процесс, делая его прозрачным для
разработчика, а аннотации вроде `@PreAuthorize` позволяют легко управлять
доступом.