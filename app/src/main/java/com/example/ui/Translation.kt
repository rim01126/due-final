package com.example.ui

import androidx.compose.runtime.staticCompositionLocalOf

enum class Lang { EN, GU }

data class LanguageContextState(
    val lang: Lang,
    val switchLanguage: (Lang) -> Unit
)

val LocalLanguageContext = staticCompositionLocalOf<LanguageContextState> {
    LanguageContextState(Lang.EN) {}
}

object AppStrings {
    fun appTitle(lang: Lang) = if (lang == Lang.EN) "Phone World CRM" else "ફોન વર્લ્ડ સીઆરએમ"
    fun dashboard(lang: Lang) = if (lang == Lang.EN) "Dashboard" else "ડેશબોર્ડ"
    fun customerModule(lang: Lang) = if (lang == Lang.EN) "Customers" else "ગ્રાહકો"
    fun dueManagement(lang: Lang) = if (lang == Lang.EN) "Dues & Dues Ledger" else "બાકી રકમ વ્યવસ્થાપન"
    fun collections(lang: Lang) = if (lang == Lang.EN) "Collections" else "ચુકવણીઓ"
    fun followUps(lang: Lang) = if (lang == Lang.EN) "Follow-Ups" else "ફોલો-અપ્સ"
    fun referrals(lang: Lang) = if (lang == Lang.EN) "Referrals" else "રેફરલ્સ"
    fun staffManagement(lang: Lang) = if (lang == Lang.EN) "Staff Performance" else "સ્ટાફ મેનેજમેન્ટ"
    fun criticalZone(lang: Lang) = if (lang == Lang.EN) "Critical Zone" else "ક્રિટિકલ ઝોન"
    fun reports(lang: Lang) = if (lang == Lang.EN) "Reports Ledger" else "અહેવાલો"
    
    // Stats Dashboard Cards
    fun totalCustomers(lang: Lang) = if (lang == Lang.EN) "Total Customers" else "કુલ ગ્રાહકો"
    fun activeCustomers(lang: Lang) = if (lang == Lang.EN) "Active Customers" else "એક્ટિવ ગ્રાહકો"
    fun totalPendingAmount(lang: Lang) = if (lang == Lang.EN) "Total Pending Amount" else "કુલ બાકી રકમ"
    fun totalCollectedAmount(lang: Lang) = if (lang == Lang.EN) "Total Collected Amount" else "કુલ મેળવેલ રકમ"
    fun todayCollection(lang: Lang) = if (lang == Lang.EN) "Today Collection" else "આજનું કલેક્શન"
    fun todayFollowUps(lang: Lang) = if (lang == Lang.EN) "Today's Follow-Ups" else "આજના ફોલો-અપ્સ"
    fun tomorrowFollowUps(lang: Lang) = if (lang == Lang.EN) "Tomorrow's Follow-Ups" else "આવતીકાલના ફોલો-અપ્સ"
    fun overdueCustomers(lang: Lang) = if (lang == Lang.EN) "Overdue Customers" else "બાકી મુદત ગ્રાહકો"
    fun criticalCustomers(lang: Lang) = if (lang == Lang.EN) "Critical Customers" else "ગંભીર ગ્રાહકો"
    fun referralCustomers(lang: Lang) = if (lang == Lang.EN) "Referral Customers" else "રેફરલ ગ્રાહકો"
    fun staffPerformance(lang: Lang) = if (lang == Lang.EN) "Staff Performance" else "સ્ટાફ કલેક્શન આંકડા"
    fun recentActivities(lang: Lang) = if (lang == Lang.EN) "Recent Business Activities" else "તાજેતરની પ્રવૃત્તિઓ"

