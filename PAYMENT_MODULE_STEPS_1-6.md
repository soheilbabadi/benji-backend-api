# Payment Module Implementation Plan
## Steps 1-6: Architecture, Domain Model, and Database Schema

---

## Step 1: Requirements Analysis & Assumptions

### Identified Ambiguities and Resolutions

#### 1. Idempotency Key Strategy
**Challenge**: Prevent duplicate payment creation under concurrent requests.

**Solution**: Database-level idempotency key table with unique constraint.

**Approach**:
- Generate idempotency key from: `userId + serviceType + serviceReferenceId + timestamp_window`
- Store in `payment_idempotency_key` table with unique constraint on `idempotency_key`
- Use database transaction to atomically check-and-insert
- Keys expire after 24 hours to prevent indefinite locking
- If key exists, return existing payment transaction instead of creating new one

**Why this approach**:
- Database uniqueness constraint prevents race conditions
- Application-level checks alone are not sufficient under concurrency
- Expiration prevents resource leaks from abandoned payment flows

#### 2. Event Reliability (Transaction Outbox Pattern)
**Challenge**: Ensure `PaymentSucceededEvent` is never lost after marking payment SUCCESS.

**Solution**: Transactional Outbox Pattern.

**Approach**:
1. In same database transaction as payment status update:
   - Update `payment_transaction.status` to SUCCESS
   - Insert event record into `payment_event_outbox` table
2. Separate scheduler/poller reads unprocessed events from outbox
3. Publish events to Spring ApplicationEventPublisher
4. Mark events as processed after successful publication
5. Retry mechanism for failed publications

**Trade-offs**:
- ✅ Guarantees event persistence in same transaction as state change
- ✅ No external dependencies (Kafka/RabbitMQ not needed)
- ✅ Simple implementation suitable for modular monolith
- ⚠️ Requires polling mechanism (small delay vs. immediate delivery)
- ⚠️ Additional database load from polling

**Why not alternatives**:
- **Spring @TransactionalEventListener**: Can lose events if application crashes before listener execution
- **Kafka/RabbitMQ**: Over-engineering for current requirements, adds operational complexity
- **Simple event publishing**: Not reliable enough for financial transactions

#### 3. Money Representation
**Decision**: Use `Long` (minor units) instead of `BigDecimal`.

**Rationale**:
- IRR (Iranian Rial) has no decimal subdivision in practice
- ZarinPal API expects amounts as integers
- Avoids floating-point precision issues
- More efficient for database storage and indexing
- Clear semantic meaning: amount in smallest currency unit (Rials)

**Implementation**:
```java
// Amount stored as Long representing Rials
private Long amount; // e.g., 1000000 = 1,000,000 Rials
```

#### 4. Ownership Validation Design
**Challenge**: Payment module must not know about service-specific entities but must validate ownership.

**Solution**: Service modules validate ownership BEFORE requesting payment.

**Flow**:
```
User Request → Service Module (ExpertConsultation)
                ↓ Validates: consultation belongs to user
                ↓ Creates payment intent with validated data
                ↓
         Payment Module
                ↓ Trusts the validated intent
                ↓ Creates payment transaction
```

**Implementation Options**:

**Option A (Chosen)**: Service module calls payment service with pre-validated parameters
- Service module ensures `serviceReferenceId` belongs to authenticated user
- Payment module trusts the caller (internal module call)
- Simple, clear responsibility boundaries

**Option B**: Payment module calls back to service module for validation
- More complex, introduces circular dependencies
- Requires additional interfaces

**Decision**: Option A - Service modules are responsible for validating that the service reference belongs to the authenticated user before invoking the payment module.

#### 5. ZarinPal Callback Method
**ZarinPal Specification**: 
- User is redirected to ZarinPal gateway
- After payment, ZarinPal redirects user's browser to callback URL with GET parameters
- Server-to-server verification is done separately via POST API call

**Implementation**:
- Callback endpoint accepts GET requests (browser redirect)
- Verification request to ZarinPal uses POST (server-to-server)
- Idempotency handled by checking transaction status before processing

---

## Step 2: Package Structure

