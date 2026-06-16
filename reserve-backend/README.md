# Reserve-backend

## Getting started

### Prerequisites

#### `application.yml`

Add `src/main/resources/application.yml`:

```yml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://<your-database-url>:3306/db?serverTimezone=UTC
    username: username
    password: password

  flyway:
    enabled: true
    baseline-on-migrate: true

  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
        default_batch_fetch_size: 20

  data:
    redis:
      host: your-redis.com # your redis url
      password: password
      port: 6379

  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 50MB

server:
  error:
    include-stacktrace: never
  ssl:
    key-store-type: PKCS12
    key-store: classpath:reserve.p12
    key-store-password: reserve
    key-alias: reserve

logging:
  level:
    root: info
    sql: debug
    org.hibernate.orm.jdbc.bind: trace

application:
  matchThreshold: 0
  security:
    jwt:
      accessTokenSecretKey: <generated-secret>
      refreshTokenSecretKey: <generated-secret>
      accessTokenExpire: 600
      refreshTokenExpire: 604800
  cors:
    allowedOrigins: https://example-domain1.com, https://example-domain2.com

# OpenAPI
springdoc:
  api-docs:
    path: '/api/v3/api-docs'
  swagger-ui:
    groups-order: asc
    tags-sorter: alpha
    operations-sorter: alpha
    path: '/api/swagger-ui'
  paths-to-match:
    - /v1/**
  override-with-generic-response: off
```

See [springdoc.org](https://springdoc.org/#properties) for more information about Springdoc OpenAPI configuration.

#### Launch MySQL and Redis with docker

```bash
docker run -d --name reserve-mysql \
  -e MYSQL_DATABASE=db \
  -e MYSQL_USER=username \
  -e MYSQL_PASSWORD=password \
  -e MYSQL_ROOT_PASSWORD=rootpassword \
  -p 3306:3306 \
  mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci

docker run -d --name reserve-redis \
  -p 6379:6379 \
  redis:7-alpine redis-server --requirepass password
```

Update `src/main/resources/application.yml`:

```yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/db?serverTimezone=UTC
    # ...
  data:
    redis:
      host: localhost
      # ...
```

#### Generate access/refresh token secret keys

Run twice:

```bash
openssl rand -base64 64
# or without newline:
openssl rand -base64 64 | tr -d '\n'
```

and paste each generated value to `application.yml`:

```yml
application:
  security:
    jwt:
      accessTokenSecretKey: <generated-secret> # replace
      refreshTokenSecretKey: <generated-secret> # replace
      # ...
```

#### Create a PKCS #12 certificate using Certbot

Use certbot docker image to generate a certificate:

```bash
docker run -it --rm \
  -v ${PWD}/certbot/etc/letsencrypt:/etc/letsencrypt \
  -v ${PWD}/certbot/var/lib/letsencrypt:/var/lib/letsencrypt \
  certbot/certbot certonly --manual \
  --preferred-challenges dns \
  --email <your-email-address> \
  --domain '*.<your-domain-url>' \
  --server https://acme-v02.api.letsencrypt.org/directory \
  --agree-tos
```

Generate a PKCS #12 certificate from the pem file:

```bash
openssl pkcs12 -export \
  -in certbot/etc/letsencrypt/archive/<your-domain-url>/fullchain1.pem \
  -inkey certbot/etc/letsencrypt/archive/<your-domain-url>/privkey1.pem \
  -out src/main/resources/reserve.p12 \
  -name reserve \
  -passout pass:reserve
```

### Test

```bash
./gradlew test
```

### Launch the spring server

```bash
./gradlew bootRun
```