    // Customer fields
    fun customerName(lang: Lang) = if (lang == Lang.EN) "Customer Name" else "ગ્રાહકનું નામ"
    fun mobileNumber(lang: Lang) = if (lang == Lang.EN) "Mobile Number" else "મોબાઇલ નંબર"
    fun alternateMobile(lang: Lang) = if (lang == Lang.EN) "Alternate Mobile" else "વૈકલ્પિક મોબાઇલ"
    fun address(lang: Lang) = if (lang == Lang.EN) "Residential Address" else "સરનામું"
    fun cityVillage(lang: Lang) = if (lang == Lang.EN) "City / Village" else "શહેર / ગામ"
    fun productPurchased(lang: Lang) = if (lang == Lang.EN) "Product Purchased" else "ખરીદેલ ફોન / પ્રોડક્ટ"
    fun purchaseDate(lang: Lang) = if (lang == Lang.EN) "Purchase Date" else "ખરીદી તારીખ"
    fun totalBillAmount(lang: Lang) = if (lang == Lang.EN) "Total Bill Amount" else "કુલ બિલ રકમ"
    fun pendingAmount(lang: Lang) = if (lang == Lang.EN) "Pending Amount" else "બાકી રકમ"
    fun notes(lang: Lang) = if (lang == Lang.EN) "Specific Notes / Remarks" else "નોંધ / રીમાર્કસ"
    fun status(lang: Lang) = if (lang == Lang.EN) "Customer Status" else "ગ્રાહક સ્થિતિ"
    
    // Status types
    fun active(lang: Lang) = if (lang == Lang.EN) "Active" else "એક્ટિવ"
    fun paid(lang: Lang) = if (lang == Lang.EN) "Paid" else "ચૂકવેલ"
    fun pending(lang: Lang) = if (lang == Lang.EN) "Pending" else "બાકી છે"
    fun partialPaid(lang: Lang) = if (lang == Lang.EN) "Partial Paid" else "અંશતઃ ચૂકવેલ"
    fun overdue(lang: Lang) = if (lang == Lang.EN) "Overdue" else "મુદત વીતી ગયેલ"
    fun critical(lang: Lang) = if (lang == Lang.EN) "Critical" else "ખૂબ ગંભીર"

    // Buttons
    fun addCustomer(lang: Lang) = if (lang == Lang.EN) "Add Customer" else "નવો ગ્રાહક ઉમેરો"
    fun editCustomer(lang: Lang) = if (lang == Lang.EN) "Edit Customer" else "વિગત સુધારો"
    fun delete(lang: Lang) = if (lang == Lang.EN) "Delete" else "કાઢી નાખો"
    fun save(lang: Lang) = if (lang == Lang.EN) "Save Details" else "વિગતો સેવ કરો"
    fun cancel(lang: Lang) = if (lang == Lang.EN) "Cancel" else "રદ કરો"
    fun call(lang: Lang) = if (lang == Lang.EN) "Call" else "ફોન કરો"
    fun whatsApp(lang: Lang) = if (lang == Lang.EN) "WhatsApp" else "વોટ્સએપ"
    fun collectPayment(lang: Lang) = if (lang == Lang.EN) "Collect Payment" else "પેમેન્ટ સ્વીકારો"
    fun addFollowup(lang: Lang) = if (lang == Lang.EN) "Add Follow-Up" else "ફોલો-અપ ઉમેરો"
    fun addDue(lang: Lang) = if (lang == Lang.EN) "Add Due Instalment" else "નવો હપ્તો ઉમેરો"
    fun sendReminder(lang: Lang) = if (lang == Lang.EN) "Send Reminder" else "રિમાઇન્ડર મોકલો"
    fun exportReport(lang: Lang) = if (lang == Lang.EN) "Export Data" else "રિપોર્ટ એક્સપોર્ટ કરો"

    // Forms Due Management
    fun dueAmount(lang: Lang) = if (lang == Lang.EN) "Due / Instalment Amount" else "હપ્તા રકમ"
    fun dueDate(lang: Lang) = if (lang == Lang.EN) "Due Date" else "ચુકવણી તારીખ"
    fun reminderDate(lang: Lang) = if (lang == Lang.EN) "Reminder Alert Date" else "રિમાઇન્ડર તારીખ"

