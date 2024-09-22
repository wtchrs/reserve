# Reserve-backend

## Example of `src/main/resources/application.yml`

```yml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://your-database.com:3306/db?serverTimezone=UTC
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
      host: your-redis.com
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
      accessTokenSecretKey: <64 characters>
      refreshTokenSecretKey: <64 characters>
      accessTokenExpire: 600
      refreshTokenExpire: 604800
  cors:
    allowedOrigins: https://example-domain1.com, https://example-domain2.com

# OpenAPI
springdoc:
  swagger-ui:
    groups-order: asc
    tags-sorter: alpha
    operations-sorter: alpha
  paths-to-match:
    - /v1/**
  override-with-generic-response: off
```

See [springdoc.org](https://springdoc.org/#properties) for more information about Springdoc OpenAPI configuration.

## Create a PKCS #12 certificate using Certbot

```bash
docker run -it --rm \
  -v ${pwd}/certbot/etc/letsencrypt:/etc/letsencrypt \
  -v ${pwd}/certbot/var/lib/letsencrypt:/var/lib/letsencrypt \
  certbot/certbot certonly --manual \
  --preferred-challenges dns \
  --email your_email@example.com \
  --domain '*.your-domain.com' \
  --server https://acme-v02.api.letsencrypt.org/directory \
  --agree-tos

openssl pkcs12 -export \
  -in certbot/etc/letsencrypt/archive/your-domain.com/fullchain1.pem \
  -inkey certbot/etc/letsencrypt/archive/your-domain.com/privkey1.pem \
  -out src/main/resources/reserve.p12
```
