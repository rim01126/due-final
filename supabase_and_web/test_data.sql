-- PHONE WORLD CRM - TEST DATA SEEDING SCRIPTS
-- Purpose: Preload database with representative test accounts

-- 1. Insert message templates
insert into public.message_templates (name, language, template_text) values
('standard_rem_en', 'English', 'Hello {customer_name}, This is a reminder from Phone World. Your pending amount is ₹{due_amount}. Please make payment by {due_date}. Thank you!'),
('standard_rem_gu', 'Gujarati', 'નમસ્તે {customer_name}, Phone World તરફથી યાદ અપાવવામાં આવે છે કે તમારી બાકી રકમ ₹{due_amount} છે. કૃપા કરીને {due_date} સુધી ચુકવણી કરો. આભાર.');

-- 2. Insert test staff members
insert into public.staff_members (name, mobile, role, is_active) values
('Rais Memon', '9724493045', 'Owner', true),
('Keval Patel', '+91 99887 76655', 'Staff', true),
('Anjali Mehta', '+91 91234 56789', 'Staff', true);

-- 3. Insert referral persons
insert into public.referral_persons (full_name, mobile_number, address, city, notes) values
('Harshil Shah', '+91 88888 77777', 'Kalawad Road', 'Rajkot', 'Supplier referral'),
('Mayur Bhai', '+91 77777 66666', 'Gondal Road', 'Rajkot', 'Loyal friend');

-- 4. Insert test customers
insert into public.customers (id, customer_name, mobile_number, alternate_mobile_number, address, city_village, product_purchased, total_bill_amount, pending_amount, notes, status) values
(1, 'Vijay Vasoya', '9909912345', '9909954321', 'Shakti Nagar', 'Rajkot', 'iPhone 15 Pro Max 256GB', 145000.00, 45000.00, 'Referred by Keval', 'Active'),
(2, 'Suresh Rathod', '9825611223', null, 'Yagnik Road', 'Rajkot', 'Samsung S24 Ultra', 120000.00, 30000.00, 'Needs regular followup', 'Overdue'),
(3, 'Dharmesh Gohil', '9426288990', null, 'Station Road', 'Gondal', 'OnePlus 12 512GB', 65000.00, 25000.00, 'Followup failed. Transition to critical.', 'Critical');

-- 5. Insert test installments
insert into public.dues (id, customer_id, customer_name, due_amount, due_date, reminder_date, due_status, notes) values
(101, 1, 'Vijay Vasoya', 45000.00, current_date + interval '10 days', current_date + interval '8 days', 'Pending', 'Future installment'),
(102, 2, 'Suresh Rathod', 30000.00, current_date - interval '15 days', current_date - interval '17 days', 'Overdue', 'Second installment overdue'),
(103, 3, 'Dharmesh Gohil', 25000.00, current_date - interval '65 days', current_date - interval '67 days', 'Critical', 'Highly critical over 60 days');

-- 6. Insert test referrals associations
insert into public.customer_referrals (customer_id, customer_name, referred_by_type, referrer_name, status) values
(1, 'Vijay Vasoya', 'Staff', 'Keval Patel', 'Pending'),
(2, 'Suresh Rathod', 'External Person', 'Harshil Shah', 'Pending');

-- 7. Insert activity log
insert into public.activity_logs (staff_name, action_type, description) values
('System', 'Create', 'Initial database preloaded manually with tests profiles.');
