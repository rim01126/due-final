-- PHONE WORLD CRM - AUTOMATIC OVERDUE CRITICAL ESCALATION JOB
-- Purpose: Automatically transitions unpaid dues 60+ days past due date to "Critical" status, logs system activity alerts, and integrates with pg_cron.

-- 1. Enable pg_cron Extension (must be run as superuser/postgres in Supabase)
create extension if not exists pg_cron;

-- 2. Create the Escalation Function
create or replace function public.transition_critical_overdue_dues()
returns void as $$
declare
    record_count integer := 0;
    due_rec record;
begin
    -- Iterate through dues that are active/unpaid, not yet marked as 'Critical', and overdue by 60+ days
    for due_rec in 
        select d.id, d.customer_id, d.customer_name, d.due_amount, d.due_date 
        from public.dues d
        where d.due_status not in ('Paid', 'Critical')
          and d.due_date <= (current_date - interval '60 days')
    loop
        -- Update the due status to critical
        update public.dues
        set due_status = 'Critical'
        where id = due_rec.id;

        -- Log system alert in activity_logs for audit trailing
        insert into public.activity_logs (staff_name, action_type, description)
        values (
            'System Cron', 
            'Critical Alert', 
            'Installment ID ' || due_rec.id || ' for customer ' || due_rec.customer_name || ' (ID ' || due_rec.customer_id || ') is ' || (current_date - due_rec.due_date) || ' days overdue. Escalated to Critical.'
        );

        record_count := record_count + 1;
    end loop;

    -- Log summary execution log if any transitions were processed
    if record_count > 0 then
        insert into public.activity_logs (staff_name, action_type, description)
        values (
            'System Cron', 
            'System Run', 
            'Overdue Escalation Job executed. ' || record_count || ' installment records transitioned to Critical status.'
        );
    end if;
end;
$$ language plpgsql security definer;

-- 3. Schedule the Cron Job via pg_cron
-- This schedules the transition function to run once daily at midnight (00:00) UTC
select cron.schedule(
    'transition-critical-overdue-dues-job', -- Unique job identifier
    '0 0 * * *',                             -- Standard Cron format: min hours day_of_month month day_of_week
    $$select public.transition_critical_overdue_dues()$$
);
