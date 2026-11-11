# 💰 Pay Service

**간편결제 시스템** — 지갑 충전 · 송금 · 결제/취소를 다루는 Spring Boot 기반 백엔드 서비스

금융 도메인의 핵심 난제인 **멱등성**, **동시성 제어**, **외부 API 장애 대응**, **데이터 정합성 보정**을
실제 동작하는 코드로 구현하는 데 초점을 맞춘 개인 프로젝트입니다.

---

## 목차

- [기술 스택](#기술-스택)
- [아키텍처](#아키텍처)
- [핵심 구현](#핵심-구현)
- [API](#api)
- [데이터 모델](#데이터-모델)
- [모니터링 · 부하 테스트](#모니터링--부하-테스트)
- [실행 방법](#실행-방법)
- [테스트](#테스트)

---

## 기술 스택

| 구분 | 사용 기술 |
|:---|:---|
| **Language / Runtime** | Java 17 |
| **Framework** | Spring Boot 3.3.2 (Web, Data JPA, Security, Validation) |
| **Database** | MySQL 8.0, Flyway (스키마 버전 관리) |
| **Lock** | Redis (ShedLock 분산 스케줄러 락) |
| **인증** | Spring Security + JWT (jjwt 0.11.5), BCrypt |
| **장애 대응** | Resilience4j (Circuit Breaker, Retry), Spring Retry |
| **외부 연동** | Toss Payments API (RestClient) |
| **모니터링** | Micrometer, Prometheus, Grafana, Spring Actuator |
| **문서화** | springdoc-openapi (Swagger UI) |
| **테스트** | JUnit 5, Mockito, H2, ArchUnit, k6 |
| **인프라** | Docker, Docker Compose, GitHub Actions |

---

## 아키텍처

### 헥사고날 아키텍처 (Ports & Adapters)

도메인 로직을 프레임워크·인프라로부터 분리하기 위해 헥사고날 구조를 적용했습니다.
도메인 객체는 JPA 엔티티와 분리되어 있으며, 지갑·결제 컨텍스트의 도메인(`Wallet`, `Payment`, `Transfer`, `Ledger`)은
**불변(immutable)** 으로 설계되어 상태 변경 시 새 인스턴스를 반환합니다.

```
com.pay
├── global/                        # 공통 관심사 (도메인 의존 없음)
│   ├── config/                    # Security, Swagger, ShedLock, Retry, JPA Auditing
│   ├── constant/                  # API URL 상수
│   ├── exception/                 # @RestControllerAdvice 전역 예외 처리
│   ├── jwt/                       # JWT 검증기
│   └── response/                  # ApiResponse<T> 공통 응답 포맷
│
├── user/                          # 회원 컨텍스트
│   ├── adapter/
│   │   ├── in/security/           # JWT 인증 필터, UserDetails
│   │   ├── in/web/v1/             # Controller, Request/Response DTO
│   │   ├── out/database/          # JPA Entity, Repository Adapter
│   │   └── out/jwt/               # JWT 발급기 (TokenProvider 구현)
│   ├── application/
│   │   ├── port/in/               # UseCase 인터페이스, Command
│   │   ├── port/out/              # Repository / TokenProvider 인터페이스
│   │   └── service/               # UseCase 구현
│   └── domain/                    # User, UserCreatedEventOutbox, 도메인 이벤트
│
└── wallet/                        # 지갑·결제 컨텍스트
    ├── adapter/
    │   ├── in/event/              # 회원가입 이벤트 리스너 (지갑 자동 생성)
    │   ├── in/scheduler/          # 지갑 생성 보상 배치, 결제 대사 배치
    │   ├── in/web/v1/             # Controller, DTO
    │   ├── out/database/          # JPA Entity, Repository Adapter
    │   └── out/tosspayments/      # Toss Payments 어댑터 (+ 부하테스트용 Fake)
    ├── application/
    │   ├── port/in/ · port/out/
    │   └── service/               # 충전, 송금, 결제, 환불, 대사
    └── domain/                    # Wallet, Payment, Transfer, Ledger
```

### 아키텍처 규칙 자동 검증

구조가 시간이 지나도 무너지지 않도록 **ArchUnit 테스트로 의존 방향을 강제**합니다.
(`src/test/java/com/pay/architecture/HexagonalArchitectureTest.java`)

- `domain` → `application` / `adapter` 의존 금지
- `application` → `adapter` 의존 금지
- `domain` → Spring Framework 의존 금지 (POJO 유지)
- `global` → 도메인 패키지(`user`, `wallet`) 의존 금지

---

## 핵심 구현

### 1. 멱등성 (Idempotency)

결제·송금은 네트워크 재시도로 인한 중복 처리가 곧 금전 사고로 이어집니다.
충전·송금은 **애플리케이션 레벨 검사 + DB UNIQUE 제약**을 이중으로 두어 중복을 차단했습니다.

- 클라이언트가 `Idempotency-Key` 헤더로 UUID를 전달
- 서버는 원장·송금 기록에 `userId::transactionId` 형태로 **네임스페이싱**하여 저장
  → 다른 사용자가 동일 UUID를 보내도 충돌하지 않음
- `ledger.transaction_id`, `transfer.transaction_id`에 UNIQUE 제약
  → 애플리케이션 검사를 통과한 동시 요청도 DB가 최종 방어
- 결제 승인은 `payment.order_id` UNIQUE 제약으로 중복 저장을 막음
- 충전 API는 키 형식(UUID)을 검증하고, **UUIDv7**의 타임스탬프로 **4일 만료**까지 검증 (`WalletChargeController`)

### 2. 동시성 제어

| 기능 | 락 전략 | 이유 |
|:---|:---|:---|
| 충전 | 비관적 락 (`SELECT ... FOR UPDATE`) | 잔액 갱신 충돌을 DB 레벨에서 직렬화 |
| 송금 | 비관적 락 (두 지갑) | 출금·입금이 하나의 원자 단위여야 함 |
| 환불 | 낙관적 락 (`@Version`) | 버전 충돌 시 `OptimisticLockingFailureException`으로 감지 |

`wallet` 테이블은 `@Version` 컬럼을 두어 낙관적 락을 함께 적용하며,
`OptimisticLockingFailureException` 발생 시 `@Retryable`로 최대 3회 재시도합니다
(충전: 지수 백오프 50ms × 2 / 환불: 1초 고정 간격).

**데드락 방지** — 송금은 두 개의 지갑을 잠그기 때문에 A→B와 B→A 요청이 동시에 들어오면 데드락이 발생합니다.
이를 `userId` 오름차순으로 락 획득 순서를 고정해 해결했습니다.

```java
// TransferService — 모든 트랜잭션이 동일한 순서로 lock 획득
Long firstId  = Math.min(senderUserId, receiverUserId);
Long secondId = Math.max(senderUserId, receiverUserId);

Wallet firstWallet  = walletRepository.findByUserIdForUpdate(firstId)...
Wallet secondWallet = walletRepository.findByUserIdForUpdate(secondId)...
```

### 3. 외부 API 호출과 트랜잭션 분리

결제 승인은 외부 Toss API를 호출하므로, 트랜잭션 안에서 호출하면
**응답을 기다리는 동안 DB 커넥션을 점유**하여 커넥션 풀이 고갈됩니다.

`TransactionTemplate`을 사용해 트랜잭션 경계를 쪼개고, 외부 호출을 트랜잭션 **바깥**에 두었습니다.

```
[TX1] Payment 저장 (READY)      ← 짧은 트랜잭션
  ↓
      Toss 결제 승인 API 호출     ← 트랜잭션 밖 (커넥션 미점유)
  ↓
[TX2] 지갑 충전 + 원장 기록
  ↓
[TX3] Payment 상태 변경 (APPROVED)
```

### 4. Circuit Breaker + Retry

Toss API 어댑터에 Resilience4j를 적용해 외부 장애가 서비스 전체로 전파되지 않도록 차단했습니다.

```yaml
resilience4j:
  circuitbreaker:
    instances:
      toss:
        sliding-window-size: 20
        minimum-number-of-calls: 10
        failure-rate-threshold: 50        # 실패율 50% 초과 시 OPEN
        wait-duration-in-open-state: 10s
        permitted-number-of-calls-in-half-open-state: 3
  retry:
    instances:
      toss:
        max-attempts: 3
        wait-duration: 500ms
        retry-exceptions:                 # 5xx / 네트워크 오류만 재시도
          - org.springframework.web.client.HttpServerErrorException
          - org.springframework.web.client.ResourceAccessException
          - java.io.IOException
```

Circuit이 OPEN되면 `CallNotPermittedException`을 전역 예외 처리기가 받아
**`503 Service Unavailable` + `Retry-After: 10`** 으로 응답합니다.
4xx는 재시도 대상에서 제외해 무의미한 호출을 막았습니다.

### 5. 결제 대사 (Reconciliation)

Toss 승인은 성공했지만 이후 지갑 충전(TX2)이나 상태 변경(TX3)이 실패하거나 그 사이 서버가 중단되면,
결제는 `READY` 상태로 남아 **"결제는 승인됐는데 지갑은 충전되지 않은"** 정합성 깨짐이 발생할 수 있습니다.
(Toss 호출 자체가 예외로 끝난 경우는 즉시 `FAILED`로 처리되어 대사 대상이 아닙니다.)

5분마다 도는 배치가 이를 보정합니다.

1. 생성된 지 5분 이상 지난 `READY` 상태 결제 조회
2. Toss 결제 조회 API로 **실제 상태** 확인
3. 상태에 따라 분기 처리

| Toss 상태 | 처리 |
|:---|:---|
| `DONE` | `APPROVE` — 결제 승인 처리 + 지갑 충전 |
| `ABORTED` / `EXPIRED` / `CANCELED` / `PARTIAL_CANCELED` | `FAIL` — 결제 실패 확정 |
| `READY` / `IN_PROGRESS` / `WAITING_FOR_DEPOSIT` | `SKIP` — 다음 배치로 이월 |

**ShedLock(Redis)** 으로 잠금을 걸어 다중 인스턴스 환경에서도 배치가 한 번만 실행됩니다.

### 6. Outbox 패턴 — 지갑 자동 생성

회원가입 시 지갑이 함께 생성되어야 하지만, 지갑 생성 실패가 회원가입을 롤백시켜서는 안 됩니다.

1. 회원가입 트랜잭션에서 `user_created_event_outbox`에 `PENDING` 레코드 저장
2. `@TransactionalEventListener` + `@Async`로 커밋 이후 비동기 지갑 생성 → 성공 시 `COMPLETED`
3. 이벤트가 유실되거나 실패하면 **30초 주기 보상 스케줄러**가 `PENDING` 건을 재처리

비동기 처리로 회원가입 응답 속도를 지키면서도, Outbox 테이블로 최종 일관성을 보장합니다.

### 7. 원장(Ledger) 기반 잔액 추적

모든 잔액 변동은 원장에 append-only로 기록됩니다.

- `balance_snapshot` — 거래 직후 잔액을 함께 저장해 시점별 잔액 추적 가능
- `transaction_id` UNIQUE — 원장 자체가 멱등성 저장소 역할
- `reference_type` (`CHARGE` / `REMITTANCE` / `REFUND`) + `reference_id`로 거래 출처 역추적
- 송금 1건은 출금(`DEBIT`)·입금(`CREDIT`) **2개의 원장**으로 복식부기 형태 기록

---

## API

Swagger UI: `http://localhost:8080/swagger-ui.html`

| 분류 | Method | Endpoint | 인증 | 설명 |
|:---|:---|:---|:---:|:---|
| 회원 | `POST` | `/api/v1/users/signUp` | — | 회원가입 (가입 시 지갑 자동 생성) |
| 회원 | `POST` | `/api/v1/users/login` | — | 로그인 (JWT 발급) |
| 회원 | `GET` | `/api/v1/users/me` | ✅ | 내 정보 조회 |
| 지갑 | `POST` | `/api/v1/wallets/charge` | ✅ | 잔액 충전 (`Idempotency-Key` 필수) |
| 송금 | `POST` | `/api/v1/transfer` | ✅ | 사용자 간 송금 (`Idempotency-Key` 필수) |
| 결제 | `POST` | `/api/v1/payments/confirm` | ✅ | Toss 결제 승인 |
| 결제 | `GET` | `/api/v1/payments/find` | ✅ | 내 결제 내역 조회 |
| 결제 | `POST` | `/api/v1/payments/cancel` | ✅ | 결제 취소 (전액 / 부분) |

모든 응답은 공통 포맷으로 감싸집니다.

```json
{
  "status": "SUCCESS",
  "message": "충전이 완료되었습니다.",
  "data": 15000
}
```

---

## 데이터 모델

Flyway로 버전 관리되며, `ddl-auto: validate`로 엔티티와 스키마의 불일치를 기동 시점에 검증합니다.

```
users ──┬── wallet ─── ledger
        │
        ├── transfer (sender / receiver)
        ├── payment
        └── user_created_event_outbox
```

| 테이블 | 역할 | 주요 제약 |
|:---|:---|:---|
| `users` | 회원 | `email`, `phone_number`, `external_id` UNIQUE |
| `wallet` | 지갑·잔액 | `user_id` UNIQUE, `version` (낙관적 락) |
| `ledger` | 원장 (모든 잔액 변동) | `transaction_id` UNIQUE, `balance_snapshot` |
| `transfer` | 송금 | `transaction_id` UNIQUE, 송신/수신 인덱스 |
| `payment` | 결제 | `payment_key` · `order_id` UNIQUE, 원본 응답 JSON 보관 |
| `user_created_event_outbox` | 지갑 생성 Outbox | `status` (PENDING / COMPLETED) |

금액은 부동소수점 오차를 피하기 위해 전부 `DECIMAL(19, 4)` / `BigDecimal`을 사용합니다.

---

## 모니터링 · 부하 테스트

### 커스텀 메트릭

Micrometer로 결제 파이프라인의 지표를 직접 수집합니다.

| 메트릭 | 타입 | 설명 |
|:---|:---|:---|
| `payment.confirm.count` | Counter | 결제 승인 성공/실패 횟수 (`result` 태그) |
| `payment.confirm.duration` | Timer | 결제 승인 전체 처리 시간 |
| `payment.toss.api.duration` | Timer | Toss API 호출 소요 시간 |
| `payment.status.count` | Counter | 결제 상태 전이 횟수 (`status` 태그) |

percentile histogram을 활성화해 Grafana에서 p95 / p99를 확인할 수 있습니다.

```bash
docker compose -f docker-compose.monitoring.yml up -d
# Prometheus  http://localhost:9090
# Grafana     http://localhost:3000
```

### 부하 테스트

외부 API에 의존하지 않고 장애 상황을 재현하기 위해 **`FakeTossPaymentsAdapter`** 를 두었습니다.
`loadtest` 프로파일에서만 활성화되며, 설정값으로 **지연 시간과 실패율을 주입**할 수 있습니다.

```yaml
# application-loadtest.yml
fake-toss:
  failure-rate: 0.80      # 80% 실패 — Circuit Breaker 동작 검증용
  min-latency-ms: 30
  max-latency-ms: 150
```

```bash
k6 run k6/warmup.js        # JIT 워밍업
k6 run k6/charge-test.js   # 로그인 + 결제 승인(지갑 충전) 시나리오 (50 → 300 VU 단계적 증가)
k6 run k6/load-test.js     # 로그인 + 결제 내역 조회 시나리오
```

---

## 실행 방법

### 요구 사항

- JDK 17
- Docker / Docker Compose

### 1. 인프라 기동

```bash
docker compose up -d       # MySQL, Redis, Kafka, Zookeeper
```

> Kafka / Zookeeper는 이벤트 발행 확장을 위해 구성만 되어 있으며, 현재 애플리케이션 로직에서는 사용하지 않습니다.

### 2. 로컬 설정 파일 생성

```bash
cp src/main/resources/application-local.yml.example \
   src/main/resources/application-local.yml
```

생성된 파일에서 DB 접속 정보를 환경에 맞게 수정합니다.
`application-local.yml`은 `.gitignore`에 등록되어 있어 커밋되지 않습니다.

### 3. 환경 변수 설정

민감 정보는 설정 파일에 두지 않고 환경 변수로 주입합니다.

| 변수 | 설명 |
|:---|:---|
| `JWT_SECRET` | JWT 서명 키 (Base64 인코딩, 512bit 이상 권장) |
| `TOSS_SECRET_KEY` | Toss Payments 시크릿 키 |

```bash
# JWT 시크릿 생성 예시
openssl rand -base64 64
```

```bash
export JWT_SECRET="<생성한 값>"
export TOSS_SECRET_KEY="<Toss 개발자센터에서 발급받은 test_sk_... 키>"
```

### 4. 애플리케이션 실행

```bash
./gradlew bootRun
```

기동 시 Flyway가 마이그레이션을 자동 적용합니다.

- API 문서 — http://localhost:8080/swagger-ui.html
- 결제 테스트 페이지 — http://localhost:8080/tossPayment.html

### 운영 프로파일 (`prod`)

```bash
DB_HOST=... DB_NAME=... DB_USERNAME=... DB_PASSWORD=... \
REDIS_HOST=... REDIS_PASSWORD=... \
JWT_SECRET=... TOSS_SECRET_KEY=... \
java -jar build/libs/pay-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

Docker 이미지 빌드는 멀티 스테이지로 구성되어 있습니다.

```bash
docker build -t pay-service .
```

---

## 테스트

```bash
./gradlew test
```

| 유형 | 대상 |
|:---|:---|
| 도메인 단위 테스트 | `Wallet`, `Transfer`, `UserCreatedEventOutbox` 등 불변식 검증 |
| 서비스 단위 테스트 | Mockito 기반 UseCase 검증 (충전, 송금, 회원, 인증) |
| 컨트롤러 테스트 | `@WebMvcTest` + Spring Security Test |
| 아키텍처 테스트 | ArchUnit 의존 방향 규칙 4종 |

`develop` 브랜치로 PR이 생성되면 GitHub Actions가 MySQL·Redis 컨테이너를 띄우고 전체 빌드를 실행합니다.
(`.github/workflows/ci.yml`)