```
social.benji.benji_backend_api.payment/
├── domain/
│   ├── model/
│   │   ├── Tariff.java                    # Aggregate root for pricing
│   │   ├── PaymentTransaction.java        # Aggregate root for payments
│   │   └── PaymentEventOutbox.java        # Entity for outbox pattern
│   ├── valueobject/
│   │   ├── PaidServiceType.java           # Enum for service types
│   │   ├── PaymentStatus.java             # Enum for payment states
│   │   ├── PaymentGatewayType.java        # Enum for gateway providers
│   │   ├── PaymentEventType.java          # Enum for outbox events
│   │   ├── Money.java                     # Value object for monetary amounts
│   │   └── TariffSnapshot.java            # Value object for tariff snapshot
│   ├── repository/
│   │   ├── TariffRepository.java          # Repository interface
│   │   ├── PaymentTransactionRepository.java
│   │   └── PaymentEventOutboxRepository.java
│   └── service/
│       └── PaymentDomainService.java      # Domain logic for state transitions
│
├── application/
│   ├── dto/
│   │   ├── command/
│   │   │   ├── CreatePaymentCommand.java
│   │   │   ├── VerifyPaymentCommand.java
│   │   │   └── RefundPaymentCommand.java
│   │   └── response/
│   │       ├── PaymentResponse.java
│   │       ├── TariffResponse.java
│   │       └── PaymentUrlResponse.java
│   ├── inport/
│   │   ├── CreatePaymentUseCase.java
│   │   ├── VerifyPaymentUseCase.java
│   │   ├── GetPaymentUseCase.java
│   │   ├── ListPaymentsUseCase.java
│   │   └── RefundPaymentUseCase.java
│   ├── outport/
│   │   ├── PaymentGatewayPort.java        # Interface for gateway abstraction
│   │   └── PaymentEventPublisherPort.java # Interface for event publishing
│   └── service/
│       ├── TariffApplicationService.java
│       ├── PaymentApplicationService.java
│       └── PaymentEventProcessor.java     # Processes outbox events
│
├── infrastructure/
│   ├── persistence/
│   │   ├── entity/
│   │   │   ├── TariffEntity.java
│   │   │   ├── PaymentTransactionEntity.java
│   │   │   ├── PaymentIdempotencyKeyEntity.java
│   │   │   └── PaymentEventOutboxEntity.java
│   │   ├── repository/
│   │   │   ├── JpaTariffRepository.java
│   │   │   ├── JpaPaymentTransactionRepository.java
│   │   │   ├── JpaPaymentIdempotencyKeyRepository.java
│   │   │   └── JpaPaymentEventOutboxRepository.java
│   │   └── mapper/
│   │       ├── TariffMapper.java
│   │       └── PaymentTransactionMapper.java
│   ├── payment/
│   │   ├── zarinpal/
│   │   │   ├── ZarinPalPaymentGateway.java
│   │   │   ├── ZarinPalProperties.java
│   │   │   ├── ZarinPalApiService.java
│   │   │   └── dto/
│   │   │       ├── ZarinPalRequest.java
│   │   │       └── ZarinPalResponse.java
│   │   └── PaymentGatewayConfig.java
│   └── event/
│       ├── PaymentSucceededEvent.java     # Spring application event
│       ├── PaymentFailedEvent.java
│       └── SpringEventPublisherAdapter.java
│
└── api/
    ├── rest/
    │   ├── user/
    │   │   ├── PaymentUserController.java
    │   │   └── dto/
    │   │       ├── CreatePaymentRequest.java
    │   │       └── PaymentListResponse.java
    │   ├── admin/
    │   │   ├── TariffAdminController.java
    │   │   ├── PaymentAdminController.java
    │   │   └── dto/
    │   │       ├── CreateTariffRequest.java
    │   │       └── AdminPaymentResponse.java
    │   └── gateway/
    │       └── ZarinPalCallbackController.java
    └── config/
        └── PaymentSecurityConfig.java
```

---

## Step 3: Domain Model and Aggregate Boundaries

### Aggregate 1: Tariff

**Purpose**: Manages pricing for paid services with historical tracking.

**Invariants**:
- Only ONE active tariff per service type at any time
- Amount must be positive (> 0)
- Currency cannot be changed after creation
- When a tariff is activated, previous active tariff for same service type is deactivated

