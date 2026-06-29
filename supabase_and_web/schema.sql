-- PHONE WORLD CRM - SUPABASE DATABASE SCHEMA
-- Purpose: Complete tables structure supporting Owner & Staff flows

-- 1. UTILS: Enable UUID extension
create extension if not exists "uuid-ossp";

-- 2. TABLE: users_profile (Linked block to auth.users)
create table if not exists public.users_profile (
    id uuid primary key references auth.users on delete cascade,
    email text not null,
    full_name text not null,
    role text not null check (role in ('Owner', 'Staff')),
    mobile_number text,
    created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 3. TABLE: staff_members
create table if not exists public.staff_members (
    id bigserial primary key,
    name text not null,
    mobile text not null,
    role text not null check (role in ('Owner', 'Staff')),
    is_active boolean default true not null,
    created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 4. TABLE: customers
create table if not exists public.customers (
    id bigserial primary key,
    customer_name text not null,
    mobile_number text not null,
    alternate_mobile_number text,
    address text,
    city_village text,
    product_purchased text,
    purchase_date date not null default current_date,
    total_bill_amount numeric(12,2) not null check (total_bill_amount >= 0),
    pending_amount numeric(12,2) not null check (pending_amount >= 0),
    notes text,
    status text not null check (status in ('Active', 'Paid', 'Pending', 'Overdue', 'Critical')),
    invoice_number text default '',
    model_detail text default '',
    created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 5. TABLE: dues (Installments tracker)
create table if not exists public.dues (
    id bigserial primary key,
    customer_id bigint not null references public.customers(id) on delete cascade,
    customer_name text not null, -- denormalized caching
    due_amount numeric(12,2) not null check (due_amount >= 0),
    due_date date not null,
    reminder_date date,
    due_status text not null check (due_status in ('Pending', 'Partial Paid', 'Paid', 'Overdue', 'Critical')),
    notes text,
    invoice_number text default '',
    purchase_date date,
    created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 6. TABLE: payment_entries
create table if not exists public.payment_entries (
    id bigserial primary key,
    customer_id bigint not null references public.customers(id) on delete cascade,
    customer_name text not null,
    due_id bigint references public.dues(id) on delete set null,
    amount_paid numeric(12,2) not null check (amount_paid > 0),
    payment_date date not null default current_date,
    payment_mode text not null check (payment_mode in ('Cash', 'UPI', 'Card', 'Finance', 'Other')),
    notes text,
    collected_by text not null, -- Name Reference of Staff/Owner
    purchase_date date,
    created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 7. TABLE: payment_followups
create table if not exists public.payment_followups (
    id bigserial primary key,
    customer_id bigint not null references public.customers(id) on delete cascade,
    customer_name text not null,
    follow_up_date date not null default current_date,
    notes text not null,
    next_follow_up_date date,
    promise_to_pay_date date,
    staff_name text not null,
    status text not null check (status in ('Pending', 'Completed', 'No Response', 'Promised', 'Paid')),
    purchase_date date,
    created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 8. TABLE: referral_persons
create table if not exists public.referral_persons (
    id bigserial primary key,
    full_name text not null,
    mobile_number text not null,
    address text,
    city text,
    notes text,
    created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 9. TABLE: customer_referrals (Linking referrals to customer registrations)
create table if not exists public.customer_referrals (
    id bigserial primary key,
    customer_id bigint not null references public.customers(id) on delete cascade,
    customer_name text not null,
    referred_by_type text not null check (referred_by_type in ('Owner', 'Staff', 'Existing Customer', 'External Person')),
    referrer_name text not null, -- Free text corresponding to source name
    status text not null default 'Pending' check (status in ('Pending', 'Collected')),
    created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 10. TABLE: whatsapp_reminder_logs
create table if not exists public.whatsapp_reminder_logs (
    id bigserial primary key,
    sent_date timestamp with time zone default timezone('utc'::text, now()) not null,
    sent_by text not null,
    customer_id bigint references public.customers(id) on delete set null,
    customer_name text not null,
    message text not null
);

-- 11. TABLE: message_templates
create table if not exists public.message_templates (
    id bigserial primary key,
    name text not null,
    language text not null check (language in ('English', 'Gujarati')),
    template_text text not null,
    created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- 12. TABLE: activity_logs (Audit Ledger)
create table if not exists public.activity_logs (
    id bigserial primary key,
    timestamp timestamp with time zone default timezone('utc'::text, now()) not null,
    staff_name text not null,
    action_type text not null,
    description text not null
);

-- INDEXING FOR READ SPEED
create index if not exists idx_customers_mobile on public.customers(mobile_number);
create index if not exists idx_dues_date on public.dues(due_date);
create index if not exists idx_dues_status on public.dues(due_status);
create index if not exists idx_payments_date on public.payment_entries(payment_date);
create index if not exists idx_followups_next_date on public.payment_followups(next_follow_up_date);
create index if not exists idx_customer_ref_cust on public.customer_referrals(customer_id);

-- SYSTEM PROCEDURE: Update Customer Pending Status automations
create or replace function public.recalculate_customer_pending()
returns trigger as $$
declare
    total_pending numeric(12,2);
    has_critical boolean;
    has_overdue boolean;
    has_pending boolean;
    new_status text;
begin
    -- 1. Sum up all unpaid dues for the customer
    select coalesce(sum(due_amount), 0.0) into total_pending
    from public.dues
    where customer_id = new.customer_id and due_status != 'Paid';

    -- 2. Detect due categories based on date passed
    select 
        exists(select 1 from public.dues where customer_id = new.customer_id and due_status != 'Paid' and (current_date - due_date) >= 60),
        exists(select 1 from public.dues where customer_id = new.customer_id and due_status != 'Paid' and (current_date - due_date) > 0 and (current_date - due_date) < 60),
        exists(select 1 from public.dues where customer_id = new.customer_id and due_status != 'Paid' and (current_date - due_date) <= 0)
    into has_critical, has_overdue, has_pending;

    -- 3. Map statuses
    if total_pending <= 0 then
        new_status := 'Paid';
    elsif has_critical then
        new_status := 'Critical';
    elsif has_overdue then
        new_status := 'Overdue';
    elsif has_pending then
        new_status := 'Pending';
    else
        new_status := 'Active';
    end if;

    -- 4. Apply back to customers
    update public.customers
    set pending_amount = total_pending,
        status = new_status
    where id = new.customer_id;

    return new;
end;
$$ language plpgsql;

drop trigger if exists trg_apply_dues_to_customer on public.dues;
create trigger trg_apply_dues_to_customer
after insert or update on public.dues
for each row execute function public.recalculate_customer_pending();
