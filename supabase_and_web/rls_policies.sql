-- PHONE WORLD CRM - ROW LEVEL SECURITY POLICIES (SUPABASE)
-- Purpose: Restribute write/delete rights selectively based on User Auth roles

-- Helper function: Check if current authenticated user is an Owner
create or replace function public.is_owner()
returns boolean as $$
begin
    return exists (
        select 1 from public.users_profile 
        where id = auth.uid() and role = 'Owner'
    );
end;
$$ language plpgsql security definer;

-- Helper function: Check if current authenticated user is a Staff member
create or replace function public.is_staff()
returns boolean as $$
begin
    return exists (
        select 1 from public.users_profile 
        where id = auth.uid() and role in ('Owner', 'Staff')
    );
end;
$$ language plpgsql security definer;


-- 1. POLICIES: users_profile
alter table public.users_profile enable row level security;

create policy "Users can read all profiles"
    on public.users_profile for select
    using ( public.is_staff() );

create policy "Owners can manage all profiles"
    on public.users_profile for all
    using ( public.is_owner() );


-- 2. POLICIES: staff_members
alter table public.staff_members enable row level security;

create policy "Staff can view standard roster"
    on public.staff_members for select
    using ( public.is_staff() );

create policy "Owners have total control on staff roster"
    on public.staff_members for all
    using ( public.is_owner() );


-- 3. POLICIES: customers
alter table public.customers enable row level security;

create policy "Staff can read customer catalog"
    on public.customers for select
    using ( public.is_staff() );

create policy "Staff can register new customers"
    on public.customers for insert
    with check ( public.is_staff() );

create policy "Staff can edit customer info"
    on public.customers for update
    using ( public.is_staff() )
    with check ( public.is_staff() );

create policy "Only Owners can delete customer entries"
    on public.customers for delete
    using ( public.is_owner() );


-- 4. POLICIES: dues
alter table public.dues enable row level security;

create policy "Staff can view dues"
    on public.dues for select
    using ( public.is_staff() );

create policy "Staff can schedule installments"
    on public.dues for insert
    with check ( public.is_staff() );

create policy "Staff can update installments"
    on public.dues for update
    using ( public.is_staff() )
    with check ( public.is_staff() );

create policy "Only Owners can remove installments"
    on public.dues for delete
    using ( public.is_owner() );


-- 5. POLICIES: payment_entries
alter table public.payment_entries enable row level security;

create policy "Staff can view collection book"
    on public.payment_entries for select
    using ( public.is_staff() );

create policy "Staff can record cash collections"
    on public.payment_entries for insert
    with check ( public.is_staff() );

create policy "Staff can adjust collected fees notes"
    on public.payment_entries for update
    using ( public.is_staff() )
    with check ( public.is_staff() );

create policy "Only Owners can delete receipt settlements"
    on public.payment_entries for delete
    using ( public.is_owner() );


-- 6. POLICIES: payment_followups
alter table public.payment_followups enable row level security;

create policy "Staff can view followups"
    on public.payment_followups for select
    using ( public.is_staff() );

create policy "Staff can schedule/record followups"
    on public.payment_followups for insert
    with check ( public.is_staff() );

create policy "Staff can edit followup notes"
    on public.payment_followups for update
    using ( public.is_staff() )
    with check ( public.is_staff() );

create policy "Only Owners can delete notes history"
    on public.payment_followups for delete
    using ( public.is_owner() );


-- 7. POLICIES: referral_persons & customer_referrals
alter table public.referral_persons enable row level security;
alter table public.customer_referrals enable row level security;

create policy "Staff can view referrers" on public.referral_persons for select using ( public.is_staff() );
create policy "Staff can record referrers" on public.referral_persons for insert with check ( public.is_staff() );
create policy "Staff can update referrers" on public.referral_persons for update using ( public.is_staff() ) with check ( public.is_staff() );
create policy "Only Owners can delete referrers" on public.referral_persons for delete using ( public.is_owner() );

create policy "Staff can view referrals state" on public.customer_referrals for select using ( public.is_staff() );
create policy "Staff can log referrals state" on public.customer_referrals for insert with check ( public.is_staff() );
create policy "Staff can update referrals state" on public.customer_referrals for update using ( public.is_staff() ) with check ( public.is_staff() );
create policy "Only Owners can delete referrals" on public.customer_referrals for delete using ( public.is_owner() );


-- 8. POLICIES: whatsapp_reminder_logs
alter table public.whatsapp_reminder_logs enable row level security;

create policy "Staff can read reminder audit logs"
    on public.whatsapp_reminder_logs for select
    using ( public.is_staff() );

create policy "Staff can append reminder audit logs"
    on public.whatsapp_reminder_logs for insert
    with check ( public.is_staff() );

create policy "Owners have full control on whatsapp reminder logs"
    on public.whatsapp_reminder_logs for all
    using ( public.is_owner() );


-- 9. POLICIES: activity_logs
alter table public.activity_logs enable row level security;

create policy "All staff can review audits"
    on public.activity_logs for select
    using ( public.is_staff() );

create policy "Appenders can insert log records"
    on public.activity_logs for insert
    with check ( true ); -- Allows automatic server side triggers or background logging

create policy "Owners have full control on activity logs"
    on public.activity_logs for all
    using ( public.is_owner() );


-- 10. POLICIES: message_templates
alter table public.message_templates enable row level security;

create policy "Staff can read message templates"
    on public.message_templates for select
    using ( public.is_staff() );

create policy "Owners have full control on message templates"
    on public.message_templates for all
    using ( public.is_owner() );

