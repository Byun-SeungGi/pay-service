# Pay Service

대규모 트래픽을 고려한 고신뢰성 간편결제 시스템

> 금융 데이터의 무결성(Integrity)과 대규모 트래픽 확녕에서의 안정성(Reliability)을 최우선으로 설계한 핀테크 백엔드 시스템

---

## 프로젝트 소개

사용자가 가상의 지갑(Wallet)에 금액을 충전하고, 타인에게 송금하거나 가맹점에서 결제할 수 있는 간편결제 서비스입니다.

### 핵심 기술적 해결 과제

과제 : 해결방법

| 동시성 제어   | 락 전략 이원화 | 개인 충전은 충돌이 적어 `Optimistic Lock`(@Version), 다건 송금은 정합성이 중요하여 `Pessimistic Lock`(For Update) 적용 |
| 분산 트랜잭션 | Saga Pattern | MSA 환경을 고려하여 송금 실패 시 보상 트랜잭션(환불) 로직을 오케스트레이션 방식으로 구현 |
| 데이터 무결성 | 일 마감 대사   | 외부 은행 API 장애 시 '미확정(Unknown)' 상태를 도입하고, 매일 자정 배치(Batch)로 데이터 정합성 보정 |
| 조회 성능    | 원장 스냅샷    | 수억 건의 원장(Ledger) 데이터 중 당일 내역만 조회하도록 `Snapshot` 아키텍처 도입 |
| 트래픽 제어  | Redis 대기열   | 선착순 이벤트 시 `Sorted Set`을 이용한 토큰 버킷 알고리즘으로 유량 제어 (Traffic Shaping) |
---

## 기술 스택

구분 : 기술 : 선택이유

Language : Java17 : LTS 안정성, Record등 활용
Framework : Spring boot 3.2 : 검증된 버전, 빠른개발
Database : MySQL 8.0 : ACID 트랜잭션 보장(금융시스템 필수)
ORM : Spring Data JPA : Dirty Checking으로 상태 변경 관리
Query : QueryDSL : 런타임 에러방지 및 복잡한 쿼리 수행
Cache/Lock : Redis + Redission : Pub/Sub 방식 분산 락으로 리소스 최소화
Message Queue | Apachge Kafka : 결제/알림 분리/ 장애시 Replay 가능

## 프로젝트 구조

com.pay
├── global                # 전역 설정 (Security, Exception, Common)
└── domain
     ├── user             # 회원 관리 (User)
     ├── wallet           # 지갑 및 잔액 관리 (Wallet - 핵심 도메인)
     ├── remittance       # 송금 및 이체 트랜잭션 (Saga Pattern)
     └── ledger           # 원장 기록 및 대사 (Ledger, Snapshot)