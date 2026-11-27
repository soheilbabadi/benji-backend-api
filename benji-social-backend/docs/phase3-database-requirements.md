# Phase 3 — Required Database and Liquibase Changes

This report is documentation only. No migration file or SQL was created.

## Lost/Found lifecycle columns

For `lost_found_advertisement`:

- `lost_lifecycle_status` — enum-compatible VARCHAR(20), nullable
- `found_lifecycle_status` — enum-compatible VARCHAR(20), nullable
- `approved_at` — UTC `TIMESTAMP WITH TIME ZONE`, nullable
- `payment_deadline_at` — UTC `TIMESTAMP WITH TIME ZONE`, nullable
- `rejected_at` — UTC `TIMESTAMP WITH TIME ZONE`, nullable
- `resubmitted_at` — UTC `TIMESTAMP WITH TIME ZONE`, nullable
- `deactivated_at` — UTC `TIMESTAMP WITH TIME ZONE`, nullable

The existing `status` column must be migrated to approved compatibility values:

- `UNDER_REVIEW`
- `REJECTED`
- `PAYMENT_PENDING`
- `PUBLISHED`
- `EXPIRED`
- `DEACTIVATED`

Remove or stop using legacy values `DRAFT`, `ACTIVE`, `RESOLVED`, `CANCELLED`, `CLOSED`, and `HIDDEN` after a migration/data-conversion decision.

Exact lost/found state values must be enforceable consistently with database constraints. Independent Lost and Found columns are recommended; do not use a shared lifecycle enum constraint.

Recommended indexes:

- `(lost_lifecycle_status, payment_deadline_at)`
- `(found_lifecycle_status, expires_at)`
- `(status, expires_at)`
- `(public_id)`

## Rejection history table

Table: `lost_found_advertisement_rejection_history`

Columns:

- `id` — UUID, primary key, not null
- `advertisement_id` — UUID, not null
- `advertisement_type` — VARCHAR(10), not null
- `administrator_user_id` — UUID, not null
- `rejection_reason` — VARCHAR(1000), not null
- `rejection_sequence` — INTEGER, not null
- `rejected_at` — UTC `TIMESTAMP WITH TIME ZONE`, not null

Unique constraint: `(advertisement_id, rejection_sequence)`.

Index: `(advertisement_id, rejection_sequence)`.

There is no foreign key to other modules. Deletion is application-managed as part of permanent advertisement deletion.

## Moderation history table

Table: `lost_found_advertisement_moderation_history`

Columns:

- `id` — UUID, primary key, not null
- `advertisement_id` — UUID, not null
- `advertisement_type` — VARCHAR(10), not null
- `administrator_user_id` — UUID, not null
- `operation_type` — VARCHAR(20), not null
- `reason` — VARCHAR(1000), nullable
- `operated_at` — UTC `TIMESTAMP WITH TIME ZONE`, not null

Index: `(advertisement_id, operated_at)`.

Allowed operation values:

- `APPROVED`
- `REJECTED`
- `DEACTIVATED`
- `DELETED`

There is no foreign key to other modules. Records are deleted with the advertisement, except retained Envers audit history.

## Payment status values

The Payment status enum now includes `VERIFYING` and `UNKNOWN` in code for deadline uncertainty. PostgreSQL enum/check constraints must be reviewed and migrated if these values are persisted.

No migration was created.

## Existing orphan cleanup requirements

Permanent deletion application logic removes:

- Advertisement row.
- Rejection history.
- Moderation history.
- Advertisement reports.
- Contact reveal logs.
- Advertisement image metadata.

MinIO objects are removed before database deletion. A cleanup/outbox table would be required for reliable database/MinIO compensation, but no such migration is implemented here.

## Envers revision table

Table configured in code: `benji_revinfo`

Columns:

- `revision_id` — integer or bigint primary key, generated
- `revision_timestamp` — UTC `TIMESTAMP WITH TIME ZONE`, not null
- `actor_user_id` — UUID, nullable
- `actor_type` — VARCHAR(20), not null
- `correlation_id` — VARCHAR(128), nullable

Indexes:

- `(revision_timestamp)`
- `(actor_user_id, revision_timestamp)`

## Envers audit tables

Envers will require `_AUD` tables for every JPA entity annotated `@Audited`, including:

- User and legacy identity entities
- OTP and refresh-token metadata
- Pet, breed, and Pet Media
- Lost/Found advertisement and image/report/reveal entities
- Rejection/moderation histories
- Payment transactions, tariffs, idempotency keys, and outbox entities
- Lookup entities
- Legacy address/role entities

Each audit table requires Envers standard columns:

- Entity primary-key column.
- `REV` foreign key to `benji_revinfo.revision_id`.
- `REVTYPE` small integer/string revision type.
- Original audited fields.

Do not cascade-delete audit tables when business rows are deleted.

Collection audit tables are required for audited JPA collections/relationships, especially LegacyUser roles and addresses.

Sensitive fields excluded in code:

- OTP challenge code.
- Refresh-token hash and replaced-hash values.
- LegacyUser OTP value.

Payment outbox payload is excluded from audit in code.

## Reliable permanent-deletion cleanup

Table: `lost_found_advertisement_deletion_cleanup`

Columns:

- `advertisement_public_id` — UUID, primary key, not null
- `owner_user_id` — UUID, not null
- `image_object_keys` — VARCHAR(8192) or text, nullable
- `status` — VARCHAR(20), not null: `PENDING`, `PROCESSING`, or `FAILED`
- `attempt_count` — integer, not null, default 0
- `last_error` — VARCHAR(1000), nullable
- `created_at` — UTC `TIMESTAMP WITH TIME ZONE`, not null
- `updated_at` — UTC `TIMESTAMP WITH TIME ZONE`, not null
- `version` — bigint, not null for optimistic locking

This table is an operational retry record. It contains no soft-deleted advertisement row and is deleted after all image-object removals succeed.

Indexes:

- `(status, created_at)`

The cleanup record is not audited. Repeated owner/admin deletion is idempotent. Database records are deleted in the same transaction as creation of the cleanup record. The background cleanup only deletes MinIO objects and then removes the cleanup record.

## Lost Payment success event deduplication

Table: `lost_found_payment_succeeded_processing`

Columns:

- `transaction_id` — UUID, primary key, not null
- `advertisement_public_id` — UUID, not null
- `processed_at` — UTC `TIMESTAMP WITH TIME ZONE`, nullable
- `created_at` — UTC `TIMESTAMP WITH TIME ZONE`, not null

Unique constraint: primary key on `transaction_id`.

Index: `(advertisement_public_id)`.

This table is a technical idempotency record. It is not a soft-deleted advertisement or audit table. Failed validation removes the reservation so the event can be retried.

## Envers deleted-advertisement lookup

No additional retention-index table is required.

The administrator audit endpoint queries Envers revisions using the original `LostFoundAdvertisementEntity.publicId` property. Envers retains deleted-row audit revisions and original entity primary-key history. Permanent deletion does not require or create a live advertisement lookup row.
