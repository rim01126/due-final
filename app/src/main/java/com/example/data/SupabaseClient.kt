package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// ==========================================
// SUPABASE API DTO MODELS
// ==========================================

@JsonClass(generateAdapter = true)
data class CustomerDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "customer_name") val customerName: String,
    @Json(name = "mobile_number") val mobileNumber: String,
    @Json(name = "alternate_mobile_number") val alternateMobileNumber: String? = null,
    @Json(name = "address") val address: String? = null,
    @Json(name = "city_village") val cityVillage: String? = null,
    @Json(name = "product_purchased") val productPurchased: String? = null,
    @Json(name = "purchase_date") val purchaseDate: String,
    @Json(name = "total_bill_amount") val totalBillAmount: Double,
    @Json(name = "pending_amount") val pendingAmount: Double,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "status") val status: String,
    @Json(name = "invoice_number") val invoiceNumber: String? = null,
    @Json(name = "model_detail") val modelDetail: String? = null
)

@JsonClass(generateAdapter = true)
data class DueDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "customer_id") val customerId: Int,
    @Json(name = "customer_name") val customerName: String,
    @Json(name = "due_amount") val dueAmount: Double,
    @Json(name = "due_date") val dueDate: String,
    @Json(name = "reminder_date") val reminderDate: String? = null,
    @Json(name = "due_status") val dueStatus: String,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "invoice_number") val invoiceNumber: String? = null
)

@JsonClass(generateAdapter = true)
data class PaymentEntryDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "customer_id") val customerId: Int,
    @Json(name = "customer_name") val customerName: String,
    @Json(name = "due_id") val dueId: Int? = null,
    @Json(name = "amount_paid") val amountPaid: Double,
    @Json(name = "payment_date") val paymentDate: String,
    @Json(name = "payment_mode") val paymentMode: String,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "collected_by") val collectedBy: String
)

@JsonClass(generateAdapter = true)
data class PaymentFollowupDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "customer_id") val customerId: Int,
    @Json(name = "customer_name") val customerName: String,
    @Json(name = "follow_up_date") val followUpDate: String,
    @Json(name = "notes") val notes: String,
    @Json(name = "next_follow_up_date") val nextFollowUpDate: String? = null,
    @Json(name = "promise_to_pay_date") val promiseToPayDate: String? = null,
    @Json(name = "staff_name") val staffName: String,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class ReferralPersonDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "mobile_number") val mobileNumber: String,
    @Json(name = "address") val address: String? = null,
    @Json(name = "city") val city: String? = null,
    @Json(name = "notes") val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class CustomerReferralDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "customer_id") val customerId: Int,
    @Json(name = "customer_name") val customerName: String,
    @Json(name = "referred_by_type") val referredByType: String,
    @Json(name = "referrer_name") val referrerName: String,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class StaffMemberDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "name") val name: String,
    @Json(name = "mobile") val mobile: String,
    @Json(name = "role") val role: String,
    @Json(name = "is_active") val isActive: Boolean
)

@JsonClass(generateAdapter = true)
data class WhatsAppReminderLogDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "sent_date") val sentDate: String,
    @Json(name = "sent_by") val sentBy: String,
    @Json(name = "customer_id") val customerId: Int? = null,
    @Json(name = "customer_name") val customerName: String,
    @Json(name = "message") val message: String
)

@JsonClass(generateAdapter = true)
data class MessageTemplateDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "name") val name: String,
    @Json(name = "language") val language: String,
    @Json(name = "template_text") val templateText: String
)

@JsonClass(generateAdapter = true)
data class ActivityLogDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "staff_name") val staffName: String,
    @Json(name = "action_type") val actionType: String,
    @Json(name = "description") val description: String
)

// ==========================================
// RETROFIT API SERVICE FOR SUPABASE REST
// ==========================================

interface SupabaseRestService {

    @GET("customers")
    suspend fun getCustomers(): List<CustomerDto>

    @POST("customers")
    suspend fun upsertCustomer(
        @Header("Prefer") prefer: String = "return=representation,resolution=merge-duplicates",
        @Query("on_conflict") onConflict: String = "id",
        @Body body: List<CustomerDto>
    ): List<CustomerDto>

    @DELETE("customers")
    suspend fun deleteCustomer(@Query("id") filter: String)

    @GET("dues")
    suspend fun getDues(): List<DueDto>

    @POST("dues")
    suspend fun upsertDue(
        @Header("Prefer") prefer: String = "return=representation,resolution=merge-duplicates",
        @Query("on_conflict") onConflict: String = "id",
        @Body body: List<DueDto>
    ): List<DueDto>

    @DELETE("dues")
    suspend fun deleteDue(@Query("id") filter: String)

