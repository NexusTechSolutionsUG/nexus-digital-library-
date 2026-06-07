# Nexus Digital Library

A role-based educational workspace and digital portal for high school students, teachers, librarians, and administrators in secondary education systems.

## Security Controls and Production Best Practices

To transition this high-fidelity workspace from an evaluation demo to a production-ready environment, the following security gates and policies are enforced:

### 1. Hardened Sandbox Authentication
- Sandbox logins and account signups only operate when compiling a **Debug build** (`BuildConfig.DEBUG = true`) AND having **`DEMO_AUTH_ENABLED`** set explicitly to `true` in your `.env` settings.
- In **Release mode**, the system fails closed safely. If credentials or Supabase integration keys are blank, missing, or mismatched, logins and sign-ups immediately fail with clear visual feedback, preventing fallback backdoors.

### 2. Privileged Staff Authorization
- Client-side registration access codes (like `NEXUSTECH2026`) are strictly designated for **Debug/Demo** evaluation gating.
- **In Production Mode:** Client-side staff registration automatically defaults any profile creation to standard, unprivileged **`STUDENT`** status.
- **Production Staff Role Assignment** must be authorized server-side. This must happen using designated:
  - Supabase Row-Level Security (RLS) policies
  - Admin Invitation Flows
  - Secure remote Edge Functions / Postgres triggers that corroborate staff emails against official employee directories.

### 3. Encrypted Credentials Store
- Plaintext student/staff persistences in standard SharedPreferences are replaced by a secure **`EncryptedAuthSessionStore`** utilizing standard **AndroidX Security Crypto** (`EncryptedSharedPreferences`).
- Persistent sessions explicitly validate token lifecycle and duration expirations (`expires_in`) before allowing sign-in restoration. Expiration or parse failures instantly wipe local session storage.

### 4. Non-Leaking AI Integrations
- Gemini API key is configured and fetched from secure environment property mappings, and passed securely within the OkHttp request headers (`x-goog-api-key`) rather than query URLs.
- Sensitive prompt payloads, attachment contents, request/response bodies, or API Keys are structural secrets and are **never** logged to logcat.
- If no Gemini API Key is found in Release Mode, the service returns a graceful closed-fail error instead of exposing a simulated local playground reply.

### 5. Deny-by-Default Device Backup Policy
- Android system backups are disabled by default (`android:allowBackup="false"`).
- Both `backup_rules.xml` and `data_extraction_rules.xml` contain robust global exclude overrides that safeguard secure preferences, cached files, downloads, and primary SQL databases.

### 6. Room Database Structural Integrity
- Room database builders ignore structural fallback-to-destructive schemas in production.
- Database schemas are preserved and exported as versioned structural artifacts using KSP Gradle arguments, allowing robust, incremental migration paths.
