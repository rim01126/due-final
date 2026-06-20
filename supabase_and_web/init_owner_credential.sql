-- PHONE WORLD BUSINESS CRM - OWNER CREDENTIALS INITIALIZATION SCRIPT
-- Purpose: Safely populate Supabase Auth (auth.users) and public.users_profile with Owner credentials
-- Full Name: Rais Memon
-- Mobile ID: 9724493045
-- Login Credentials: [Mobile / Email] + Password '123456789'

do $$
declare
    new_user_id uuid := gen_random_uuid();
    encrypted_pw text;
    owner_email text := 'rim01119@gmail.com'; -- Associated user email from setup context
    owner_mobile text := '9724493045';
    owner_name text := 'Rais Memon';
begin
    -- 1. Activate the pgcrypto extension inside the extensions schema to secure bcrypt hashing
    create extension if not exists pgcrypto with schema extensions;

    -- 2. Encrypt the password strings '123456789' using blowfish (bf) bcrypt algorithm (rounds = 10)
    encrypted_pw := extensions.crypt('123456789', extensions.gen_salt('bf', 10));

    -- 3. Avoid double entry violations by checking if a user with this mobile/email already exists
    if not exists (select 1 from auth.users where email = owner_email or phone = owner_mobile) then
        
        -- A. Setup credentials under auth.users (Supabase Identity)
        insert into auth.users (
            instance_id,
            id,
            aud,
            role,
            email,
            encrypted_password,
            email_confirmed_at,
            raw_app_meta_data,
            raw_user_meta_data,
            created_at,
            updated_at,
            phone,
            phone_confirmed_at,
            is_sso_user
        ) values (
            '00000000-0000-0000-0000-000000000000',
            new_user_id,
            'authenticated',
            'authenticated',
            owner_email,
            encrypted_pw,
            now(),
            '{"provider": "email", "providers": ["email"]}'::jsonb,
            jsonb_build_object('full_name', owner_name, 'role', 'Owner'),
            now(),
            now(),
            owner_mobile,
            now(),
            false
        );

        -- B. Setup credentials under public.users_profile (Internal App Link)
        insert into public.users_profile (
            id,
            email,
            full_name,
            role,
            mobile_number,
            created_at
        ) values (
            new_user_id,
            owner_email,
            owner_name,
            'Owner',
            owner_mobile,
            now()
        );

        -- C. Ensure match in local public.staff_members for collection logging streams
        if not exists (select 1 from public.staff_members where mobile = owner_mobile) then
            insert into public.staff_members (name, mobile, role, is_active)
            values (owner_name || ' (Owner)', owner_mobile, 'Owner', true);
        end if;

        raise notice 'SUCCESS: Owner account with Rais Memon (9724493045) successfully spawned.';
    else
        -- Update the profile and staff if the user somehow already existed, keeping system unified
        raise notice 'WARNING: User with profile credentials already exist. Execution skipped to prevent data duplication.';
    end if;
end;
$$;