    @GET("payment_entries")
    suspend fun getPaymentEntries(): List<PaymentEntryDto>

    @POST("payment_entries")
    suspend fun upsertPaymentEntry(
        @Header("Prefer") prefer: String = "return=representation,resolution=merge-duplicates",
        @Query("on_conflict") onConflict: String = "id",
        @Body body: List<PaymentEntryDto>
    ): List<PaymentEntryDto>

    @DELETE("payment_entries")
    suspend fun deletePaymentEntry(@Query("id") filter: String)

    @GET("payment_followups")
    suspend fun getPaymentFollowups(): List<PaymentFollowupDto>

    @POST("payment_followups")
    suspend fun upsertPaymentFollowup(
        @Header("Prefer") prefer: String = "return=representation,resolution=merge-duplicates",
        @Query("on_conflict") onConflict: String = "id",
        @Body body: List<PaymentFollowupDto>
    ): List<PaymentFollowupDto>

    @DELETE("payment_followups")
    suspend fun deletePaymentFollowup(@Query("id") filter: String)

    @GET("referral_persons")
    suspend fun getReferralPersons(): List<ReferralPersonDto>

    @POST("referral_persons")
    suspend fun upsertReferralPerson(
        @Header("Prefer") prefer: String = "return=representation,resolution=merge-duplicates",
        @Query("on_conflict") onConflict: String = "id",
        @Body body: List<ReferralPersonDto>
    ): List<ReferralPersonDto>

    @DELETE("referral_persons")
    suspend fun deleteReferralPerson(@Query("id") filter: String)

    @GET("customer_referrals")
    suspend fun getCustomerReferrals(): List<CustomerReferralDto>

    @POST("customer_referrals")
    suspend fun upsertCustomerReferral(
        @Header("Prefer") prefer: String = "return=representation,resolution=merge-duplicates",
        @Query("on_conflict") onConflict: String = "id",
        @Body body: List<CustomerReferralDto>
    ): List<CustomerReferralDto>

    @GET("staff_members")
    suspend fun getStaffMembers(): List<StaffMemberDto>

    @POST("staff_members")
    suspend fun upsertStaffMember(
        @Header("Prefer") prefer: String = "return=representation,resolution=merge-duplicates",
        @Query("on_conflict") onConflict: String = "id",
        @Body body: List<StaffMemberDto>
    ): List<StaffMemberDto>

    @DELETE("staff_members")
    suspend fun deleteStaffMember(@Query("id") filter: String)

    @GET("whatsapp_reminder_logs")
    suspend fun getWhatsAppReminderLogs(): List<WhatsAppReminderLogDto>

    @POST("whatsapp_reminder_logs")
    suspend fun upsertWhatsAppReminderLog(
        @Header("Prefer") prefer: String = "return=representation,resolution=merge-duplicates",
        @Query("on_conflict") onConflict: String = "id",
        @Body body: List<WhatsAppReminderLogDto>
    ): List<WhatsAppReminderLogDto>

    @GET("message_templates")
    suspend fun getMessageTemplates(): List<MessageTemplateDto>

    @POST("message_templates")
    suspend fun upsertMessageTemplate(
        @Header("Prefer") prefer: String = "return=representation,resolution=merge-duplicates",
        @Query("on_conflict") onConflict: String = "id",
        @Body body: List<MessageTemplateDto>
    ): List<MessageTemplateDto>

    @GET("activity_logs")
    suspend fun getActivityLogs(): List<ActivityLogDto>

    @POST("activity_logs")
    suspend fun upsertActivityLog(
        @Header("Prefer") prefer: String = "return=representation,resolution=merge-duplicates",
        @Query("on_conflict") onConflict: String = "id",
        @Body body: List<ActivityLogDto>
    ): List<ActivityLogDto>
}

// ==========================================
// SUPABASE CLIENT INITIALIZATION & WRAPPER
// ==========================================

object SupabaseClient {
    private const val TAG = "SupabaseClient"

    // Fetch parameters from BuildConfig safely
    val supabaseUrl: String
        get() = try {
            val rawUrl = BuildConfig.SUPABASE_URL
            if (rawUrl.isNullOrEmpty()) {
                "https://your-project-id.supabase.co"
            } else {
                rawUrl.trim().removeSuffix("/")
            }
        } catch (e: Throwable) {
            "https://your-project-id.supabase.co"
        }

    val supabaseAnonKey: String
        get() = try {
            val rawKey = BuildConfig.SUPABASE_ANON_KEY
            if (rawKey.isNullOrEmpty()) {
                "your-supabase-public-anon-key"
            } else {
                rawKey.trim()
            }
        } catch (e: Throwable) {
            "your-supabase-public-anon-key"
        }