**Entities**:
```
Tariff (Aggregate Root)
├── id: UUID
├── serviceType: PaidServiceType
├── amount: Long (minor units)
├── currency: String (ISO 4217)
├── isActive: Boolean
├── createdAt: Instant
├── updatedAt: Instant
└── activatedAt: Instant (when became active)
```

### Aggregate 2: PaymentTransaction

**Purpose**: Tracks payment lifecycle from creation to completion/refund.

**Invariants**:
- Amount is immutable after creation (snapshot from tariff)
- Status transitions must follow valid state machine
- Gateway authority must be unique (idempotency)
- User ID cannot be changed after creation
- Service reference cannot be changed after creation

**Entities**:
```
PaymentTransaction (Aggregate Root)
├── id: UUID
├── userId: UUID
├── serviceType: PaidServiceType
├── serviceReferenceId: UUID
├── tariffId: UUID (foreign key for audit)
├── amount: Long (snapshot)
├── currency: String (snapshot)
├── gateway: PaymentGatewayType
├── gatewayAuthority: String (unique)
├── gatewayReferenceId: String (after success)
├── gatewayRequestId: String
├── status: PaymentStatus
├── createdAt: Instant
├── updatedAt: Instant
├── paidAt: Instant
├── refundedAt: Instant
├── failureReason: String
└── version: Long (optimistic locking)
```

**Value Objects**:
- `Money`: Encapsulates amount + currency with validation
- `TariffSnapshot`: Captures tariff state at payment creation time

### Entity (Not Aggregate): PaymentEventOutbox

**Purpose**: Supports transactional outbox pattern for reliable event delivery.

```
PaymentEventOutbox
├── id: UUID
├── eventType: PaymentEventType
├── aggregateId: UUID (payment_transaction.id)
├── aggregateType: String
├── payload: JsonB
├── occurredAt: Instant
├── processed: Boolean
├── processedAt: Instant
├── retryCount: Integer
├── lastError: String
└── version: Long
```

### Entity (Not Aggregate): PaymentIdempotencyKey

**Purpose**: Prevents duplicate payment creation under concurrent requests.

```
PaymentIdempotencyKey
├── idempotencyKey: String (PK)
├── userId: UUID
├── serviceType: PaidServiceType
├── serviceReferenceId: UUID
├── paymentTransactionId: UUID (FK)
├── createdAt: Instant
└── expiresAt: Instant
```

---

## Step 4: Payment State Machine

### Valid State Transitions

```
                    ┌─────────────┐
                    │   PENDING   │
                    └──────┬──────┘
                           │
          ┌────────────────┼────────────────┐
          │                │                │
          ▼                ▼                ▼
    ┌──────────┐    ┌──────────┐    ┌───────────┐
    │ SUCCESS  │    │  FAILED  │    │ CANCELLED │
    └────┬─────┘    └──────────┘    └───────────┘
         │
         ▼
   ┌──────────┐
   │ REFUNDED │
   └──────────┘
```

### Transition Rules

| From | To | Conditions |
|------|-----|------------|
| PENDING | SUCCESS | Gateway verification successful, gateway_reference_id present |
| PENDING | FAILED | Gateway verification failed OR timeout OR gateway error |
| PENDING | CANCELLED | User explicitly cancelled OR timeout exceeded |
| SUCCESS | REFUNDED | Admin initiates refund AND gateway supports refund |
| FAILED | PENDING | ❌ NOT ALLOWED - Failed payments cannot be retried by status change |
| SUCCESS | PENDING | ❌ NOT ALLOWED - Successful payments are final |
| SUCCESS | FAILED | ❌ NOT ALLOWED - Cannot undo success |
| ANY | PENDING | ❌ NOT ALLOWED - Cannot reset to pending |

### Enforcement Strategy

**Domain Service Method**:
```java
public void transitionTo(PaymentTransaction payment, PaymentStatus newStatus) {
    if (!isValidTransition(payment.getStatus(), newStatus)) {
        throw new InvalidPaymentStateException(...);
    }
    
    // Apply business rules for specific transitions
    if (newStatus == SUCCESS && payment.getStatus() == PENDING) {
        payment.verifySuccess(gatewayReferenceId, paidAt);
    } else if (newStatus == FAILED && payment.getStatus() == PENDING) {
        payment.markFailed(failureReason);
    }
    // ... other transitions
}
```