    // Collections
    fun amountPaid(lang: Lang) = if (lang == Lang.EN) "Amount Paid" else "ભરેલી રકમ"
    fun paymentMode(lang: Lang) = if (lang == Lang.EN) "Payment Mode" else "ચુકવણી મોડ"
    fun collectedBy(lang: Lang) = if (lang == Lang.EN) "Collected By" else "મેળવનાર સ્ટાફ"
    fun paymentDate(lang: Lang) = if (lang == Lang.EN) "Date of Payment" else "પેમેન્ટ તારીખ"

    // Follow ups
    fun followUpDate(lang: Lang) = if (lang == Lang.EN) "Follow-Up Date" else "ફોલો-અપ તારીખ"
    fun nextFollowup(lang: Lang) = if (lang == Lang.EN) "Next Follow-Up Date" else "આગામી ફોલો-અપ તારીખ"
    fun promiseToPay(lang: Lang) = if (lang == Lang.EN) "Promise To Pay Date" else "ચુકવણી આપવાની તારીખ"
    fun promiseDateEmpty(lang: Lang) = if (lang == Lang.EN) "No Promise Date" else "કોઈ વચન તારીખ નથી"

    // Referrals
    fun refersTitle(lang: Lang) = if (lang == Lang.EN) "Referral System" else "રેફરલ વ્યવસ્થાપન"
    fun addReferrer(lang: Lang) = if (lang == Lang.EN) "Add Referral Person" else "નવો રેફરર ઉમેરો"
    fun selectReferrer(lang: Lang) = if (lang == Lang.EN) "Referred By" else "કોના દ્વારા રેફરલ?"
    fun referrerName(lang: Lang) = if (lang == Lang.EN) "Referrer Person Name" else "રેફરલ વ્યક્તિનું નામ"
    fun totalSales(lang: Lang) = if (lang == Lang.EN) "Total Sales" else "કુલ સેલ્સ"
    fun rewardPoints(lang: Lang) = if (lang == Lang.EN) "Referral Rewards" else "રેફરલ ઈનામો (₹)"

    // Staff
    fun staffTitle(lang: Lang) = if (lang == Lang.EN) "Staff Directory" else "સ્ટાફ ડિરેક્ટરી"
    fun addStaff(lang: Lang) = if (lang == Lang.EN) "Add Staff Member" else "નવો સ્ટાફ ઉમેરો"
    fun staffRole(lang: Lang) = if (lang == Lang.EN) "Assigned Role" else "સ્ટાફ હોદ્દો"
    fun activeStatus(lang: Lang) = if (lang == Lang.EN) "Current Active Status" else "હાલમાં એક્ટિવ સ્થિતિ"
    
    // Auth & Workspace
    fun currentRoleMsg(lang: Lang) = if (lang == Lang.EN) "Current Role" else "વર્તમાન સ્ટાફ હોદ્દો"
    fun ownerOnlyMessage(lang: Lang) = if (lang == Lang.EN) "Access Denied: Owner Permissions Required." else "પ્રવેશ અસ્વીકાર: ઓનર મંજૂરી જરૂરી છે."
    fun switchUser(lang: Lang) = if (lang == Lang.EN) "Switch Role Profile" else "સ્ટાફ પ્રોફાઇલ બદલો"
    
    // Reminders
    fun reminderLogHeader(lang: Lang) = if (lang == Lang.EN) "Reminder Dispatch Log" else "રિમાઇન્ડર મોકલવાના લોગ"
    fun englishTemplateTitle(lang: Lang) = if (lang == Lang.EN) "English Notification Content" else "અંગ્રેજી રિમાઇન્ડર ટેમ્પલેટ"
    fun gujaratiTemplateTitle(lang: Lang) = if (lang == Lang.EN) "Gujarati Notification Content" else "ગુજરાતી રિમાઇન્ડર ટેમ્પલેટ"
}