    val isConfigured: Boolean
        get() = supabaseUrl.isNotEmpty() && 
                !supabaseUrl.contains("your-project-id") && 
                supabaseAnonKey.isNotEmpty() && 
                !supabaseAnonKey.contains("your-supabase-public-anon")

    val service: SupabaseRestService? by lazy {
        if (!isConfigured) {
            Log.w(TAG, "Supabase credentials are placeholders. REST client disabled.")
            null
        } else {
            try {
                val loggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }

                val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor { chain ->
                        val original = chain.request()
                        val request = original.newBuilder()
                            .header("apikey", supabaseAnonKey)
                            .header("Authorization", "Bearer $supabaseAnonKey")
                            .header("Content-Type", "application/json")
                            .header("Accept", "application/json")
                            .method(original.method, original.body)
                            .build()
                        chain.proceed(request)
                    }
                    .build()

                val moshi = Moshi.Builder()
                    .add(AlwaysSerializeNullsFactory())
                    .add(KotlinJsonAdapterFactory())
                    .build()

                val urlWithSlash = if (supabaseUrl.endsWith("/")) supabaseUrl else "$supabaseUrl/"
                val restUrl = "${urlWithSlash}rest/v1/"

                Retrofit.Builder()
                    .baseUrl(restUrl)
                    .client(okHttpClient)
                    .addConverterFactory(NullOnEmptyConverterFactory())
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .build()
                    .create(SupabaseRestService::class.java)
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing Retrofit client: ${e.message}", e)
                null
            }
        }
    }
}

class NullOnEmptyConverterFactory : retrofit2.Converter.Factory() {
    override fun responseBodyConverter(
        type: java.lang.reflect.Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): retrofit2.Converter<okhttp3.ResponseBody, *>? {
        val delegate = retrofit.nextResponseBodyConverter<Any>(this, type, annotations)
        return retrofit2.Converter<okhttp3.ResponseBody, Any?> { body ->
            val isList = try {
                val raw = getRawType(type)
                List::class.java.isAssignableFrom(raw)
            } catch (e: Throwable) {
                type.toString().contains("java.util.List") || type.toString().contains("kotlin.collections.List")
            }

            try {
                if (body.contentLength() == 0L) {
                    return@Converter if (isList) emptyList<Any>() else null
                }
                
                // Let's check if the body is exhausted without consuming it
                val source = body.source()
                source.request(1) // Buffer at least 1 byte
                if (source.buffer.size == 0L) {
                    return@Converter if (isList) emptyList<Any>() else null
                }

                delegate.convert(body)
            } catch (e: java.io.EOFException) {
                if (isList) emptyList<Any>() else null
            }
        }
    }
}

class AlwaysSerializeNullsFactory : JsonAdapter.Factory {
    override fun create(
        type: java.lang.reflect.Type,
        annotations: Set<Annotation>,
        moshi: Moshi
    ): JsonAdapter<*>? {
        val delegate = moshi.nextAdapter<Any>(this, type, annotations)
        return delegate.serializeNulls()
    }
}

// ==========================================
// MAPPER EXTENSION FUNCTIONS
// ==========================================

fun Customer.toDto() = CustomerDto(
    id = if (id == 0) null else id,
    customerName = customerName,
    mobileNumber = mobileNumber,
    alternateMobileNumber = alternateMobileNumber.takeIf { it.isNotEmpty() },
    address = address.takeIf { it.isNotEmpty() },
    cityVillage = cityVillage.takeIf { it.isNotEmpty() },
    productPurchased = productPurchased.takeIf { it.isNotEmpty() },
    purchaseDate = purchaseDate,
    totalBillAmount = totalBillAmount,
    pendingAmount = pendingAmount,
    notes = notes.takeIf { it.isNotEmpty() },
    status = status,
    invoiceNumber = invoiceNumber.takeIf { it.isNotEmpty() },
    modelDetail = modelDetail.takeIf { it.isNotEmpty() }
)

fun CustomerDto.toEntity() = Customer(
    id = id ?: 0,
    customerName = customerName,
    mobileNumber = mobileNumber,
    alternateMobileNumber = alternateMobileNumber ?: "",
    address = address ?: "",
    cityVillage = cityVillage ?: "",
    productPurchased = productPurchased ?: "",
    purchaseDate = purchaseDate,
    totalBillAmount = totalBillAmount,
    pendingAmount = pendingAmount,
    notes = notes ?: "",
    status = status,
    invoiceNumber = invoiceNumber ?: "",
    modelDetail = modelDetail ?: ""
)

fun Due.toDto() = DueDto(
    id = if (id == 0) null else id,
    customerId = customerId,
    customerName = customerName,
    dueAmount = dueAmount,
    dueDate = dueDate,
    reminderDate = reminderDate.takeIf { it.isNotEmpty() },
    dueStatus = dueStatus,
    notes = notes.takeIf { it.isNotEmpty() },
    invoiceNumber = invoiceNumber.takeIf { it.isNotEmpty() }
)

