package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // User Profile
    @Query("SELECT * FROM users_profile LIMIT 1")
    fun getActiveUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM users_profile WHERE id = :id LIMIT 1")
    suspend fun getUserProfileById(id: String): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    @Query("DELETE FROM users_profile")
    suspend fun clearUserProfile()


    // Customer Screen
    @Query("SELECT * FROM customers ORDER BY createdAt DESC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    suspend fun getCustomerById(id: Int): Customer?

    @Query("SELECT * FROM customers WHERE id = :id LIMIT 1")
    fun getCustomerByIdFlow(id: Int): Flow<Customer?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomerById(id: Int)


    // Dues
    @Query("SELECT * FROM dues ORDER BY dueDate ASC")
    fun getAllDues(): Flow<List<Due>>

    @Query("SELECT * FROM dues WHERE id = :id LIMIT 1")
    suspend fun getDueById(id: Int): Due?

    @Query("SELECT * FROM dues WHERE customerId = :customerId ORDER BY dueDate ASC")
    fun getDuesForCustomer(customerId: Int): Flow<List<Due>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDue(due: Due): Long

    @Query("DELETE FROM dues WHERE id = :id")
    suspend fun deleteDueById(id: Int)

    @Query("DELETE FROM dues WHERE customerId = :customerId")
    suspend fun deleteDuesByCustomerId(customerId: Int)


    // Payments
    @Query("SELECT * FROM payment_entries ORDER BY paymentDate DESC")
    fun getAllPayments(): Flow<List<PaymentEntry>>

    @Query("SELECT * FROM payment_entries WHERE customerId = :customerId ORDER BY paymentDate DESC")
    fun getPaymentsForCustomer(customerId: Int): Flow<List<PaymentEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentEntry(payment: PaymentEntry): Long

    @Query("DELETE FROM payment_entries WHERE id = :id")
    suspend fun deletePaymentById(id: Int)


    // Follow Ups
    @Query("SELECT * FROM payment_followups ORDER BY followUpDate DESC")
    fun getAllFollowups(): Flow<List<PaymentFollowup>>

    @Query("SELECT * FROM payment_followups WHERE customerId = :customerId ORDER BY followUpDate DESC")
    fun getFollowupsForCustomer(customerId: Int): Flow<List<PaymentFollowup>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowup(followup: PaymentFollowup): Long

    @Query("DELETE FROM payment_followups WHERE id = :id")
    suspend fun deleteFollowupById(id: Int)


    // Referral Persons
    @Query("SELECT * FROM referral_persons ORDER BY fullName ASC")
    fun getAllReferralPersons(): Flow<List<ReferralPerson>>

    @Query("SELECT * FROM referral_persons WHERE id = :id LIMIT 1")
    suspend fun getReferralPersonById(id: Int): ReferralPerson?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferralPerson(person: ReferralPerson): Long

    @Query("DELETE FROM referral_persons WHERE id = :id")
    suspend fun deleteReferralPersonById(id: Int)


    // Customer Referrals
    @Query("SELECT * FROM customer_referrals ORDER BY id DESC")
    fun getAllCustomerReferrals(): Flow<List<CustomerReferral>>

    @Query("SELECT * FROM customer_referrals WHERE customerId = :customerId ORDER BY id DESC")
    fun getReferralForCustomer(customerId: Int): Flow<List<CustomerReferral>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomerReferral(referral: CustomerReferral): Long


    // Staff Members
    @Query("SELECT * FROM staff_members ORDER BY name ASC")
    fun getAllStaff(): Flow<List<StaffMember>>

    @Query("SELECT * FROM staff_members WHERE id = :id LIMIT 1")
    suspend fun getStaffById(id: Int): StaffMember?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStaff(staff: StaffMember): Long

    @Query("DELETE FROM staff_members WHERE id = :id")
    suspend fun deleteStaffById(id: Int)


    // WhatsApp reminder logs
    @Query("SELECT * FROM whatsapp_reminder_logs ORDER BY sentDate DESC")
    fun getAllReminderLogs(): Flow<List<WhatsAppReminderLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminderLog(log: WhatsAppReminderLog): Long


    // Message Templates
    @Query("SELECT * FROM message_templates")
    fun getAllTemplates(): Flow<List<MessageTemplate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: MessageTemplate): Long


    // Activity Logs
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAllActivityLogs(): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(log: ActivityLog): Long
}
