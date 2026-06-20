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
