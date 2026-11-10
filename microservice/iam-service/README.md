# IAM Service

Identity and access management service for the platform. It manages users, roles,
permissions, local and federated identities, and OAuth2/OpenID Connect authorization.
The application runs at **http://localhost:9999** by default.

## Features

- User registration, profile updates, password changes, account locking and enabling.
- Role and permission management, including role assignments and direct user permissions.
- OAuth2 authorization server with JDBC-backed clients, authorizations, and consent.
- JWT-protected REST APIs with permission-based authorization.
- Social sign-in through Google, GitHub, Facebook, and Telegram.
- Account linking, consent management, and connected application revocation.
- Server-rendered JTE pages with English and Khmer translations.
- PostgreSQL persistence and Liquibase database migrations.

## Modules

| Module | Responsibility |
| --- | --- |
| `iam-service-domain/iam-domain-core` | Domain entities, rules, and authority calculation |
| `iam-service-domain/iam-application-service` | Use cases and ports |
| `iam-service-persistence` | JPA entities, repositories, adapters, and migrations |
| `iam-service-infrastructure` | Authentication, federated identity, and authorization server infrastructure |
| `iam-service-restapi` | Controllers, DTOs, API security, and error handling |
| `iam-service-web` | JTE templates, styles, translations, and web pages |
| `iam-service-main` | Spring Boot entry point and application configuration |

The build uses Java 25, Spring Boot, Spring Security, Spring Data JPA, and Gradle.
Shared build conventions and the version catalog live at the repository root.

## Requirements

- JDK 25. A full JDK is also needed for runtime JTE template compilation.
- PostgreSQL with an existing database and a user allowed to run schema migrations.
- Docker for the Testcontainers integration tests.
- OAuth provider credentials to exercise social login.

Run the following commands from the **repository root**. IAM participates in the
repository's composite Gradle build; it has no separate Gradle wrapper.

## Local setup

### 1. Start PostgreSQL

For an isolated local database, run:

```bash
docker run --name iam-postgres \
  -e POSTGRES_DB=iam_db \
  -e POSTGRES_USER=iam \
  -e POSTGRES_PASSWORD=local-iam-password \
  -p 5432:5432 \
  -d postgres:17
```

This example starts a container without a persistent volume. Use an existing
PostgreSQL instance if preferred, and adjust the connection variables below.

### 2. Configure the environment

```bash
export DB_URL='jdbc:postgresql://localhost:5432/iam_db'
export DB_USERNAME='iam'
export DB_PASSWORD='local-iam-password'
export IAM_REGISTRATION_CLIENT_ID='local-registrar'
export IAM_REGISTRATION_CLIENT_SECRET='replace-with-a-long-random-secret'

export GOOGLE_CLIENT_ID='replace-with-google-client-id'
export GOOGLE_CLIENT_SECRET='replace-with-google-client-secret'
export GITHUB_CLIENT_ID='replace-with-github-client-id'
export GITHUB_CLIENT_SECRET='replace-with-github-client-secret'
export FACEBOOK_CLIENT_ID='replace-with-facebook-client-id'
export FACEBOOK_CLIENT_SECRET='replace-with-facebook-client-secret'
export TELEGRAM_CLIENT_ID='replace-with-telegram-client-id'
export TELEGRAM_CLIENT_SECRET='replace-with-telegram-client-secret'
```

All variables above are referenced without defaults in
[`application.yaml`](iam-service-main/src/main/resources/application.yaml).
For local password-only development, nonempty placeholder provider values can be
used; social sign-in requires real credentials. A `.env` file is not automatically
loaded by these commands: export the values or configure them in your IDE.

The registration client is created on startup with the `client_credentials` grant
and `client.create` scope. If its client ID already exists in the database, startup
leaves that record unchanged; changing the environment secret does not rotate it.

### 3. Run the service

```bash
./gradlew :microservice:iam-service:iam-service-main:bootRun
```

Liquibase applies the changelog from `iam-service-persistence` on startup, then
Hibernate validates the schema. The configured active profile is `local`.

