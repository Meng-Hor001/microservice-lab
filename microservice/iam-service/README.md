# IAM service — Parts 1 and 2

The domain module contains plain Java models and rules. The application module
contains use cases and output ports; persistence implements those ports with
Spring Data JPA. TransactionRunner lets multi-save use cases share a database
transaction without importing Spring into the application module.

## Assignment lifecycle

There can be one unrevoked assignment per user/role, role/permission, or
user/permission pair. Repeated assignment requests raise
DuplicateAssignmentException. An expired assignment must also be explicitly
revoked before replacement. Changing a direct permission from GRANT to DENY
requires revoking the current assignment first. Revocation preserves the row,
actor, timestamp, and reason. Liquibase migration 007 enforces this rule for
concurrent writers as well as application requests.

## Verification

Run from the repository root with Docker running:

```sh
./gradlew \
  :microservice:iam-service:iam-service-domain:iam-domain-core:test \
  :microservice:iam-service:iam-service-main:test
```

PartTwoIntegrationTest starts an isolated PostgreSQL 17 container and applies all
Liquibase migrations to its empty database. It checks seeds, assignment history,
duplicates, permission expiry and DENY precedence, transaction rollback,
optimistic locking, and identity linking/unlinking. It does not use the local
application database. The tests deliberately have no outer test transaction, so
adapter transaction mistakes and failed rollbacks remain visible.

To start the application from the repository root:

```sh
./gradlew :microservice:iam-service:iam-service-main:bootRun
```
