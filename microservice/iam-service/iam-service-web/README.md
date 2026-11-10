# IAM web pages

Restart the IAM application, then visit http://localhost:9999/login.
The web module uses JTE 3.2.5 and packages templates as resources. Runtime
compilation requires a JDK. Replace this with build-time precompilation before
producing a JRE-only deployment.

Routes: /login, /register, /, /profile/edit, /profile/password,
/profile/accounts, /profile/apps, and /oauth2/consent.
Switch English/Khmer with ?lang=en or ?lang=km. Sessions remember the language.
All state-changing forms include CSRF fields; validation failures retain
non-password fields and display a translated message.

To link Telegram or Facebook when the provider has no email, sign in to an
existing IAM account first, open /profile/accounts, and choose Link account.
The explicit intent expires after five minutes. Use the same origin throughout
this flow; Telegram configured with ngrok must start on the ngrok origin.
The provider callback is verified by Spring before the identity is linked.
New users without an email still require a separate verified-email onboarding
flow; this page supports linking to an existing account.

Connected apps are listed from stored consent. Revoking removes consent and
server-side authorization/refresh-token state. Previously issued self-contained
JWTs remain valid until expiration unless a resource server adds revocation checks.

The consent page is used for authorization-code requests that require consent.
Set requireAuthorizationConsent(true) on test clients when testing it.

Verification:
./gradlew :microservice:iam-service:iam-service-web:test
./gradlew :microservice:iam-service:iam-service-main:compileJava

Live OAuth provider callbacks require your own configured credentials and browser
consent. Template tests compile all views and render English/Khmer pages, including
escaping and CSRF checks. Browser-test local sign-in, edit/password forms,
provider linking, consent Allow/Cancel, and application revocation after restart.
