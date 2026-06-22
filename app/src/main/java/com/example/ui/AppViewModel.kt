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

    // Secure MPIN State (Persisted in SharedPreferences)
    private val prefs = application.getSharedPreferences("crm_app_prefs", android.content.Context.MODE_PRIVATE)
    val mpinState = MutableStateFlow(prefs.getString("security_mpin", "1234") ?: "1234")

    fun updateMpin(newMpin: String) {
        prefs.edit().putString("security_mpin", newMpin).apply()
        mpinState.value = newMpin
        logActivity("Security", "MPIN updated successfully.")
    }

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

    val whatsAppReminderDialogData = MutableStateFlow<WhatsAppReminderData?>(null)

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
        referrerName: String,
        invoiceNumber: String = "",
        modelDetail: String = "",
        dueDays: Int = 30
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
                status = status,
                invoiceNumber = invoiceNumber,
                modelDetail = modelDetail
            )
            val customerId = repository.addCustomer(customer, referredByType, referrerName, staffNameState.value)
            
            // If there's a pending amount, auto-create a default Due installation
            if (pending > 0.0) {
                val cal = java.util.Calendar.getInstance()
                cal.add(java.util.Calendar.DAY_OF_YEAR, dueDays) // Due in dueDays
                val dateStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(cal.time)
                
                val due = Due(
                    customerId = customerId,
                    customerName = name,
                    dueAmount = pending,
                    dueDate = dateStr,
                    reminderDate = dateStr,
                    dueStatus = "Pending",
                    notes = if (dueDays != 30) "Pay after $dueDays days promise" else "Direct auto installments",
                    invoiceNumber = invoiceNumber
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

    private fun calculateDaysPassedLocal(dateString: String): Long {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val date = sdf.parse(dateString) ?: return 0L
            val currentDate = sdf.parse(sdf.format(java.util.Date())) ?: return 0L
            val diff = currentDate.time - date.time
            diff / (24 * 60 * 60 * 1000)
        } catch (e: Exception) {
            0L
        }
    }

    fun seed50RandomCustomers() {
        viewModelScope.launch {
            val names = listOf(
                "Arvind Kejriwal", "Girish Savalia", "Mansukh Kakadiya", "Rajesh Vora", "Hasmukh Patel",
                "Kishor Ghetiya", "Bhupat Bhayani", "Lalit Vasoya", "Hardik Patel", "Ramesh Chothani",
                "Dinesh Tilva", "Sanjay Sorathiya", "Alpesh Kathiriya", "Gopal Italia", "Paresh Dhanani",
                "Vijay Rupani", "Anandiben Patel", "Keshubhai Patel", "Suresh Modi", "Narendra Makwana",
                "Mansukh Mandaviya", "Parshottam Rupala", "Kanu Bhalala", "Jitu Vaghani", "Harsh Sanghavi",
                "Cheteshwar Pujara", "Ravindra Jadeja", "Axar Patel", "Jasprit Bumrah", "Keval Kakadiya",
                "Jenish Vekaria", "Vipul Radadiya", "Vitthal Radadiya", "Jayesh Radadiya", "Lalit Kagathra",
                "Pravin Togadia", "Nalin Kotadiya", "Dhiru Gajera", "Siddharth Randeria", "Malhar Thakar",
                "Manan Desai", "Kirtidan Gadhvi", "Geeta Rabari", "Kinjal Dave", "Osman Mir",
                "Farida Mir", "Devraj Gadhvi", "Rajbha Gadhvi", "Arvind Vegda", "Mayur Patel"
            )

            val cities = listOf(
                "Rajkot", "Surat", "Ahmedabad", "Vadodara", "Jamnagar", "Bhavnagar", "Morbi", "Gondal", 
                "Junagadh", "Bhuj", "Jasdan", "Jetpur", "Anand", "Nadiad", "Mehsana", "Amreli"
            )

            val products = listOf(
                Pair("iPhone 15 Pro", "256GB Black Titanium"),
                Pair("Samsung S24 Ultra", "512GB Titanium Yellow"),
                Pair("OnePlus 12", "512GB Flowy Emerald"),
                Pair("iPhone 15", "128GB Blue Matte"),
                Pair("Vivo V30 Pro", "256GB Classic Black"),
                Pair("Samsung A55 5G", "128GB Awesome Iceblue"),
                Pair("OnePlus Nord CE4", "128GB Dark Chrome"),
                Pair("Oppo Reno 11 Pro", "256GB Wave Green"),
                Pair("Xiaomi 14", "512GB Jade Green"),
                Pair("Google Pixel 8 Pro", "256GB Bay Blue")
            )

            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            
            for (i in 0 until 50) {
                val name = names[i % names.size]
                val city = cities[i % cities.size]
                val prodPair = products[i % products.size]
                val mob = "9${(10000000 + i * 1793) % 99999999}"
                
                // Varying purchase date from 3 to 220 days ago
                val cal = java.util.Calendar.getInstance()
                val daysAgo = 3 + (i * 4) // Spreads from 3 to 200+ days ago
                cal.add(java.util.Calendar.DAY_OF_YEAR, -daysAgo)
                val purchaseDateStr = sdf.format(cal.time)
                
                // Varied bill amount and outstanding pending
                val isPaid = (i % 5 == 0) // Every 5th customer has paid fully
                val billAmount = 40000.0 + (i * 1500)
                val pendingAmount = if (isPaid) 0.0 else (billAmount * 0.3)
                
                val status = when {
                    isPaid -> "Paid"
                    daysAgo > 90 -> "Critical"
                    daysAgo > 35 -> "Overdue"
                    else -> "Pending"
                }

                val notes = "Auto-seeded demo record ${i + 1}. Ordered from applet simulator."

                val customer = Customer(
                    customerName = name,
                    mobileNumber = mob,
                    alternateMobileNumber = "99099" + (10000 + i),
                    address = "Applet Street Block ${i + 1}",
                    cityVillage = city,
                    productPurchased = prodPair.first,
                    purchaseDate = purchaseDateStr,
                    totalBillAmount = billAmount,
                    pendingAmount = pendingAmount,
                    notes = notes,
                    status = status,
                    invoiceNumber = "INV-" + (1000 + i),
                    modelDetail = prodPair.second
                )

                val custId = repository.addCustomer(
                    customer = customer,
                    referredByType = "Direct",
                    referrerName = "",
                    staffName = staffNameState.value
                )

                if (pendingAmount > 0.0) {
                    // Due date is purchase date + 30 days
                    val dueCal = java.util.Calendar.getInstance()
                    dueCal.time = cal.time
                    dueCal.add(java.util.Calendar.DAY_OF_YEAR, 30)
                    val dueDateStr = sdf.format(dueCal.time)
                    
                    val parsedDays = calculateDaysPassedLocal(dueDateStr)
                    val dueStatus = when {
                        parsedDays >= 60 -> "Critical"
                        parsedDays > 0 -> "Overdue"
                        else -> "Pending"
                    }

                    val due = Due(
                        customerId = custId,
                        customerName = name,
                        dueAmount = pendingAmount,
                        dueDate = dueDateStr,
                        reminderDate = dueDateStr,
                        dueStatus = dueStatus,
                        notes = "Seed Due for installment ${i + 1}",
                        invoiceNumber = "INV-" + (1000 + i)
                    )
                    repository.addDue(due, staffNameState.value)
                }
            }
            logActivity("System", "Bulk seeding of 50 diverse test customers completed successfully.")
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