fun DueDto.toEntity() = Due(
    id = id ?: 0,
    customerId = customerId,
    customerName = customerName,
    dueAmount = dueAmount,
    dueDate = dueDate,
    reminderDate = reminderDate ?: "",
    dueStatus = dueStatus,
    notes = notes ?: "",
    invoiceNumber = invoiceNumber ?: ""
)

fun PaymentEntry.toDto() = PaymentEntryDto(
    id = if (id == 0) null else id,
    customerId = customerId,
    customerName = customerName,
    dueId = if (dueId == 0) null else dueId,
    amountPaid = amountPaid,
    paymentDate = paymentDate,
    paymentMode = paymentMode,
    notes = notes.takeIf { it.isNotEmpty() },
    collectedBy = collectedBy
)

fun PaymentEntryDto.toEntity() = PaymentEntry(
    id = id ?: 0,
    customerId = customerId,
    customerName = customerName,
    dueId = dueId ?: 0,
    amountPaid = amountPaid,
    paymentDate = paymentDate,
    paymentMode = paymentMode,
    notes = notes ?: "",
    collectedBy = collectedBy
)

fun PaymentFollowup.toDto() = PaymentFollowupDto(
    id = if (id == 0) null else id,
    customerId = customerId,
    customerName = customerName,
    followUpDate = followUpDate,
    notes = notes,
    nextFollowUpDate = nextFollowUpDate.takeIf { it.isNotEmpty() },
    promiseToPayDate = promiseToPayDate.takeIf { it.isNotEmpty() },
    staffName = staffName,
    status = status
)

fun PaymentFollowupDto.toEntity() = PaymentFollowup(
    id = id ?: 0,
    customerId = customerId,
    customerName = customerName,
    followUpDate = followUpDate,
    notes = notes,
    nextFollowUpDate = nextFollowUpDate ?: "",
    promiseToPayDate = promiseToPayDate ?: "",
    staffName = staffName,
    status = status
)

fun ReferralPerson.toDto() = ReferralPersonDto(
    id = if (id == 0) null else id,
    fullName = fullName,
    mobileNumber = mobileNumber,
    address = address.takeIf { it.isNotEmpty() },
    city = city.takeIf { it.isNotEmpty() },
    notes = notes.takeIf { it.isNotEmpty() }
)

fun ReferralPersonDto.toEntity() = ReferralPerson(
    id = id ?: 0,
    fullName = fullName,
    mobileNumber = mobileNumber,
    address = address ?: "",
    city = city ?: "",
    notes = notes ?: ""
)

fun CustomerReferral.toDto() = CustomerReferralDto(
    id = if (id == 0) null else id,
    customerId = customerId,
    customerName = customerName,
    referredByType = referredByType,
    referrerName = referrerName,
    status = status
)

fun CustomerReferralDto.toEntity() = CustomerReferral(
    id = id ?: 0,
    customerId = customerId,
    customerName = customerName,
    referredByType = referredByType,
    referrerName = referrerName,
    status = status
)

fun StaffMember.toDto() = StaffMemberDto(
    id = if (id == 0) null else id,
    name = name,
    mobile = mobile,
    role = role,
    isActive = isActive
)

fun StaffMemberDto.toEntity() = StaffMember(
    id = id ?: 0,
    name = name,
    mobile = mobile,
    role = role,
    isActive = isActive
)

fun WhatsAppReminderLog.toDto() = WhatsAppReminderLogDto(
    id = if (id == 0) null else id,
    sentDate = sentDate,
    sentBy = sentBy,
    customerId = if (customerId == 0) null else customerId,
    customerName = customerName,
    message = message
)

fun WhatsAppReminderLogDto.toEntity() = WhatsAppReminderLog(
    id = id ?: 0,
    sentDate = sentDate,
    sentBy = sentBy,
    customerId = customerId ?: 0,
    customerName = customerName,
    message = message
)

fun MessageTemplate.toDto() = MessageTemplateDto(
    id = if (id == 0) null else id,
    name = name,
    language = language,
    templateText = templateText
)

fun MessageTemplateDto.toEntity() = MessageTemplate(
    id = id ?: 0,
    name = name,
    language = language,
    templateText = templateText
)

fun ActivityLog.toDto() = ActivityLogDto(
    id = if (id == 0) null else id,
    staffName = staffName,
    actionType = actionType,
    description = description
)

fun ActivityLogDto.toEntity() = ActivityLog(
    id = id ?: 0,
    staffName = staffName,
    actionType = actionType,
    description = description
)