Open [the registration page](http://localhost:9999/register) to create an account,
then [sign in](http://localhost:9999/login). Administrative API operations require
an account with the relevant permissions; self-registration is not an admin setup.

## Web pages

| Path | Purpose |
| --- | --- |
| `/login` | Local and social sign-in |
| `/register` | Account registration |
| `/` | Signed-in home page |
| `/profile/edit` | Edit profile |
| `/profile/password` | Change password |
| `/profile/accounts` | Manage linked identities |
| `/profile/apps` | View and revoke connected applications |
| `/oauth2/consent` | Authorization consent |

Use `?lang=en` or `?lang=km` to switch languages. Web forms use session
authentication and include CSRF protection. See the
[web module README](iam-service-web/README.md) for account-linking details.

## REST API

| Base path | Operations |
| --- | --- |
| `/api/auth/register` | Public account registration (`POST`) |
| `/api/me` | Current profile (`GET`, `PATCH`), password (`PUT /password`), identities (`GET /identities`, `DELETE /identities/{identityUuid}`) |
| `/api/users` | Create/list users; read, update, delete, enable, disable, lock, unlock, and reset passwords; manage roles, permissions, and identities |
| `/api/roles` | Create/list/read/update/delete roles, activate/deactivate, and assign/revoke permissions |
| `/api/permissions` | Create/list/read/update/delete permissions and activate/deactivate |

Except for registration, `/api/**` requires a bearer JWT. The API is stateless;
a browser login session does not authenticate API requests. Tokens must have the
configured issuer (`http://localhost:9999`), audience (`iam-api`), and a UUID user
subject. The `permissions` claim supplies authorities such as `user:read`; the
`roles` claim supplies authorities prefixed with `ROLE_`. Controller methods enforce
the required permissions.

Register a local account:

```bash
curl -X POST http://localhost:9999/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "demo",
    "email": "demo@example.com",
    "password": "DemoPassword123!",
    "confirmPassword": "DemoPassword123!",
    "givenName": "Demo",
    "familyName": "User",
    "locale": "en"
  }'
```

Call the profile API with a user access token obtained through an OAuth2
authorization-code flow:

```bash
curl http://localhost:9999/api/me \
  -H "Authorization: Bearer ${ACCESS_TOKEN}"
```

OAuth clients must be registered with appropriate redirect URIs and grants before
starting the authorization flow. The bootstrap registrar's client-credentials token
is for client registration, and does not provide a UUID user subject for `/api/me`.

## OAuth2 and social login

The authorization server exposes discovery at
`/.well-known/openid-configuration`, authorization at `/oauth2/authorize`, token
issuance at `/oauth2/token`, and public keys at `/oauth2/jwks`.

Provider login starts at `/oauth2/authorization/{registrationId}`, where the
registration ID is `google`, `github`, `facebook`, or `telegram`. Configure the
provider console with the matching callback URI. Google, GitHub, and Facebook use
`http://localhost:9999/login/oauth2/code/{registrationId}` for local development.
Telegram currently has a fixed ngrok callback in `application.yaml`; override
`SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_TELEGRAM_REDIRECT_URI` with your own
registered public callback. Start account-linking flows on the same origin used
for the callback.

The issuer is hardcoded in `AuthorizationServerKeyConfig`. Changing the deployment
origin requires aligning that issuer with the resource-server issuer and JWK URL.
RSA signing keys are generated in memory at startup, so restarting changes the
published key. Durable deployments need persistent signing keys and a rotation
strategy. Revoking a connected app removes stored consent and server-side token
state; already issued JWTs can remain valid until expiry.

## Build and test

Build the runnable application and its dependencies:

```bash
./gradlew :microservice:iam-service:iam-service-main:bootJar
```

The executable JAR is written under `iam-service-main/build/libs/` inside this
service directory. It requires the same environment configuration as `bootRun`.

Run the IAM module tests:

```bash
./gradlew \
  :microservice:iam-service:iam-service-domain:iam-domain-core:test \
  :microservice:iam-service:iam-service-domain:iam-application-service:test \
  :microservice:iam-service:iam-service-infrastructure:test \
  :microservice:iam-service:iam-service-restapi:test \
  :microservice:iam-service:iam-service-web:test \
  :microservice:iam-service:iam-service-main:test
```

Main-module integration tests use Testcontainers with PostgreSQL 17 and require a
running Docker daemon. They supply test-only provider and registration-client
settings. Live provider callbacks still need manual verification with real credentials.

## Troubleshooting

- **Unresolved configuration placeholder:** export every required environment variable.
- **Database connection or migration failure:** check the JDBC URL, database existence,
  credentials, and schema permissions. Keep Liquibase enabled with `ddl-auto: validate`.
- **API returns 401:** check token expiry, signature, issuer, audience, and UUID subject.
- **API returns 403:** check the account's effective permissions and the token claims.
- **Provider callback fails:** verify credentials, registered callback URI, and browser origin.
- **Templates fail under a JRE:** run with a full JDK; templates currently compile at runtime.
