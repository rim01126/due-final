package com.example.data

import android.util.Log

/**
 * Utility functions for interacting directly with Supabase via Kotlin Co-routines/Retrofit
 * to perform robust CRUD operations on follow-ups and payments data.
 */
object SupabaseCrudUtils {
    private const val TAG = "SupabaseCrudUtils"

    /**
     * Checks if the Supabase credentials are properly configured.
     * Useful for safely guarding network calls.
     */
    fun isConfigured(): Boolean {
        val configured = SupabaseClient.isConfigured
        Log.d(TAG, "Checking Supabase configuration status: $configured")
        return configured
    }

    // =========================================================================
    // CRUD OPERATIONS FOR PAYMENT ENTRIES
    // =========================================================================

    /**
     * READ: Fetches all payment entries currently stored in Supabase.
     * Maps the returned list of Dto models back to standard Entity models.
     */
    suspend fun fetchPaymentEntries(): List<PaymentEntry> {
        val service = SupabaseClient.service
        if (service == null) {
            Log.e(TAG, "fetchPaymentEntries: Supabase REST service is not initialized.")
            return emptyList()
        }
        return try {
            Log.d(TAG, "Initiating network fetch for all payment entries...")
            val dtos = service.getPaymentEntries()
            Log.d(TAG, "Fetched ${dtos.size} payment entry DTOs from Supabase.")
            dtos.map { it.toEntity() }
        } catch (e: Exception) {
            Log.e(TAG, "Exception encountered in fetchPaymentEntries: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * CREATE (Upsert): Saves or inserts a payment entry into Supabase.
     * Converts the entity to a Dto before posting and maps the response back.
     */
    suspend fun createPaymentEntry(payment: PaymentEntry): PaymentEntry? {
        val service = SupabaseClient.service
        if (service == null) {
            Log.e(TAG, "createPaymentEntry: Supabase REST service is not initialized.")
            return null
        }
        return try {
            val dto = payment.toDto()
            Log.d(TAG, "[CREATE/UPSERT] Sending payment entry DTO: $dto")
            val results = service.upsertPaymentEntry(body = listOf(dto))
            if (results.isNotEmpty()) {
                val savedEntity = results.first().toEntity()
                Log.d(TAG, "[CREATE/UPSERT] Successfully saved payment entry with ID: ${savedEntity.id}")
                savedEntity
            } else {
                Log.w(TAG, "[CREATE/UPSERT] Upsert response returned an empty list.")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception encountered in createPaymentEntry: ${e.message}", e)
            null
        }
    }

    /**
     * UPDATE: Updates an existing payment entry on Supabase.
     * Leverages the upsert logic to overwrite records matching the primary id.
     */
    suspend fun updatePaymentEntry(payment: PaymentEntry): PaymentEntry? {
        if (payment.id == 0) {
            Log.e(TAG, "updatePaymentEntry: Aborted update. Target payment entry ID must not be 0.")
            return null
        }
        Log.d(TAG, "Forwarding update request for payment ID ${payment.id}...")
        return createPaymentEntry(payment)
    }

    /**
     * DELETE: Deletes a payment entry from Supabase.
     * Targets the unique record where id equals the parsed parameter.
     */
    suspend fun deletePaymentEntry(paymentId: Int): Boolean {
        val service = SupabaseClient.service
        if (service == null) {
            Log.e(TAG, "deletePaymentEntry: Supabase REST service is not initialized.")
            return false
        }
        if (paymentId == 0) {
            Log.e(TAG, "deletePaymentEntry: Aborted deletion. Target payment ID must not be 0.")
            return false
        }
        return try {
            Log.d(TAG, "Requesting hard-deletion for payment ID $paymentId from Supabase...")
            service.deletePaymentEntry(filter = "id=eq.$paymentId")
            Log.d(TAG, "Successfully requested deletion of payment entry ID $paymentId.")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Exception encountered in deletePaymentEntry: ${e.message}", e)
            false
        }
    }

    // =========================================================================
    // CRUD OPERATIONS FOR PAYMENT FOLLOWUPS
    // =========================================================================

    /**
     * READ: Fetches all payment follow-ups from Supabase.
     * Maps the response sequence of Dto models back into database Entity models.
     */
    suspend fun fetchPaymentFollowups(): List<PaymentFollowup> {
        val service = SupabaseClient.service
        if (service == null) {
            Log.e(TAG, "fetchPaymentFollowups: Supabase REST service is not initialized.")
            return emptyList()
        }
        return try {
            Log.d(TAG, "Initiating network fetch for all payment follow-ups...")
            val dtos = service.getPaymentFollowups()
            Log.d(TAG, "Fetched ${dtos.size} payment follow-up DTOs from Supabase.")
            dtos.map { it.toEntity() }
        } catch (e: Exception) {
            Log.e(TAG, "Exception encountered in fetchPaymentFollowups: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * CREATE (Upsert): Saves or inserts a payment follow-up into Supabase.
     * Converts the entity to a Dto before posting and maps the response back.
     */
    suspend fun createPaymentFollowup(followup: PaymentFollowup): PaymentFollowup? {
        val service = SupabaseClient.service
        if (service == null) {
            Log.e(TAG, "createPaymentFollowup: Supabase REST service is not initialized.")
            return null
        }
        return try {
            val dto = followup.toDto()
            Log.d(TAG, "[CREATE/UPSERT] Sending payment followup DTO: $dto")
            val results = service.upsertPaymentFollowup(body = listOf(dto))
            if (results.isNotEmpty()) {
                val savedEntity = results.first().toEntity()
                Log.d(TAG, "[CREATE/UPSERT] Successfully saved payment followup with ID: ${savedEntity.id}")
                savedEntity
            } else {
                Log.w(TAG, "[CREATE/UPSERT] Upsert response returned an empty list.")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception encountered in createPaymentFollowup: ${e.message}", e)
            null
        }
    }

    /**
     * UPDATE: Updates an existing payment follow-up on Supabase.
     * Overwrites status, notes, or next follow-up dates matching the primary id.
     */
    suspend fun updatePaymentFollowup(followup: PaymentFollowup): PaymentFollowup? {
        if (followup.id == 0) {
            Log.e(TAG, "updatePaymentFollowup: Aborted update. Target follow-up ID must not be 0.")
            return null
        }
        Log.d(TAG, "Forwarding update request for follow-up ID ${followup.id}...")
        return createPaymentFollowup(followup)
    }

    /**
     * DELETE: Deletes a payment follow-up from Supabase.
     * Targets the unique records where id equals the parsed parameter.
     */
    suspend fun deletePaymentFollowup(followupId: Int): Boolean {
        val service = SupabaseClient.service
        if (service == null) {
            Log.e(TAG, "deletePaymentFollowup: Supabase REST service is not initialized.")
            return false
        }
        if (followupId == 0) {
            Log.e(TAG, "deletePaymentFollowup: Aborted deletion. Target follow-up ID must not be 0.")
            return false
        }
        return try {
            Log.d(TAG, "Requesting hard-deletion for follow-up ID $followupId from Supabase...")
            service.deletePaymentFollowup(filter = "id=eq.$followupId")
            Log.d(TAG, "Successfully requested deletion of payment follow-up ID $followupId.")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Exception encountered in deletePaymentFollowup: ${e.message}", e)
            false
        }
    }
}
