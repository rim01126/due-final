package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = AppRepository(database.appDao())

    // Settings States
    val languageState = MutableStateFlow(Lang.EN)
    val darkThemeState = MutableStateFlow(true) // Defaults to Premium Dark Mode

    // Active Session Management
    val isLoggedInState = MutableStateFlow(false)
    val loginErrorState = MutableStateFlow<String?>(null)

    // Currently simulated logged-in user
    val currentRoleState = MutableStateFlow("Owner") // "Owner" or "Staff"
    val staffNameState = MutableStateFlow("Rais Memon") // Current name for logs

    // Selected Screens Navigation (Simple route string)
    val currentRouteState = MutableStateFlow("dashboard")

    // Selection contexts
    val selectedCustomerIdState = MutableStateFlow<Int?>(null)

    // Filter states
    val searchCustomerQueryState = MutableStateFlow("")
    val filterStatusState = MutableStateFlow("All") // "All", "Active", "Paid", "Pending", "Overdue", "Critical"

    // Observed Data Lists Live from DB
    val customersList = repository.allCustomers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val duesList = repository.allDues.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val paymentsList = repository.allPayments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val followupsList = repository.allFollowups.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val referralPersonsList = repository.allReferralPersons.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val customerReferralsList = repository.allCustomerReferrals.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val staffMemberList = repository.allStaff.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val reminderLogsList = repository.allReminderLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val activityLogsList = repository.allActivityLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun getCustomerByIdFlow(id: Int): Flow<Customer?> = repository.getCustomerByIdFlow(id)
    fun getDuesForCustomer(customerId: Int): Flow<List<Due>> = repository.getDuesForCustomer(customerId)
    fun getPaymentsForCustomer(customerId: Int): Flow<List<PaymentEntry>> = repository.getPaymentsForCustomer(customerId)
    fun getFollowupsForCustomer(customerId: Int): Flow<List<PaymentFollowup>> = repository.getFollowupsForCustomer(customerId)

    // Templates customization
    val englishTemplateState = MutableStateFlow(
        "Hello {customer_name},\n\nThis is a reminder from Phone World. Your pending installment amount is ₹{due_amount}.\n\nPlease make payment by {due_date}.\n\nThank you!"
    )
    
    val gujaratiTemplateState = MutableStateFlow(
        "નમસ્તે {customer_name},\n\nPhone World તરફથી યાદ અપાવવામાં આવે છે કે તમારી બાકી રકમ ₹{due_amount} છે.\n\nકૃપા કરીને {due_date} સુધી ચુકવણી કરો.\n\nઆભાર."
    )

    // Supabase Sync States
    val syncStatusState = MutableStateFlow<String>("Idle")
    val syncErrorState = MutableStateFlow<String?>(null)
    val isSupabaseConfigured = SupabaseClient.isConfigured

    init {
        viewModelScope.launch {
            // Seed database on launch if empty
            repository.seedDatabase()
            // Pull initial sync safely from Supabase if configured
            if (isSupabaseConfigured) {
                try {
                    syncStatusState.value = "Syncing"
                    val success = repository.syncSupabaseToRoom()
                    syncStatusState.value = if (success) "Success" else "Failed"
                } catch (e: Exception) {
                    syncStatusState.value = "Failed"
                }
            }
        }
    }

    fun syncWithSupabase() {
        if (!isSupabaseConfigured) {
            syncStatusState.value = "Not Configured"
            return
        }
        viewModelScope.launch {
            syncStatusState.value = "Syncing"
            syncErrorState.value = null
            // Reset repository's last error before starting
            repository.lastSyncError = null
            
            val pullOk = repository.syncSupabaseToRoom()
            val pushOk = repository.syncRoomToSupabase()
            if (pullOk && pushOk) {
                syncStatusState.value = "Success"
            } else {
                syncStatusState.value = "Failed"
                syncErrorState.value = repository.lastSyncError ?: "Verification failed. Check network or credentials."
            }
        }
    }

    // Role switcher helper
    fun switchUserRole(role: String) {
        currentRoleState.value = role
        if (role == "Owner") {
            staffNameState.value = "Rais Memon"
        } else {
            staffNameState.value = "Keval Patel"
        }
        viewModelScope.launch {
            repository.logActivity(staffNameState.value, "ProfileSwitch", "Switched current role Profile to: $role")
        }
    }

    // Interactive Owner/Staff authentication credentials
    fun loginWithCredentials(mobile: String, pass: String): Boolean {
        // Owner Check (hardcoded / standard)
        if ((mobile == "9724493045" || mobile == "owner@phoneworld.com" || mobile == "Owner" || mobile == "Rais Memon") && pass == "123456789") {
            currentRoleState.value = "Owner"
            staffNameState.value = "Rais Memon"
            isLoggedInState.value = true
            loginErrorState.value = null
            logActivity("Login", "Owner validated successfully.")
            return true
        }
        
        // Dynamic DB staff check
        val matchedStaff = staffMemberList.value.find { 
            (it.mobile == mobile || it.email == mobile || it.name == mobile) && 
            it.password == pass
        }
        
        if (matchedStaff != null) {
            if (!matchedStaff.isActive) {
                loginErrorState.value = "This account has been disabled."
                return false
            }
            currentRoleState.value = matchedStaff.role // "Owner" or "Staff"
            staffNameState.value = matchedStaff.name
            isLoggedInState.value = true
            loginErrorState.value = null
            logActivity("Login", "${matchedStaff.role} (${matchedStaff.name}) validated successfully.")
            return true
        }
        
        loginErrorState.value = "Incorrect mobile/email or password."
        return false
    }

    fun logout() {
        isLoggedInState.value = false
        loginErrorState.value = null
        currentRouteState.value = "dashboard"
    }

    fun logActivity(actionType: String, description: String) {
        viewModelScope.launch {
            repository.logActivity(staffNameState.value, actionType, description)
        }
    }

    // Customer mutations
    fun addCustomer(
        name: String,
        mobile: String,
        altMobile: String,
        address: String,
        city: String,
        product: String,
        price: Double,
        pending: Double,
        notes: String,
        referredByType: String,
        referrerName: String
    ) {
        viewModelScope.launch {
            val status = if (pending <= 0.0) "Paid" else "Pending"
            val customer = Customer(
                customerName = name,
                mobileNumber = mobile,
                alternateMobileNumber = altMobile,
                address = address,
                cityVillage = city,
                productPurchased = product,
                purchaseDate = repository.getTodayDateString(),
                totalBillAmount = price,
                pendingAmount = pending,
                notes = notes,
                status = status
            )
            val customerId = repository.addCustomer(customer, referredByType, referrerName, staffNameState.value)
            
            // If there's a pending amount, auto-create a default Due installation
            if (pending > 0.0) {
                val cal = java.util.Calendar.getInstance()
                cal.add(java.util.Calendar.DAY_OF_YEAR, 30) // Due in 30 days
                val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(cal.time)
                
                val due = Due(
                    customerId = customerId,
                    customerName = name,
                    dueAmount = pending,
                    dueDate = dateStr,
                    reminderDate = dateStr,
                    dueStatus = "Pending",
                    notes = "Direct auto installments"
                )
                repository.addDue(due, staffNameState.value)
            }
        }
    }

    fun modifyCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.updateCustomer(customer, staffNameState.value)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer, staffNameState.value)
            if (selectedCustomerIdState.value == customer.id) {
                selectedCustomerIdState.value = null
            }
        }
    }

    // Due mutations
    fun addDue(customerId: Int, customerName: String, amount: Double, dueDate: String, notes: String, invoiceNumber: String = "") {
        viewModelScope.launch {
            val due = Due(
                customerId = customerId,
                customerName = customerName,
                dueAmount = amount,
                dueDate = dueDate,
                reminderDate = dueDate,
                dueStatus = "Pending",
                notes = notes,
                invoiceNumber = invoiceNumber
            )
            repository.addDue(due, staffNameState.value)
        }
    }

    fun deleteDue(dueId: Int, customerId: Int) {
        viewModelScope.launch {
            repository.deleteDue(dueId, customerId, staffNameState.value)
        }
    }

    // Payment collection
    fun collectPayment(customerId: Int, dueId: Int, amount: Double, mode: String, notes: String) {
        viewModelScope.launch {
            repository.collectPayment(customerId, dueId, amount, mode, notes, staffNameState.value)
        }
    }

    // Follow-ups
    fun addFollowup(customerId: Int, name: String, notes: String, nextFollow: String, promisePay: String, status: String) {
        viewModelScope.launch {
            val f = PaymentFollowup(
                customerId = customerId,
                customerName = name,
                followUpDate = repository.getTodayDateString(),
                notes = notes,
                nextFollowUpDate = nextFollow,
                promiseToPayDate = promisePay,
                staffName = staffNameState.value,
                status = status
            )
            repository.addFollowup(f, staffNameState.value)
        }
    }

    // Referral
    fun addReferralPerson(name: String, mobile: String, address: String, city: String, notes: String) {
        viewModelScope.launch {
            val rp = ReferralPerson(
                fullName = name,
                mobileNumber = mobile,
                address = address,
                city = city,
                notes = notes
            )
            repository.addReferralPerson(rp, staffNameState.value)
        }
    }

    fun deleteReferralPerson(id: Int) {
        viewModelScope.launch {
            repository.deleteReferralPerson(id, staffNameState.value)
        }
    }

    // Staff
    fun addStaffMember(name: String, mobile: String, email: String, password: String, role: String, isActive: Boolean, id: Int = 0) {
        viewModelScope.launch {
            repository.addStaff(
                StaffMember(id = id, name = name, mobile = mobile, email = email, password = password, role = role, isActive = isActive),
                staffNameState.value
            )
        }
    }

    fun deleteStaffMember(id: Int) {
        viewModelScope.launch {
            repository.deleteStaff(id, staffNameState.value)
        }
    }

    // WhatsApp Reminder Dispatch Mock
    fun sendWhatsAppReminder(customerId: Int, name: String, amount: Double, date: String, isGujarati: Boolean) {
        val template = if (isGujarati) gujaratiTemplateState.value else englishTemplateState.value
        val customized = template
            .replace("{customer_name}", name)
            .replace("{due_amount}", amount.toInt().toString())
            .replace("{due_date}", date)
            .replace("{shop_name}", "Phone World")

        viewModelScope.launch {
            repository.logWhatsAppReminder(customerId, name, customized, staffNameState.value)
        }
    }

    fun sendReferralWhatsAppReminder(personId: Int, name: String, messageText: String) {
        viewModelScope.launch {
            repository.logWhatsAppReminder(0, "[Referral] $name", messageText, staffNameState.value)
        }
    }

    // Helper function to aggregate data dynamically for each referral person
    fun getReferralMetricsForPerson(referrerName: String): ReferralMetrics {
        val custs = customersList.value
        val refs = customerReferralsList.value
        val ds = duesList.value

        // Referee customers referred by this person
        val refereeCustomers = custs.filter { c ->
            refs.any { r -> r.customerId == c.id && r.referrerName == referrerName }
        }

        val pendingAmt = refereeCustomers.sumOf { it.pendingAmount }

        val criticalCustomers = refereeCustomers.filter { c ->
            ds.any { d ->
                d.customerId == c.id &&
                d.dueStatus != "Paid" &&
                d.dueAmount > 0.0 &&
                repository.getDaysDifference(d.dueDate) >= 60
            }
        }

        return ReferralMetrics(
            pendingAmount = pendingAmt,
            criticalCount = criticalCustomers.size,
            criticalCustomers = criticalCustomers
        )
    }
}

data class ReferralMetrics(
    val pendingAmount: Double,
    val criticalCount: Int,
    val criticalCustomers: List<Customer>
)