**Database Constraints**:
- Check constraints for basic validation (amount > 0, status NOT NULL)
- Unique constraint on gateway_authority prevents duplicate authorities
- Optimistic locking (version column) prevents concurrent modifications

---

## Step 5: Database Schema Design

### Key Design Decisions

#### 1. Monetary Amounts
- **Type**: `BIGINT` (8 bytes)
- **Unit**: Minor currency units (Rials for IRR)
- **Rationale**: 
  - IRR has no practical decimal subdivision
  - ZarinPal API uses integer amounts
  - Avoids BigDecimal serialization complexity
  - Efficient indexing and comparisons

#### 2. UUIDs for Primary Keys
- **Type**: `UUID` with `gen_random_uuid()` default
- **Rationale**:
  - Distributed ID generation without coordination
  - Security (non-sequential, non-guessable)
  - Easy merging of databases if needed
  - PostgreSQL has native UUID support

#### 3. Timestamps with Time Zone
- **Type**: `TIMESTAMP WITH TIME ZONE`
- **Default**: `CURRENT_TIMESTAMP`
- **Rationale**:
  - Iran Standard Time (IRST/IRDT) awareness
  - Audit trail accuracy
  - Daylight saving time handling

#### 4. Enum Types
- **PostgreSQL ENUM** for: `paid_service_type`, `payment_status`, `payment_gateway`, `payment_event_type`
- **Benefits**:
  - Database-level type safety
  - Efficient storage
  - Clear documentation of allowed values
- **Migration Strategy**: Adding new enum values requires ALTER TYPE

#### 5. JSONB for Event Payload
- **Type**: `JSONB` (binary JSON)
- **Rationale**:
  - Flexible schema for different event types
  - PostgreSQL can index JSON fields if needed
  - Efficient storage and querying
  - Type-safe deserialization in application

#### 6. Indexes Strategy

**Critical Indexes**:
- `idx_tariff_service_type_active`: Fast lookup of active tariff (unique)
- `idx_payment_user_id`: User transaction history
- `idx_payment_gateway_authority`: Idempotency check (unique)
- `idx_payment_status_created`: Admin filtering
- `idx_outbox_processed_occurred`: Efficient polling for unprocessed events

**Composite Indexes**:
- `(service_type, service_reference_id)`: Find payment for specific service instance
- `(user_id, created_at)`: User history sorted by date
- `(status, created_at)`: Admin dashboard filtering

#### 7. Foreign Keys
- `payment_transaction.tariff_id` → `tariff.id`: Audit trail
- `payment_idempotency_key.payment_transaction_id` → `payment_transaction.id`: Link to transaction

**Intentionally Missing**:
- No FK from `payment_transaction.service_reference_id` to service tables
- **Reason**: Payment module must remain decoupled from service modules

#### 8. Optimistic Locking
- **Column**: `version BIGINT DEFAULT 0`
- **Usage**: JPA `@Version` annotation
- **Prevents**: Lost updates from concurrent modifications
- **Critical For**: Payment status changes, outbox event processing

---

## Step 6: Liquibase Changelogs Summary

### Created Files

1. **`db.changelog-master.yaml`**
   - Master changelog including all payment changesets

2. **`001-create-tariff.yaml`**
   - Creates `paid_service_type` ENUM
   - Creates `tariff` table
   - Unique constraint: one active tariff per service type
   - Check constraint: amount > 0
   - Indexes for active tariff lookup

3. **`002-create-payment-transaction.yaml`**
   - Creates `payment_status` and `payment_gateway` ENUMs
   - Creates `payment_transaction` table
   - Unique constraint on `gateway_authority` (idempotency)
   - Foreign key to `tariff`
   - Comprehensive indexes for queries
   - Optimistic locking column

4. **`003-create-payment-idempotency.yaml`**
   - Creates `payment_idempotency_key` table
   - Primary key on `idempotency_key`
   - Foreign key to `payment_transaction`
   - Expiration mechanism for cleanup

