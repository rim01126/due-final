package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users_profile")
data class UserProfile(
    @PrimaryKey val id: String, // Matches Supabase Auth user UUID
    val email: String,
    val fullName: String,
    val role: String, // "Owner" or "Staff"
    val mobileNumber: String = ""
)

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val mobileNumber: String,
    val alternateMobileNumber: String = "",
    val address: String = "",
    val cityVillage: String = "",
    val productPurchased: String = "",
    val purchaseDate: String, // yyyy-MM-dd
    val totalBillAmount: Double,
    val pendingAmount: Double,
    val notes: String = "",
    val status: String, // "Active", "Paid", "Pending", "Overdue", "Critical"
    val invoiceNumber: String = "",
    val modelDetail: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "dues")
data class Due(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val customerName: String, // Denormalized for easier rendering
    val dueAmount: Double,
    val dueDate: String, // yyyy-MM-dd
    val reminderDate: String = "", // yyyy-MM-dd
    val dueStatus: String, // "Pending", "Partial Paid", "Paid", "Overdue", "Critical"
    val notes: String = "",
    val invoiceNumber: String = ""
)

@Entity(tableName = "payment_entries")
data class PaymentEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val customerName: String,
    val dueId: Int,
    val amountPaid: Double,
    val paymentDate: String, // yyyy-MM-dd
    val paymentMode: String, // "Cash", "UPI", "Card", "Finance", "Other"
    val notes: String = "",
    val collectedBy: String // Name or ID of Owner/Staff member
)

@Entity(tableName = "payment_followups")
data class PaymentFollowup(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val customerName: String,
    val followUpDate: String, // yyyy-MM-dd
    val notes: String,
    val nextFollowUpDate: String = "", // yyyy-MM-dd
    val promiseToPayDate: String = "", // yyyy-MM-dd
    val staffName: String,
    val status: String // "Pending", "Completed", "No Response", "Promised", "Paid"
)

@Entity(tableName = "referral_persons")
data class ReferralPerson(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val mobileNumber: String,
    val address: String = "",
    val city: String = "",
    val notes: String = ""
)

@Entity(tableName = "customer_referrals")
data class CustomerReferral(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerId: Int,
    val customerName: String,
    val referredByType: String, // "Owner", "Staff", "Existing Customer", "External Person"
    val referrerName: String, // Store name of direct referer
    val status: String = "Pending" // "Pending", "Collected"
)

@Entity(tableName = "staff_members")
data class StaffMember(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val mobile: String,
    val email: String = "",
    val password: String = "123456789",
    val role: String, // "Owner" or "Staff"
    val isActive: Boolean = true
)

@Entity(tableName = "whatsapp_reminder_logs")
data class WhatsAppReminderLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sentDate: String, // yyyy-MM-dd HH:mm:ss
    val sentBy: String,
    val customerId: Int,
    val customerName: String,
    val message: String
)

@Entity(tableName = "message_templates")
data class MessageTemplate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val language: String, // "English" or "Gujarati"
    val templateText: String
)

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val staffName: String,
    val actionType: String, // "Create", "Update", "Payment", "Followup", "Delete"
    val description: String
)
