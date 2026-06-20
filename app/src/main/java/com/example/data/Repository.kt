package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AppRepository(private val appDao: AppDao) {

    private val syncScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
    var lastSyncError: String? = null

    fun pushToSupabase(task: suspend SupabaseRestService.() -> Unit) {
        val service = SupabaseClient.service ?: return
        syncScope.launch {
            try {
                task(service)
            } catch (e: Exception) {
                android.util.Log.e("SupabaseSync", "Error pushing to Supabase: ${e.message}", e)
            }
        }
    }

    // Active User (Simulation of Supabase login)
    val activeUser = appDao.getActiveUserProfile()

    suspend fun getActiveUserSync(id: String): UserProfile? = appDao.getUserProfileById(id)

    suspend fun loginAsUser(email: String, fullName: String, role: String, id: String = "mock-uid-123") {
        val profile = UserProfile(id = id, email = email, fullName = fullName, role = role)
        appDao.clearUserProfile()
        appDao.insertUserProfile(profile)
        logActivity(fullName, "Login", "User logged in with role of $role")
    }

    suspend fun logoutUser() {
        appDao.clearUserProfile()
        logActivity("System", "Logout", "User logged out")
    }

    // Customers with real-time status calculation based on dates and dues!
    val allCustomers = appDao.getAllCustomers()

    fun getCustomerByIdFlow(id: Int) = appDao.getCustomerByIdFlow(id)
    suspend fun getCustomerById(id: Int) = appDao.getCustomerById(id)

    suspend fun addCustomer(customer: Customer, referredByType: String = "Direct", referrerName: String = "", staffName: String = "Owner"): Int {
        val customerId = appDao.insertCustomer(customer).toInt()
        
        // Log Referral if applicable
        if (referredByType != "Direct" && referrerName.isNotEmpty()) {
            val ref = CustomerReferral(
                customerId = customerId,
                customerName = customer.customerName,
                referredByType = referredByType,
                referrerName = referrerName
            )
            val generatedRefId = appDao.insertCustomerReferral(ref).toInt()
            val savedRef = ref.copy(id = generatedRefId)
            pushToSupabase { upsertCustomerReferral(body = listOf(savedRef.toDto())) }
        }

        val updatedCustomer = appDao.getCustomerById(customerId)
        if (updatedCustomer != null) {
            pushToSupabase { upsertCustomer(body = listOf(updatedCustomer.toDto())) }
        }

        logActivity(staffName, "Create", "Added new customer: ${customer.customerName}")
        return customerId
    }

    suspend fun updateCustomer(customer: Customer, staffName: String = "Owner") {
        appDao.insertCustomer(customer)
        pushToSupabase { upsertCustomer(body = listOf(customer.toDto())) }
        logActivity(staffName, "Update", "Updated customer: ${customer.customerName}")
    }

    suspend fun deleteCustomer(customer: Customer, staffName: String = "Owner") {
        appDao.deleteCustomer(customer)
        appDao.deleteDuesByCustomerId(customer.id)
        pushToSupabase {
            deleteCustomer("id=eq.${customer.id}")
            deleteDue("customer_id=eq.${customer.id}")
        }
        logActivity(staffName, "Delete", "Deleted customer: ${customer.customerName}")
    }

    // Dues
    val allDues = appDao.getAllDues()
    fun getDuesForCustomer(customerId: Int) = appDao.getDuesForCustomer(customerId)

    suspend fun addDue(due: Due, staffName: String = "Owner") {
        val generatedId = appDao.insertDue(due).toInt()
        val savedDue = due.copy(id = generatedId)
        pushToSupabase { upsertDue(body = listOf(savedDue.toDto())) }
        logActivity(staffName, "Create", "Added due entry: ₹${due.dueAmount} for ${due.customerName}")
        recalculateCustomerStatus(due.customerId)
    }

    suspend fun deleteDue(dueId: Int, customerId: Int, staffName: String = "Owner") {
        val due = appDao.getDueById(dueId)
        appDao.deleteDueById(dueId)
        pushToSupabase { deleteDue("id=eq.$dueId") }
        due?.let {
            logActivity(staffName, "Delete", "Deleted due entry: ₹${it.dueAmount} for ${it.customerName}")
        }
        recalculateCustomerStatus(customerId)
    }

    // Payments
    val allPayments = appDao.getAllPayments()
    fun getPaymentsForCustomer(customerId: Int) = appDao.getPaymentsForCustomer(customerId)

    suspend fun collectPayment(
        customerId: Int,
        dueId: Int,
        amountCollected: Double,
        paymentMode: String,
        notes: String,
        collectedBy: String
    ): Boolean {
        val customer = appDao.getCustomerById(customerId) ?: return false
        val due = appDao.getDueById(dueId) ?: return false

        // 1. Log Payment
        val entry = PaymentEntry(
            customerId = customerId,
            customerName = customer.customerName,
            dueId = dueId,
            amountPaid = amountCollected,
            paymentDate = getTodayDateString(),
            paymentMode = paymentMode,
            notes = notes,
            collectedBy = collectedBy
        )
        val generatedPaymentId = appDao.insertPaymentEntry(entry).toInt()
        val savedEntry = entry.copy(id = generatedPaymentId)

        // 2. Update Due Amount
        val newDueAmount = (due.dueAmount - amountCollected).coerceAtLeast(0.0)
        val newDueStatus = if (newDueAmount <= 0.0) {
            "Paid"
        } else {
            "Partial Paid"
        }

        val updatedDue = due.copy(
            dueAmount = newDueAmount,
            dueStatus = newDueStatus,
            notes = if (notes.isNotEmpty()) notes else due.notes
        )
        appDao.insertDue(updatedDue)

        // 3. Update Customer Pending
        val newCustomerPending = (customer.pendingAmount - amountCollected).coerceAtLeast(0.0)
        val updatedCustomer = customer.copy(
            pendingAmount = newCustomerPending
        )
        appDao.insertCustomer(updatedCustomer)

        pushToSupabase {
            upsertPaymentEntry(body = listOf(savedEntry.toDto()))
            upsertDue(body = listOf(updatedDue.toDto()))
            upsertCustomer(body = listOf(updatedCustomer.toDto()))
        }

        logActivity(
            collectedBy,
            "Payment",
            "Collected ₹$amountCollected from ${customer.customerName} via $paymentMode"
        )

        // 4. Recalculate status automatically
        recalculateCustomerStatus(customerId)
        return true
    }

    // Follow-Ups
    val allFollowups = appDao.getAllFollowups()
    fun getFollowupsForCustomer(customerId: Int) = appDao.getFollowupsForCustomer(customerId)

    suspend fun addFollowup(followup: PaymentFollowup, staffName: String = "Owner") {
        val generatedId = appDao.insertFollowup(followup).toInt()
        val savedFollowup = followup.copy(id = generatedId)
        pushToSupabase { upsertPaymentFollowup(body = listOf(savedFollowup.toDto())) }
        logActivity(staffName, "Followup", "Added follow-up for ${followup.customerName}: ${followup.notes}")
        
        // If status is promised or paid, can optionally update due notes, etc.
        if (followup.status == "Paid") {
            recalculateCustomerStatus(followup.customerId)
        }
    }

    // Referral Management
    val allReferralPersons = appDao.getAllReferralPersons()
    val allCustomerReferrals = appDao.getAllCustomerReferrals()

    suspend fun addReferralPerson(person: ReferralPerson, staffName: String = "Owner"): Int {
        val id = appDao.insertReferralPerson(person).toInt()
        val savedPerson = person.copy(id = id)
        pushToSupabase { upsertReferralPerson(body = listOf(savedPerson.toDto())) }
        logActivity(staffName, "Create", "Added referral person: ${person.fullName}")
        return id
    }

    suspend fun deleteReferralPerson(id: Int, staffName: String = "Owner") {
        val person = appDao.getReferralPersonById(id)
        if (person != null) {
            appDao.deleteReferralPersonById(id)
            pushToSupabase { deleteReferralPerson("id=eq.$id") }
            logActivity(staffName, "Delete", "Deleted referral person: ${person.fullName}")
        }
    }

    // Staff Management
    val allStaff = appDao.getAllStaff()

    suspend fun addStaff(staff: StaffMember, operatorName: String = "Owner") {
        val generatedId = appDao.insertStaff(staff).toInt()
        val savedStaff = staff.copy(id = generatedId)
        pushToSupabase { upsertStaffMember(body = listOf(savedStaff.toDto())) }
        logActivity(operatorName, "Create", "Added/updated staff member: ${staff.name} (${staff.role})")
    }

    suspend fun deleteStaff(id: Int, operatorName: String = "Owner") {
        val staff = appDao.getStaffById(id)
        if (staff != null) {
            appDao.deleteStaffById(id)
            pushToSupabase { deleteStaffMember("id=eq.$id") }
            logActivity(operatorName, "Delete", "Deleted staff member: ${staff.name}")
        }
    }

    // WhatsApp logs
    val allReminderLogs = appDao.getAllReminderLogs()

    suspend fun logWhatsAppReminder(customerId: Int, customerName: String, messageText: String, sentBy: String) {
        val log = WhatsAppReminderLog(
            sentDate = getDateTimeString(),
            sentBy = sentBy,
            customerId = customerId,
            customerName = customerName,
            message = messageText
        )
        val generatedId = appDao.insertReminderLog(log).toInt()
        val savedLog = log.copy(id = generatedId)
        pushToSupabase { upsertWhatsAppReminderLog(body = listOf(savedLog.toDto())) }
        logActivity(sentBy, "Reminder", "Sent WhatsApp reminder to $customerName")
    }

    // Activity Logs
    val allActivityLogs = appDao.getAllActivityLogs()

    suspend fun logActivity(staffName: String, actionType: String, description: String) {
        val log = ActivityLog(
            staffName = staffName,
            actionType = actionType,
            description = description
        )
        val generatedId = appDao.insertActivityLog(log).toInt()
        val savedLog = log.copy(id = generatedId)
        pushToSupabase { upsertActivityLog(body = listOf(savedLog.toDto())) }
    }

    // Re-calculates and updates Customer Status
    suspend fun recalculateCustomerStatus(customerId: Int) {
        val customer = appDao.getCustomerById(customerId) ?: return
        
        // Find dues belonging to this customer
        var hasCritical = false
        var hasOverdue = false
        var hasPending = false
        var totalPending = 0.0

        val dues = getDuesSync(customerId)
        for (due in dues) {
            if (due.dueStatus == "Paid" || due.dueAmount <= 0.0) continue
            
            totalPending += due.dueAmount
            val daysDiff = getDaysDifference(due.dueDate)
            
            when {
                daysDiff >= 60 -> {
                    hasCritical = true
                    // Update due state in DB if it changed
                    if (due.dueStatus != "Critical") {
                        appDao.insertDue(due.copy(dueStatus = "Critical"))
                    }
                }
                daysDiff > 0 -> {
                    hasOverdue = true
                    if (due.dueStatus != "Overdue") {
                        appDao.insertDue(due.copy(dueStatus = "Overdue"))
                    }
                }
                else -> {
                    hasPending = true
                    if (due.dueStatus != "Pending") {
                        appDao.insertDue(due.copy(dueStatus = "Pending"))
                    }
                }
            }
        }

        val newStatus = when {
            totalPending <= 0.0 -> "Paid"
            hasCritical -> "Critical"
            hasOverdue -> "Overdue"
            hasPending -> "Pending"
            else -> "Active"
        }

        // Save updated info
        val updatedCustomer = customer.copy(
            pendingAmount = totalPending,
            status = newStatus
        )
        appDao.insertCustomer(updatedCustomer)
    }

    // Sync helpers
    private suspend fun getDuesSync(customerId: Int): List<Due> {
        // Run database queries on a safe background thread (handled by Room)
        // Since we need it blockingly inside suspend sequence, we'd query dues from a Map or use a specific async flow
        // To be safe, we can run raw queries. But since Live Flow is used, we can query a simple helper to prevent thread issues.
        // Let's implement helper direct queries in DAO or we can estimate based on our current data.
        return appDao.getAllDues().map { list -> list.filter { it.customerId == customerId } }.currentValue()
    }

    // Helper to extract current value from custom flows safely
    private suspend fun <T> Flow<T>.currentValue(): T {
        return this.first()
    }

    // Date Arithmetic Helper Utilities
    fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    private fun getDateTimeString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        return sdf.format(Date())
    }

    fun getDaysDifference(dueDateStr: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val dueDate = sdf.parse(dueDateStr) ?: return 0L
            val currentDateString = sdf.format(Date())
            val currentDate = sdf.parse(currentDateString) ?: return 0L
            
            val diff = currentDate.time - dueDate.time
            diff / (24 * 60 * 60 * 1000)
        } catch (e: Exception) {
            0L
        }
    }

    // Seed/Prepopulate standard records on app initial start.
    suspend fun seedDatabase() {
        // Prepopulate owner user
        val mockOwnerId = "mock-owner-uid"
        val existing = appDao.getUserProfileById(mockOwnerId)
        if (existing == null) {
            appDao.insertUserProfile(
                UserProfile(
                    id = mockOwnerId,
                    email = "owner@phoneworld.com",
                    fullName = "Rais Memon",
                    role = "Owner",
                    mobileNumber = "9724493045"
                )
            )
            
            // Seed Staff members
            appDao.insertStaff(StaffMember(name = "Rais Memon (Owner)", mobile = "9724493045", role = "Owner", isActive = true))

            // Seed Referral Persons
            appDao.insertReferralPerson(ReferralPerson(fullName = "Harshil Shah", mobileNumber = "+91 88888 77777", address = "Kalawad Road", city = "Rajkot", notes = "Supplier Referral"))
            appDao.insertReferralPerson(ReferralPerson(fullName = "Mayur Bhai", mobileNumber = "+91 77777 66666", address = "Gondal Road", city = "Rajkot", notes = "Customer Friend"))

            // Seed customers with overdue days to show in dashboard, critical zone, etc.
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cal = Calendar.getInstance()

            // 1. Active/Pending Customer (Due date tomorrow)
            cal.time = Date()
            cal.add(Calendar.DAY_OF_YEAR, 2)
            val dueTomorrow = sdf.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, -5)
            val purDate1 = sdf.format(cal.time)
            
            val c1Id = appDao.insertCustomer(Customer(
                customerName = "Vijay Vasoya",
                mobileNumber = "9909912345",
                alternateMobileNumber = "9909954321",
                address = "Shakti Nagar",
                cityVillage = "Rajkot",
                productPurchased = "iPhone 15 Pro Max 256GB",
                purchaseDate = purDate1,
                totalBillAmount = 145000.0,
                pendingAmount = 45000.0,
                notes = "Promised to pay before deadline.",
                status = "Pending"
            )).toInt()
            appDao.insertDue(Due(customerId = c1Id, customerName = "Vijay Vasoya", dueAmount = 45000.0, dueDate = dueTomorrow, reminderDate = dueTomorrow, dueStatus = "Pending", notes = "Final installment"))

            // 2. Overdue Customer (Due date passed by 10 days)
            cal.time = Date()
            cal.add(Calendar.DAY_OF_YEAR, -10)
            val duePassed10 = sdf.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, -10)
            val purDate2 = sdf.format(cal.time)

            val c2Id = appDao.insertCustomer(Customer(
                customerName = "Suresh Rathod",
                mobileNumber = "9825611223",
                address = "Yagnik Road",
                cityVillage = "Rajkot",
                productPurchased = "Samsung S24 Ultra",
                purchaseDate = purDate2,
                totalBillAmount = 120000.0,
                pendingAmount = 30000.0,
                notes = "Follow up required. Customer requested grace days.",
                status = "Overdue"
            )).toInt()
            appDao.insertDue(Due(customerId = c2Id, customerName = "Suresh Rathod", dueAmount = 30000.0, dueDate = duePassed10, reminderDate = duePassed10, dueStatus = "Overdue", notes = "Second installment"))

            // 3. Critical Customer (Due date passed by 65 days)
            cal.time = Date()
            cal.add(Calendar.DAY_OF_YEAR, -65)
            val duePassed65 = sdf.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, -15)
            val purDate3 = sdf.format(cal.time)

            val c3Id = appDao.insertCustomer(Customer(
                customerName = "Dharmesh Gohil",
                mobileNumber = "9426288990",
                address = "Station Road",
                cityVillage = "Gondal",
                productPurchased = "OnePlus 12 512GB",
                purchaseDate = purDate3,
                totalBillAmount = 65000.0,
                pendingAmount = 25000.0,
                notes = "Critically Overdue. Call and action needed immediately.",
                status = "Critical"
            )).toInt()
            appDao.insertDue(Due(customerId = c3Id, customerName = "Dharmesh Gohil", dueAmount = 25000.0, dueDate = duePassed65, reminderDate = duePassed65, dueStatus = "Critical", notes = "First installment is overdue long time"))

            // Add dynamic customer referrals
            appDao.insertCustomerReferral(CustomerReferral(customerId = c1Id, customerName = "Vijay Vasoya", referredByType = "Staff", referrerName = "Keval Patel"))
            appDao.insertCustomerReferral(CustomerReferral(customerId = c2Id, customerName = "Suresh Rathod", referredByType = "External Person", referrerName = "Harshil Shah"))

            // Add activities
            appDao.insertActivityLog(ActivityLog(staffName = "System", actionType = "Create", description = "Initial seed data loaded successfully."))
        }
    }

    suspend fun syncSupabaseToRoom(): Boolean {
        val service = SupabaseClient.service ?: return false
        return try {
            // 1. Fetch from Supabase
            val customers = service.getCustomers().map { it.toEntity() }
            val dues = service.getDues().map { it.toEntity() }
            val payments = service.getPaymentEntries().map { it.toEntity() }
            val followups = service.getPaymentFollowups().map { it.toEntity() }
            val referrals = service.getCustomerReferrals().map { it.toEntity() }
            val referralPersons = service.getReferralPersons().map { it.toEntity() }
            val staff = service.getStaffMembers().map { it.toEntity() }
            val reminderLogs = service.getWhatsAppReminderLogs().map { it.toEntity() }
            val templates = service.getMessageTemplates().map { it.toEntity() }
            val activityLogs = service.getActivityLogs().map { it.toEntity() }

            // 2. Insert into Room locally
            customers.forEach { appDao.insertCustomer(it) }
            dues.forEach { appDao.insertDue(it) }
            payments.forEach { appDao.insertPaymentEntry(it) }
            followups.forEach { appDao.insertFollowup(it) }
            referrals.forEach { appDao.insertCustomerReferral(it) }
            referralPersons.forEach { appDao.insertReferralPerson(it) }
            staff.forEach { appDao.insertStaff(it) }
            reminderLogs.forEach { appDao.insertReminderLog(it) }
            templates.forEach { appDao.insertTemplate(it) }
            activityLogs.forEach { appDao.insertActivityLog(it) }

            logActivity("System", "Sync", "Successfully synced all database tables from Supabase.")
            true
        } catch (e: Exception) {
            android.util.Log.e("SupabaseSync", "Failed to sync tables from Supabase: ${e.message}", e)
            lastSyncError = "Pull Error: ${e.message ?: e.toString()}"
            false
        }
    }

    suspend fun syncRoomToSupabase(): Boolean {
        val service = SupabaseClient.service ?: return false
        return try {
            // Retrieve all from Room
            val customers = appDao.getAllCustomers().first().map { it.toDto() }
            val dues = appDao.getAllDues().first().map { it.toDto() }
            val payments = appDao.getAllPayments().first().map { it.toDto() }
            val followups = appDao.getAllFollowups().first().map { it.toDto() }
            val referralPersons = appDao.getAllReferralPersons().first().map { it.toDto() }
            val customerReferrals = appDao.getAllCustomerReferrals().first().map { it.toDto() }
            val staff = appDao.getAllStaff().first().map { it.toDto() }
            val reminderLogs = appDao.getAllReminderLogs().first().map { it.toDto() }
            val templates = appDao.getAllTemplates().first().map { it.toDto() }
            val activityLogs = appDao.getAllActivityLogs().first().map { it.toDto() }

            // Upsert in batches
            if (customers.isNotEmpty()) service.upsertCustomer(body = customers)
            if (dues.isNotEmpty()) service.upsertDue(body = dues)
            if (payments.isNotEmpty()) service.upsertPaymentEntry(body = payments)
            if (followups.isNotEmpty()) service.upsertPaymentFollowup(body = followups)
            if (referralPersons.isNotEmpty()) service.upsertReferralPerson(body = referralPersons)
            if (customerReferrals.isNotEmpty()) service.upsertCustomerReferral(body = customerReferrals)
            if (staff.isNotEmpty()) service.upsertStaffMember(body = staff)
            if (reminderLogs.isNotEmpty()) service.upsertWhatsAppReminderLog(body = reminderLogs)
            if (templates.isNotEmpty()) service.upsertMessageTemplate(body = templates)
            if (activityLogs.isNotEmpty()) service.upsertActivityLog(body = activityLogs)

            logActivity("System", "Sync", "Successfully pushed all local database tables to Supabase.")
            true
        } catch (e: Exception) {
            android.util.Log.e("SupabaseSync", "Failed to upload tables to Supabase: ${e.message}", e)
            lastSyncError = "Push Error: ${e.message ?: e.toString()}"
            false
        }
    }
}