5. **`004-create-payment-event-outbox.yaml`**
   - Creates `payment_event_type` ENUM
   - Creates `payment_event_outbox` table
   - JSONB payload storage
   - Unique constraint to prevent duplicate events
   - Indexes for efficient polling

6. **`005-add-payment-indexes-and-constraints.yaml`**
   - Additional composite indexes
   - Performance optimizations
   - Admin query support

### Migration Execution Order
```
001 → 002 → 003 → 004 → 005
```

### Rollback Support
Each changeset includes rollback instructions where practical:
- DROP TABLE for tables
- DROP TYPE for enums
- DROP INDEX for indexes
- Note: Some constraints may require manual intervention

### PreConditions
Each changeset includes preConditions to prevent re-running:
```yaml
preConditions:
  - onFail: MARK_RAN
    not:
      tableNameExists:
        tableName: tariff
```

---

## Next Steps (Awaiting Confirmation)

After confirming these first 6 steps, I will proceed with:

**Step 7**: JPA Entities and Repositories
- Domain entities with proper mappings
- Repository interfaces and implementations
- Entity-to-Domain mappers

**Step 8**: Application Services/Use Cases
- Tariff management use cases
- Payment creation with idempotency
- Payment verification flow
- Refund processing

**Step 9**: ZarinPal Gateway Implementation
- PaymentGatewayPort interface
- ZarinPalPaymentGateway implementation
- ZarinPal API client with WebClient
- Configuration properties

**Step 10**: REST APIs
- User payment endpoints
- Admin tariff and payment endpoints
- ZarinPal callback endpoint
- Request/Response DTOs

**Step 11**: Event Handling
- PaymentSucceededEvent definition
- Transactional outbox processor
- Spring event publisher integration
- Subscriber examples for service modules

**Step 12**: Security and Authorization
- Spring Security configuration
- User ownership validation
- Admin role-based access control
- Method-level security

**Step 13**: Tests
- Unit tests for domain logic
- Integration tests for payment flow
- ZarinPal gateway mock tests
- Security tests
- Idempotency tests
- Concurrent request tests

---

## Critical Implementation Notes

### ZarinPal Integration Specifics

**Official API Endpoints** (must verify current docs):
- Request: `https://api.zarinpal.com/pg/v4/payment/request.json`
- Verify: `https://api.zarinpal.com/pg/v4/payment/verify.json`
- Sandbox: `https://sandbox.zarinpal.com/pg/...`

**Required Configuration**:
```yaml
zarinpal:
  merchant-id: ${ZARINPAL_MERCHANT_ID}
  sandbox: true  # false for production
  callback-url: ${ZARINPAL_CALLBACK_URL}
  api-url: https://api.zarinpal.com/pg/v4
```

**Amount Handling**:
- ZarinPal expects amounts in **Rials** (not Tomans)
- 1 Toman = 10 Rials
- Common mistake: Confusing Rials and Tomans
- Our system stores in Rials consistently

**Callback Flow**:
1. User pays on ZarinPal
2. ZarinPal redirects browser: `GET /callback?Authority=XXX&Status=OK`
3. Backend extracts Authority
4. Backend calls ZarinPal Verify API (POST)
5. Backend updates payment status
6. Backend shows success/failure page to user

### Security Considerations

1. **Never log**: Merchant ID, gateway credentials, full request/response bodies with sensitive data
2. **Validate callback**: Always verify with ZarinPal server-side, never trust browser parameters alone
3. **HTTPS required**: All payment endpoints must use HTTPS in production
4. **CSRF protection**: Callback endpoint exempted (external callback), user endpoints protected

### Error Handling Strategy

**Domain Exceptions**:
- `TariffNotFoundException`
- `InactiveTariffException`
- `PaymentNotFoundException`
- `InvalidPaymentStateException`
- `PaymentVerificationException`
- `DuplicatePaymentException`
- `UnauthorizedPaymentAccessException`

**Global Exception Handler**:
- Consistent API error responses
- Hide internal details from clients
- Log full stack traces internally
- Return appropriate HTTP status codes

---

**Ready to proceed with Steps 7-13 upon your confirmation.**
