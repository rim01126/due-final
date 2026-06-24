# PHONE WORLD BUSINESS CRM - SETUP & DEPLOYMENT MANUAL

A production deployment guide for Phone World, covering the Android (Kotlin) app compile tasks, Supabase back-end provisioning, and external WhatsApp API routing.

---

## SECTION 1: DATABASE PROVISIONING (SUPABASE)

1. **Create Project**:
   - Go to [Supabase Console](https://supabase.com).
   - Click **New Project** and select your region.

2. **Execute Schema SQL**:
   - Navigate to the **SQL Editor** tab in Supabase.
   - Click **New Query** and copy the contents of your `/supabase_and_web/schema.sql` file.
   - Run the script. This will establish all 11 database structures, tables indexes, and cash audit sync triggers.

3. **Verify Row Level Security policies**:
   - Under SQL editor, open another query and run the `/supabase_and_web/rls_policies.sql` file.
   - This activates dynamic constraints prohibiting Staff members from issuing delete commands.

4. **Populate initial tables state**:
   - Running `/supabase_and_web/test_data.sql` populates the initial staff list, message templates, referrers, and overdue clients.

5. **Enable Auto-Escalation Cron Job (pg_cron)**:
   - To automatically transition aging dues past 60 days of due date to Critical status and log system alerts, run `/supabase_and_web/critical_overdue_job.sql` in the SQL Editor.
   - This installs the `pg_cron` extension, creates an escalation stored function, and registers a backend scheduler triggering every midnight.

---

## SECTION 2: ANDROID APPLICATION DEPLOYMENT (BUILDING APK)

### Option A: Local Customization in Android Studio

If you export this code to edit locally:
1. Unzip the downloaded project.
2. Open Android Studio, select **Open An Existing Project**, and specify the root directory.
3. Wait for the Gradle compilation task to complete.
4. Open the **Secrets panel in AI Studio** or create a `.env` file in the root directory (to match `.env.example` configurations):
   ```env
   SUPABASE_URL=https://your-project-id.supabase.co
   SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```
5. Click **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
6. The resulting file is generated at:
   `/app/build/outputs/apk/debug/app-debug.apk`

### Option B: Deploying to Google Play (AAB Release Build)

To prepare a production release AAB container:
1. In Android Studio, go to **Build > Generate Signed Bundle / APK**.
2. Select **Android App Bundle (AAB)** and click Next.
3. Create or select an existing `.jks` keystore container.
4. Input store passwords and alias mappings (as configured inside the `signingConfigs` block's release variables).
5. Specify **release** as the Build Type, select **V2 (Full Signature)**, and click Finish.
6. The AAB container can now be uploaded directly to the Google Play Console!

---

## SECTION 3: WHATSAPP INTEGRATION WIRING

### Method 1: Direct Universal WhatsApp Intents (Default/Zero-Cost)

The Android application is pre-loaded with programmatic deep linking templates:
- **English/Gujarati dispatch**: It loads the templates dynamically, replaces template variables with active client data, and triggers a system intent directly to WhatsApp:
  ```kotlin
  val intent = Intent(Intent.ACTION_VIEW).apply {
      data = Uri.parse("https://api.whatsapp.com/send?phone=$mobileNumber&text=${Uri.encode(messageText)}")
  }
  ```
- **Benefits**: Completely free, requires zero server hosting, uses the active logged-in device's WhatsApp Business, and logs activity states instantly in `whatsapp_reminder_logs` database tables.

### Method 2: Enterprise Twilio WhatsApp SMS Route (Optional)

To trigger reminders silently from the web dashboard or cloud functions:
1. Register for an [Enterprise Twilio Sandbox for WhatsApp](https://www.twilio.com/whatsapp).
2. Grab your Twilio Account SID and Auth Token.
3. Configure a Supabase Edge function (`post_message_handler`) making a REST call to Twilio's HTTP API.
4. Point the client call directly to your newly compiled Edge endpoint!

---

## SECTION 4: WEB ADHERENCE PANEL DEPLOYMENT (REACT HOSTING)

1. **Verify stack dependencies**:
   - Double check that your web project has `lucide-react`, `tailwindcss` and `vite` configured.
2. **Move Source**:
   - Drop the `/supabase_and_web/web_admin_panel.jsx` structure inside your React `src/components/` folder.
3. **Build Target**:
   - Compile production static files:
     ```bash
     npm run build
     ```
4. **Deploy static builds**:
   - Upload the static distribution folder (`dist/`) directly to static hosting platforms such as **Vercel**, **Netlify**, or **Supabase Hosting**!

---

## SECTION 5: TROUBLESHOOTING SYNC PERMISSION ERRORS (RLS / 42501)

If you see errors like:
* `"new row violates row-level security policy..."` (Code `42501`)
* Sync fails during data push/pull

This means the database has **Row-Level Security (RLS)** active, but your mobile application is running with unauthenticated client access (using the public anon key).

### To Fix This Instantly (Recommended for Development/Testing):
Go to your **Supabase Dashboard** -> **SQL Editor** -> click **New Query**, paste the following script, and click **Run**:

```sql
-- Disable Row Level Security to allow unauthenticated app synchronization
alter table public.users_profile disable row level security;
alter table public.staff_members disable row level security;
alter table public.customers disable row level security;
alter table public.dues disable row level security;
alter table public.payment_entries disable row level security;
alter table public.payment_followups disable row level security;
alter table public.referral_persons disable row level security;
alter table public.customer_referrals disable row level security;
alter table public.whatsapp_reminder_logs disable row level security;
alter table public.activity_logs disable row level security;
alter table public.message_templates disable row level security;
```

This instantly opens up the tables for client-side direct sync, resolving all database access failures.

---

## SECTION 6: TROUBLESHOOTING SEQUENCE DESYNCHRONIZATION (DUPLICATE KEY ERRORS)

If you see errors like:
* `"duplicate key value violates unique constraint..."` (e.g. `Key (id)=(1) already exists`)
* Database inserts fail during manual creation of customers, installments, payments, or logs

This occurs because manual insertion of mock data with explicit IDs (like in `test_data.sql`) does not advance PostgreSQL's internal auto-increment sequence generators. When a new record is created without specifying an ID, the sequence returns `1` or another low number that has already been taken.

### To Fix This Instantly:
Go to your **Supabase Dashboard** -> **SQL Editor** -> click **New Query**, paste the following script, and click **Run**:

```sql
-- Reset sequences to align with the maximum existing ID for each table
select setval('public.customers_id_seq', coalesce((select max(id) from public.customers), 1));
select setval('public.dues_id_seq', coalesce((select max(id) from public.dues), 1));
select setval('public.payment_entries_id_seq', coalesce((select max(id) from public.payment_entries), 1));
select setval('public.payment_followups_id_seq', coalesce((select max(id) from public.payment_followups), 1));
select setval('public.referral_persons_id_seq', coalesce((select max(id) from public.referral_persons), 1));
select setval('public.customer_referrals_id_seq', coalesce((select max(id) from public.customer_referrals), 1));
select setval('public.staff_members_id_seq', coalesce((select max(id) from public.staff_members), 1));
select setval('public.whatsapp_reminder_logs_id_seq', coalesce((select max(id) from public.whatsapp_reminder_logs), 1));
select setval('public.message_templates_id_seq', coalesce((select max(id) from public.message_templates), 1));
select setval('public.activity_logs_id_seq', coalesce((select max(id) from public.activity_logs), 1));
```

