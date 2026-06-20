// PHONE WORLD CRM - MULTILINGUAL TRANSLATION ENGINE
// Technology: React Context & Hook for Bilingual Localization (English & Gujarati)

import React, { createContext, useContext, useState, useEffect } from 'react';

// 1. Translation Dictionaries for English (EN) and Gujarati (GU)
export const translations = {
  EN: {
    brand: "Phone World CRM",
    tagline: "High-Density Collection Ledger",
    subtitle: "HIGH-DENSITY VISUAL HUB",
    ownerMode: "Owner Mode",
    staffMode: "Staff Mode",
    dashboard: "Dashboard",
    totalCustomers: "Total Customers",
    pendingAmount: "Total Pending Amount",
    collectedAmount: "Total Collected Amount",
    criticalCustomers: "Critical Customers",
    criticalZone: "Critical Recovery Alerts",
    criticalZoneDesc: "Warning: High-risk client ledger files need immediate contact.",
    recentPayments: "Settlement Register",
    staffPerformance: "Staff Performance",
    addCustomer: "Add Customer",
    searchHint: "Search accounts, mobile, locations...",
    customerName: "Customer Name",
    mobile: "Mobile Number",
    altMobile: "Alt Mobile",
    pendingVal: "Pending Amount",
    status: "Status",
    actions: "Actions",
    collectPayments: "Collect",
    deleteRecord: "Delete Account",
    save: "Save Details",
    cancel: "Cancel",
    ownerAlert: "Owner Permissions Active",
    fastEntryDesk: "Fast Data Entry Desk",
    fastEntryLabel: "Quick Log Customer",
    systemActivities: "System Activity Streams",
    performanceMeter: "Field Performance Meter",
    recoveryRatio: "Recovery Success Ratio",
    collectPaymentTitle: "Collect Recovery Funds",
    enterReceivedAmount: "Enter Amount Received (₹)",
    paymentModeLabel: "Collected Payment Mode",
    selectCustomer: "Select Customer",
    activeFieldBook: "Active field recovery book",
    dueOver60Days: "Dues older than 60+ days",
    settlementRegistry: "Settlement log registry total",
    registeredCrm: "Registered CRM books",
    customerCreated: "Customer created successfully!",
    paymentCollected: "Payment collected successfully!",
    allCaughtUp: "All caught up for today!",
    filterCritical: "Filter Critical",
    last7Days: "Last 7 Days"
  },
  GU: {
    brand: "ફોન વર્લ્ડ સીઆરએમ",
    tagline: "હાઈ-ડેન્સિટી કલેક્શન લેજર",
    subtitle: "હાઇ-ડેન્સિટી વિઝ્યુઅલ હબ",
    ownerMode: "ઓનર પરવાનગી",
    staffMode: "સ્ટાફ પરવાનગી",
    dashboard: "ડેશબોર્ડ",
    totalCustomers: "કુલ ગ્રાહકો",
    pendingAmount: "કુલ બાકી રકમ",
    collectedAmount: "કુલ વસૂલાત રકમ",
    criticalCustomers: "પડકારજનક ખાતાઓ",
    criticalZone: "ખૂબ જ જોખમી ગ્રાકકો બાકી છે",
    criticalZoneDesc: "ચેતવણી: ચૂકવણીનો સમય ૬૦ દિવસ વટાવી ગયો છે. તાત્કાલિક એક્શન લો.",
    recentPayments: "કલેક્શન રજિસ્ટર",
    staffPerformance: "સ્ટાફ કામગીરી",
    addCustomer: "ગ્રાહક ઉમેરો",
    searchHint: "ગ્રાહક શોધ, નામ, ફોન, ગામ...",
    customerName: "ગ્રાકનું નામ",
    mobile: "મોબાઇલ નંબર",
    altMobile: "વૈકલ્પિક નંબર",
    pendingVal: "બાકી રકમ",
    status: "સ્થિતિ",
    actions: "કાર્યો",
    collectPayments: "પેમેન્ટ મેળવો",
    deleteRecord: "કાઢી નાખો",
    save: "સેવ કરો",
    cancel: "રદ કરો",
    ownerAlert: "ઓનર પરવાનગી સક્રિય",
    fastEntryDesk: "ફાસ્ટ ડેટા એન્ટ્રી ડેસ્ક",
    fastEntryLabel: "ઝડપી ગ્રાહક લોગ",
    systemActivities: "સિસ્ટમ પ્રવૃત્તિ પ્રવાહ",
    performanceMeter: "કુલ કલેક્શન કામગીરી",
    recoveryRatio: "પુનઃપ્રાપ્તિ સફળતા ગુણોત્તર",
    collectPaymentTitle: "વસૂલાત ભંડોળ મેળવો",
    enterReceivedAmount: "પ્રાપ્ત રકમ દાખલ કરો (₹)",
    paymentModeLabel: "ચુકવણી પદ્ધતિ પસંદ કરો",
    selectCustomer: "ગ્રાહક પસંદ કરો",
    activeFieldBook: "સક્રિય ક્ષેત્ર પુનઃપ્રાપ્તિ બુક",
    dueOver60Days: "૬૦+ દિવસથી વધુ સમય બાકી",
    settlementRegistry: "કુલ ટ્રાન્ઝેક્શન સેટલમેન્ટ",
    registeredCrm: "રજિસ્ટર્ડ સીએમઆર ગ્રાહકો",
    customerCreated: "નવો ગ્રાહક સફળતાપૂર્વક ઉમેરાયો છે!",
    paymentCollected: "પેમેન્ટ સફળતાપૂર્વક મેળવી લેવાયું છે!",
    allCaughtUp: "આજના બધા કાર્યો પૂર્ણ છે!",
    filterCritical: "ક્રિટિકલ ફિલ્ટર કરો",
    last7Days: "છેલ્લા ૭ દિવસ"
  }
};

// 2. Create the Language Context
const LanguageContext = createContext({
  language: 'EN',
  setLanguage: () => {},
  t: (key) => key,
  toggleLanguage: () => {}
});

// 3. Language Provider Component to Wrap the App
export function LanguageProvider({ children }) {
  const [language, setLanguage] = useState(() => {
    // Try to restore previous choice from local storage if available
    try {
      const saved = localStorage.getItem('phone_world_crm_lang');
      return saved === 'GU' ? 'GU' : 'EN';
    } catch {
      return 'EN';
    }
  });

  useEffect(() => {
    try {
      localStorage.setItem('phone_world_crm_lang', language);
    } catch (e) {
      // Ignore security block exceptions in sandboxes
    }
  }, [language]);

  // Translate helper function
  const t = (key) => {
    const dict = translations[language] || translations.EN;
    return dict[key] || translations.EN[key] || key;
  };

  // Toggle Language Helper
  const toggleLanguage = () => {
    setLanguage((prev) => (prev === 'EN' ? 'GU' : 'EN'));
  };

  return (
    <LanguageContext.Provider value={{ language, setLanguage, t, toggleLanguage }}>
      {children}
    </LanguageContext.Provider>
  );
}

// 4. Custom hook for consuming language configurations
export function useLanguage() {
  const context = useContext(LanguageContext);
  if (!context) {
    throw new Error('useLanguage must be used within a LanguageProvider');
  }
  return context;
}
