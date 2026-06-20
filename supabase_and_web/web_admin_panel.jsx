// PHONE WORLD CRM - WEB ADMINISTRATIVE PANEL
// Technology: React with Tailwind CSS & Lucide Icons (Optimized for High-Density Mobile-First & Desktop UI)

import React, { useState, useMemo } from 'react';
import { 
  Users, DollarSign, Clock, AlertTriangle, UserPlus, 
  Search, Shield, Globe, Sun, Moon, Phone, Send, Plus, 
  Trash2, TrendingUp, BarChart2, CheckCircle, ArrowRight,
  MessageSquare, UserCheck, Calendar, Activity, ChevronRight, Check
} from 'lucide-react';

export default function PhoneWorldWebAdmin() {
  // Application Settings State
  const [lang, setLang] = useState('EN'); // EN or GU
  const [darkTheme, setDarkTheme] = useState(true);
  const [role, setRole] = useState('Owner'); // Owner or Staff

  // Search and Filters
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('All');

  // Seed Mock Database State
  const [customers, setCustomers] = useState([
    { id: 1, name: 'Vijay Vasoya', phone: '9909912345', altPhone: '9909954321', address: 'Shakti Nagar, Rajkot', product: 'iPhone 15 Pro Max 256GB', totalBill: 145000, pending: 45000, notes: 'Promised soon', status: 'Pending', date: '2026-06-12' },
    { id: 2, name: 'Suresh Rathod', phone: '9825611223', altPhone: '', address: 'Yagnik Road, Rajkot', product: 'Samsung S24 Ultra', totalBill: 120000, pending: 30000, notes: 'Awaiting grace period', status: 'Overdue', date: '2026-06-01' },
    { id: 3, name: 'Dharmesh Gohil', phone: '9426288990', altPhone: '', address: 'Station Road, Gondal', product: 'OnePlus 12 512GB', totalBill: 65000, pending: 25000, notes: 'Critical default state. Call back.', status: 'Critical', date: '2026-04-10' }
  ]);

  const [payments, setPayments] = useState([
    { id: 101, customerName: 'Vijay Vasoya', amount: 100000, mode: 'UPI', date: '2026-06-12', collectedBy: 'Keval Patel' },
    { id: 102, customerName: 'Suresh Rathod', amount: 90000, mode: 'Cash', date: '2026-06-01', collectedBy: 'Anjali Mehta' }
  ]);

  const [followups, setFollowups] = useState([
    { id: 201, customerName: 'Suresh Rathod', notes: 'Agreed to pay by next Tuesday', nextDate: '2026-06-20', status: 'Promised', date: '2026-06-14' }
  ]);

  const [referrers, setReferrers] = useState([
    { id: 301, name: 'Harshil Shah', phone: '+91 88888 77777', city: 'Rajkot', totalReferred: 1 }
  ]);

  const [activityLogs, setActivityLogs] = useState([
    { id: 1, timestamp: '11:20 AM', type: 'Payment', user: 'Keval', desc: 'Collected ₹100,000 from Vijay Vasoya' }
  ]);

  // Dialog & Quick Form togglers
  const [showAddCustomer, setShowAddCustomer] = useState(false);
  const [showPaymentCollect, setShowPaymentCollect] = useState(false);
  const [activeCustomerToPay, setActiveCustomerToPay] = useState(null);
  
  // Quick Entry Inputs
  const [quickCustName, setQuickCustName] = useState('');
  const [quickCustPhone, setQuickCustPhone] = useState('');
  const [quickCustAmount, setQuickCustAmount] = useState('');
  const [quickCustProduct, setQuickCustProduct] = useState('');

  // Translation Dictionaries
  const text = {
    EN: {
      brand: 'Phone World CRM',
      subtitle: 'HIGH-DENSITY VISUAL HUB',
      totalCustomers: 'Total Customers',
      pendingAmount: 'Total Pending Amount',
      collectedAmount: 'Total Collected Amount',
      criticalZone: 'Critical Recovery Alerts',
      recentPayments: 'Settlement Register',
      staffPerformance: 'Staff Collector Lead',
      addCustomer: 'Add Customer',
      searchHint: 'Search accounts, mobile, locations...',
      customerName: 'Customer Name',
      mobile: 'Mobile Number',
      pendingVal: 'Pending Amount',
      status: 'Status',
      actions: 'Actions',
      collectPayments: 'Collect',
      deleteRecord: 'Delete',
      save: 'Save Details',
      cancel: 'Cancel',
      languageLabel: 'ગુજરાતી',
      ownerAlert: 'Access Approved: Owner'
    },
    GU: {
      brand: 'ફોન વર્લ્ડ સીઆરએમ',
      subtitle: 'હાઇ-ડેન્સિટી વિઝ્યુઅલ હબ',
      totalCustomers: 'કુલ ગ્રાહકો',
      pendingAmount: 'કુલ બાકી રકમ',
      collectedAmount: 'કુલ વસૂલાત રકમ',
      criticalZone: 'પડકારજનક ખાતાઓ',
      recentPayments: 'કલેક્શન રજિસ્ટર',
      staffPerformance: 'સ્ટાફ કામગીરી',
      addCustomer: 'ગ્રાહક ઉમેરો',
      searchHint: 'ગ્રાહક શોધ, નામ, ફોન...',
      customerName: 'ગ્રાકનું નામ',
      mobile: 'મોબાઇલ નંબર',
      pendingVal: 'બાકી રકમ',
      status: 'સ્થિતિ',
      actions: 'ક્રિયાઓ',
      collectPayments: 'પેમેન્ટ મેળવો',
      deleteRecord: 'કાઢી નાખો',
      save: 'સેવ કરો',
      cancel: 'રદ કરો',
      languageLabel: 'English',
      ownerAlert: 'ઓનર પરવાનગી સક્રિય'
    }
  }[lang];

  // Calculations
  const stats = useMemo(() => {
    const totalP = customers.reduce((sum, c) => sum + c.pending, 0);
    const totalC = payments.reduce((sum, p) => sum + p.amount, 0);
    const criticalC = customers.filter(c => c.status === 'Critical').length;
    return { totalP, totalC, criticalC };
  }, [customers, payments]);

  // Handle Quick Add Customer Submit
  const handleQuickAddCustomerSubmit = (e) => {
    e.preventDefault();
    if (!quickCustName || !quickCustPhone || !quickCustAmount) return;

    const newCustomer = {
      id: Date.now(),
      name: quickCustName,
      phone: quickCustPhone,
      altPhone: '',
      address: 'Rajkot Main Office',
      product: quickCustProduct || 'Universal Smart Accessory',
      totalBill: parseFloat(quickCustAmount) * 1.5,
      pending: parseFloat(quickCustAmount),
      notes: 'Logged via Fast Data Entry Desk',
      status: 'Pending',
      date: new Date().toISOString().split('T')[0]
    };

    setCustomers([newCustomer, ...customers]);
    
    // Log audit trail
    setActivityLogs([
      { id: Date.now(), timestamp: 'Now', type: 'System', user: role, desc: `Created customer account: ${quickCustName}` },
      ...activityLogs
    ]);

    // Clear forms
    setQuickCustName('');
    setQuickCustPhone('');
    setQuickCustAmount('');
    setQuickCustProduct('');
  };

  // Handle payments
  const handleCollectPaymentSubmit = (e) => {
    e.preventDefault();
    const form = e.target;
    const amount = parseFloat(form.amount.value);
    const mode = form.mode.value;

    if (!amount || amount <= 0) return;

    // Log Settlement
    const newPayment = {
      id: Date.now(),
      customerName: activeCustomerToPay.name,
      amount,
      mode,
      date: new Date().toISOString().split('T')[0],
      collectedBy: role === 'Owner' ? 'Rais Memon' : 'Keval Patel'
    };

    setPayments([newPayment, ...payments]);

    // Update Customer Amount
    setCustomers(customers.map(c => {
      if (c.id === activeCustomerToPay.id) {
        const remaining = Math.max(0, c.pending - amount);
        const nextStatus = remaining <= 0 ? 'Paid' : c.status;
        return { ...c, pending: remaining, status: nextStatus };
      }
      return c;
    }));

    // Log audit
    setActivityLogs([
      { id: Date.now(), timestamp: 'Now', type: 'Payment', user: role, desc: `Collected ₹${amount} from ${activeCustomerToPay.name}` },
      ...activityLogs
    ]);

    setShowPaymentCollect(false);
    setActiveCustomerToPay(null);
  };

  return (
    <div className={`min-h-screen font-sans ${darkTheme ? 'bg-slate-950 text-slate-100' : 'bg-slate-50 text-slate-900'} transition-colors duration-200`}>
      {/* HEADER BAR */}
      <header className={`px-4 py-2.5 flex flex-wrap justify-between items-center gap-3 border-b ${darkTheme ? 'bg-slate-900/90 border-slate-800' : 'bg-white border-slate-200'} sticky top-0 backdrop-blur z-20`}>
        <div className="flex items-center gap-2">
          <div className="bg-gradient-to-tr from-emerald-600 to-teal-500 text-white p-1.5 rounded-lg shadow-md shadow-emerald-900/10">
            <Users className="w-5 h-5" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-base font-black tracking-tight">{text.brand}</h1>
              <span className="bg-emerald-500/10 text-emerald-400 text-3xs font-extrabold px-1.5 py-0.2 rounded uppercase border border-emerald-500/20">{text.subtitle}</span>
            </div>
            <p className="text-3xs text-slate-500 font-bold">Logged in as {role} • UTC 2026</p>
          </div>
        </div>

        {/* CONTROLS */}
        <div className="flex items-center gap-2">
          {/* USER MODE TOGGLE */}
          <button 
            onClick={() => setRole(role === 'Owner' ? 'Staff' : 'Owner')}
            className={`px-2.5 py-1 rounded-md text-3xs font-black flex items-center gap-1 transition uppercase ${darkTheme ? 'bg-slate-800 hover:bg-slate-700' : 'bg-slate-200 hover:bg-slate-300'}`}
          >
            <Shield className="w-3 h-3 text-emerald-500" />
            Mode: {role}
          </button>

          {/* LANGUAGE TOGGLE */}
          <button 
            onClick={() => setLang(lang === 'EN' ? 'GU' : 'EN')}
            className="px-2.5 py-1 rounded-md bg-emerald-600/10 text-emerald-500 hover:bg-emerald-600/20 text-3xs font-black transition"
          >
            {lang === 'EN' ? 'ગુજરાતી' : 'English'}
          </button>

          {/* THEME TOGGLE */}
          <button 
            onClick={() => setDarkTheme(!darkTheme)}
            className={`p-1.5 rounded-md transition ${darkTheme ? 'bg-slate-800 text-yellow-400' : 'bg-slate-200 text-indigo-600'}`}
          >
            {darkTheme ? <Sun className="w-3.5 h-3.5" /> : <Moon className="w-3.5 h-3.5" />}
          </button>
        </div>
      </header>

      <main className="p-4 max-w-7xl mx-auto space-y-4">
        {/* HIGH-DENSITY CRITICAL WARNING ALARM STRIP */}
        {stats.criticalC > 0 && (
          <div className={`p-2.5 rounded-xl border flex flex-wrap items-center justify-between gap-2.5 ${darkTheme ? 'bg-rose-950/40 border-rose-900/60 text-rose-200' : 'bg-rose-50 border-rose-200 text-rose-900'} relative overflow-hidden`}>
            <div className="flex items-center gap-2.5">
              <div className="w-7 h-7 rounded-lg bg-rose-600 flex items-center justify-center animate-pulse shadow-md shadow-rose-900/20">
                <AlertTriangle className="w-4 h-4 text-white" />
              </div>
              <div>
                <p className="text-xs font-black uppercase tracking-wider">{text.criticalZone}</p>
                <p className="text-3xs font-bold opacity-90">
                  {lang === 'EN' 
                    ? `Warning: ${stats.criticalC} client ledger files need immediate contact. Overdue duration has exceeded local state guidelines.`
                    : `ચેતવણી: ${stats.criticalC} ગ્રાહક લોડિંગ તરત જ કોન્ટેક્ટ કરો. ચૂકવણીનો સમય ૬૦ દિવસ વટાવી ગયો છે.`}
                </p>
              </div>
            </div>
            
            <button 
              onClick={() => setStatusFilter('Critical')}
              className="bg-rose-600 text-white font-extrabold text-3xs px-2.5 py-1.5 rounded-lg tracking-wider uppercase hover:bg-rose-500 transition-colors"
            >
              Filter Critical
            </button>
          </div>
        )}

        {/* HIGH-DENSITY SUMMARY CARDS (Top of the screen view as requested) */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
          {/* CARD 1: PENDING AMOUNT */}
          <div className={`p-3 rounded-xl border relative overflow-hidden ${darkTheme ? 'bg-slate-900/80 border-slate-800' : 'bg-white border-slate-100'} shadow-sm`}>
            <div className="flex justify-between items-start">
              <span className="text-3xs text-slate-400 uppercase font-bold tracking-wider">{text.pendingAmount}</span>
              <div className="p-1 rounded bg-amber-500/10 text-amber-500">
                <Clock className="w-3.5 h-3.5" />
              </div>
            </div>
            <div className="text-lg font-black text-amber-500 mt-1">₹{stats.totalP.toLocaleString()}</div>
            <div className="text-4xs text-slate-500 mt-0.5">Active field recovery book</div>
          </div>

          {/* CARD 2: CRITICAL COUNT */}
          <div className={`p-3 rounded-xl border relative overflow-hidden ${stats.criticalC > 0 ? (darkTheme ? 'bg-rose-950/20 border-rose-900/60' : 'bg-rose-50 border-rose-100') : (darkTheme ? 'bg-slate-900/80 border-slate-800' : 'bg-white border-slate-100')} shadow-sm`}>
            <div className="flex justify-between items-start">
              <span className="text-3xs text-slate-400 uppercase font-bold tracking-wider">Critical Customers</span>
              <div className={`p-1 rounded ${stats.criticalC > 0 ? 'bg-rose-500/20 text-rose-500' : 'bg-slate-500/10 text-slate-400'}`}>
                <AlertTriangle className="w-3.5 h-3.5" />
              </div>
            </div>
            <div className={`text-lg font-black mt-1 ${stats.criticalC > 0 ? 'text-rose-500' : ''}`}>{stats.criticalC}</div>
            <div className="text-4xs text-slate-500 mt-0.5">Dues older than 60+ days</div>
          </div>

          {/* CARD 3: COLLECTED AMOUNT */}
          <div className={`p-3 rounded-xl border relative overflow-hidden ${darkTheme ? 'bg-slate-900/80 border-slate-800' : 'bg-white border-slate-100'} shadow-sm`}>
            <div className="flex justify-between items-start">
              <span className="text-3xs text-slate-400 uppercase font-bold tracking-wider">{text.collectedAmount}</span>
              <div className="p-1 rounded bg-emerald-500/10 text-emerald-500">
                <CheckCircle className="w-3.5 h-3.5" />
              </div>
            </div>
            <div className="text-lg font-black text-emerald-500 mt-1">₹{stats.totalC.toLocaleString()}</div>
            <div className="text-4xs text-slate-500 mt-0.5">Settlement log registry total</div>
          </div>

          {/* CARD 4: TOTAL ACCOUNTS */}
          <div className={`p-3 rounded-xl border relative overflow-hidden ${darkTheme ? 'bg-slate-900/80 border-slate-800' : 'bg-white border-slate-100'} shadow-sm`}>
            <div className="flex justify-between items-start">
              <span className="text-3xs text-slate-400 uppercase font-bold tracking-wider">{text.totalCustomers}</span>
              <div className="p-1 rounded bg-blue-500/10 text-blue-500">
                <Users className="w-3.5 h-3.5" />
              </div>
            </div>
            <div className="text-lg font-black mt-1">{customers.length}</div>
            <div className="text-4xs text-slate-500 mt-0.5">Registered CRM books</div>
          </div>
        </div>

        {/* TWO COLUMN CONTENT: LEFT SIDE BAR OF FAST ACTION CONTROLS / RIGHT CUSTOMER VIEW */}
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-3 items-start">
          
          {/* FAST DATA ENTRY DESK PANEL (1/3 weight) */}
          <div className="lg:col-span-4 space-y-3">
            
            {/* COMPACT REAL-TIME FAST ENTRY DESK */}
            <div className={`p-3 rounded-xl border ${darkTheme ? 'bg-slate-900/80 border-slate-800' : 'bg-white border-slate-200'} shadow-sm`}>
              <div className="flex items-center gap-1.5 mb-2 border-b border-slate-800/60 pb-1.5">
                <UserPlus className="w-4 h-4 text-emerald-500" />
                <h2 className="text-xs font-black tracking-wide uppercase">Fast Entry Desk</h2>
              </div>
              
              <form onSubmit={handleQuickAddCustomerSubmit} className="space-y-2">
                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <label className="block text-4xs text-slate-400 font-bold uppercase mb-0.5">Name</label>
                    <input 
                      type="text" 
                      placeholder="e.g. Ketan" 
                      value={quickCustName}
                      onChange={(e) => setQuickCustName(e.target.value)}
                      required
                      className={`w-full px-2 py-1.5 text-2xs rounded-lg outline-none border focus:ring-1 focus:ring-emerald-500 transition ${darkTheme ? 'bg-slate-950 border-slate-850 text-white' : 'bg-slate-50 border-slate-200'}`}
                    />
                  </div>
                  <div>
                    <label className="block text-4xs text-slate-400 font-bold uppercase mb-0.5">Phone</label>
                    <input 
                      type="tel" 
                      maxLength="10"
                      placeholder="Mobile no." 
                      value={quickCustPhone}
                      onChange={(e) => setQuickCustPhone(e.target.value)}
                      required
                      className={`w-full px-2 py-1.5 text-2xs rounded-lg outline-none border focus:ring-1 focus:ring-emerald-500 transition ${darkTheme ? 'bg-slate-950 border-slate-850 text-white' : 'bg-slate-50 border-slate-200'}`}
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <label className="block text-4xs text-slate-400 font-bold uppercase mb-0.5">Dues Pending (₹)</label>
                    <input 
                      type="number" 
                      placeholder="Dues e.g. 5000" 
                      value={quickCustAmount}
                      onChange={(e) => setQuickCustAmount(e.target.value)}
                      required
                      className={`w-full px-2 py-1.5 text-2xs rounded-lg outline-none border focus:ring-1 focus:ring-emerald-500 transition ${darkTheme ? 'bg-slate-950 border-slate-850 text-white' : 'bg-slate-50 border-slate-200'}`}
                    />
                  </div>
                  <div>
                    <label className="block text-4xs text-slate-400 font-bold uppercase mb-0.5">Product</label>
                    <input 
                      type="text" 
                      placeholder="e.g. iPhone 15" 
                      value={quickCustProduct}
                      onChange={(e) => setQuickCustProduct(e.target.value)}
                      className={`w-full px-2 py-1.5 text-2xs rounded-lg outline-none border focus:ring-1 focus:ring-emerald-500 transition ${darkTheme ? 'bg-slate-950 border-slate-850 text-white' : 'bg-slate-50 border-slate-200'}`}
                    />
                  </div>
                </div>

                <button 
                  type="submit" 
                  className="w-full mt-1.5 bg-emerald-600 hover:bg-emerald-500 text-white text-3xs font-extrabold uppercase py-2 rounded-lg transition tracking-wider flex items-center justify-center gap-1 shadow-md shadow-emerald-950/20"
                >
                  <Plus className="w-3.5 h-3.5" />
                  Quick Log Customer
                </button>
              </form>
            </div>

            {/* AUDIT & RECENT HISTORIC STREAM */}
            <div className={`p-3 rounded-xl border ${darkTheme ? 'bg-slate-900/60 border-slate-800' : 'bg-white border-slate-200'} shadow-sm`}>
              <div className="flex justify-between items-center mb-2">
                <span className="text-3xs text-slate-400 uppercase font-bold tracking-wider">System Activity Streams</span>
                <span className="bg-emerald-500/10 text-emerald-400 px-1 text-4xs font-bold rounded">Live</span>
              </div>
              <div className="space-y-2 max-h-48 overflow-y-auto pr-1">
                {activityLogs.map((log) => (
                  <div key={log.id} className="text-4xs flex items-start gap-1 pb-1.5 border-b border-slate-800/40 last:border-0 last:pb-0">
                    <span className="text-slate-500 font-bold shrink-0">[{log.timestamp}]</span>
                    <div>
                      <span className="text-emerald-400 font-bold">{log.user}:</span> {log.desc}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* TELEMETRY RATIO STATISTICS */}
            <div className={`p-3 rounded-xl border ${darkTheme ? 'bg-slate-900/60 border-slate-800' : 'bg-white border-slate-200'} shadow-sm`}>
              <div className="flex items-center gap-2 mb-2">
                <TrendingUp className="w-3.5 h-3.5 text-emerald-500" />
                <span className="text-3xs uppercase font-bold text-slate-400 tracking-wider">Field Performance Meter</span>
              </div>
              <div className="space-y-1">
                {(() => {
                  const totalRatioMoney = stats.totalP + stats.totalC;
                  const ratio = totalRatioMoney > 0 ? (stats.totalC / totalRatioMoney) * 100 : 0;
                  return (
                    <>
                      <div className="flex justify-between text-4xs font-bold">
                        <span>Recovery Success Ratio</span>
                        <span className="text-emerald-400">{ratio.toFixed(1)}%</span>
                      </div>
                      <div className="w-full h-2 bg-slate-800 rounded-full overflow-hidden">
                        <div 
                          className="h-full bg-gradient-to-r from-emerald-500 to-teal-400 rounded-full"
                          style={{ width: `${ratio}%` }}
                        ></div>
                      </div>
                      <div className="flex justify-between text-4xs text-slate-500 pt-0.5">
                        <span>Recovered: ₹{stats.totalC.toLocaleString()}</span>
                        <span>Pending: ₹{stats.totalP.toLocaleString()}</span>
                      </div>
                    </>
                  );
                })()}
              </div>
            </div>
            
          </div>

          {/* CUSTOMER LEDGER AND FILTERS PANEL (2/3 weight) */}
          <div className="lg:col-span-8 space-y-3">
            
            {/* LIVE FILTERS HUB */}
            <div className={`p-2.5 rounded-xl border flex flex-wrap items-center justify-between gap-2 ${darkTheme ? 'bg-slate-900/60 border-slate-800' : 'bg-white border-slate-200'} shadow-sm`}>
              <div className="flex items-center gap-2 w-full md:w-72 relative">
                <Search className="w-3.5 h-3.5 text-slate-400 absolute left-2.5" />
                <input 
                  type="text" 
                  placeholder={text.searchHint}
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  className={`pl-8 pr-3 py-1 text-2xs rounded-lg w-full outline-none border focus:ring-1 focus:ring-emerald-500 transition ${darkTheme ? 'bg-slate-950 border-slate-850 text-white' : 'bg-slate-50 border-slate-200'}`}
                />
              </div>

              <div className="flex items-center gap-1 flex-wrap">
                {['All', 'Pending', 'Overdue', 'Critical', 'Paid'].map(status => (
                  <button 
                    key={status} 
                    onClick={() => setStatusFilter(status)}
                    className={`px-2 py-1 rounded text-3xs font-extrabold transition ${statusFilter === status ? 'bg-emerald-600 text-white' : (darkTheme ? 'bg-slate-950 text-slate-300 hover:bg-slate-800' : 'bg-slate-100 text-slate-755 hover:bg-slate-200')}`}
                  >
                    {status}
                  </button>
                ))}
              </div>
            </div>

            {/* CUSTOMER LEDGER HIGH-DENSITY CARD-LIST / TABLE */}
            <div className={`border rounded-xl backdrop-blur overflow-hidden shadow-sm ${darkTheme ? 'bg-slate-900/40 border-slate-800' : 'bg-white border-slate-250'}`}>
              
              {/* Desktop responsive ledger representation */}
              <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse">
                  <thead>
                    <tr className={`border-b text-4xs font-black uppercase tracking-wider ${darkTheme ? 'bg-slate-900/80 border-slate-800 text-slate-400' : 'bg-slate-50 border-slate-150 text-slate-500'}`}>
                      <th className="p-2.5">{text.customerName}</th>
                      <th className="p-2.5">{text.mobile}</th>
                      <th className="p-2.5">Purchased Product</th>
                      <th className="p-2.5 text-right">{text.pendingVal}</th>
                      <th className="p-2.5 text-center">{text.status}</th>
                      <th className="p-2.5 text-right">{text.actions}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {customers
                      .filter(c => {
                        const mSearch = c.name.toLowerCase().includes(search.toLowerCase()) || 
                                        c.phone.includes(search) || 
                                        c.address.toLowerCase().includes(search.toLowerCase());
                        const mStatus = statusFilter === 'All' ? true : c.status === statusFilter;
                        return mSearch && mStatus;
                      })
                      .map((customer) => {
                        const initials = customer.name.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase();
                        return (
                          <tr key={customer.id} className={`border-b text-2xs transition hover:bg-emerald-600/5 ${darkTheme ? 'border-slate-800/50' : 'border-slate-100'}`}>
                            {/* Name & Avatar Column */}
                            <td className="p-2.5">
                              <div className="flex items-center gap-2">
                                <div className={`w-6 h-6 rounded-full flex items-center justify-center text-4xs font-black shrink-0 ${darkTheme ? 'bg-slate-800 text-emerald-400' : 'bg-slate-100 text-emerald-700'}`}>
                                  {initials}
                                </div>
                                <div>
                                  <span className="font-extrabold block text-slate-200 dark:text-slate-100">{customer.name}</span>
                                  <span className="text-4xs text-slate-500 block">{customer.address}</span>
                                </div>
                              </div>
                            </td>

                            {/* Mobile Column */}
                            <td className="p-2.5 font-medium">{customer.phone}</td>

                            {/* Product Purchased Column */}
                            <td className="p-2.5 text-slate-400 max-w-xs truncate">{customer.product}</td>

                            {/* Outstanding Balance Column */}
                            <td className="p-2.5 text-right font-black text-amber-500">
                              ₹{customer.pending.toLocaleString()}
                            </td>

                            {/* Status Badges */}
                            <td className="p-2.5 text-center">
                              <span className={`px-2 py-0.5 rounded-md text-4xs font-black tracking-wider uppercase border ${
                                customer.status === 'Paid' ? 'bg-emerald-900/30 text-emerald-400 border-emerald-500/20' : 
                                customer.status === 'Overdue' ? 'bg-red-950/40 text-red-400 border-red-500/20' : 
                                customer.status === 'Critical' ? 'bg-rose-950/60 text-rose-400 border-rose-500/40' : 
                                'bg-amber-950/30 text-amber-400 border-amber-500/20'
                              }`}>
                                {customer.status}
                              </span>
                            </td>

                            {/* Action Buttons Column */}
                            <td className="p-2.5 text-right space-x-1.5">
                              {customer.pending > 0 ? (
                                <button 
                                  onClick={() => {
                                    setActiveCustomerToPay(customer);
                                    setShowPaymentCollect(true);
                                  }}
                                  className="bg-emerald-600 hover:bg-emerald-500 text-white px-2 py-1 rounded text-4xs font-black transition uppercase inline-flex items-center gap-0.5"
                                >
                                  <DollarSign className="w-2.5 h-2.5" />
                                  Collect
                                </button>
                              ) : (
                                <span className="text-4xs text-emerald-400 font-bold uppercase inline-flex items-center gap-0.5"><Check className="w-3 h-3" /> Settled</span>
                              )}
                              
                              {role === 'Owner' && (
                                <button 
                                  onClick={() => {
                                    setCustomers(customers.filter(c => c.id !== customer.id));
                                    setActivityLogs([
                                      { id: Date.now(), timestamp: 'Now', type: 'Deletion', user: 'Owner', desc: `Deleted customer ledger file for: ${customer.name}` },
                                      ...activityLogs
                                    ]);
                                  }}
                                  className="text-rose-500 hover:text-rose-400 p-1.5 rounded-md hover:bg-rose-500/10 transition inline"
                                  title="Delete Account Ledger"
                                >
                                  <Trash2 className="w-3 h-3" />
                                </button>
                              )}
                            </td>
                          </tr>
                        );
                      })}
                  </tbody>
                </table>
              </div>
            </div>

          </div>
        </div>
      </main>

      {/* COLLECT PAYMENT MODAL DIALOG */}
      {showPaymentCollect && activeCustomerToPay && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-sm flex items-center justify-center p-4 z-50">
          <div className={`p-4 rounded-xl shadow-xl border w-full max-w-sm ${darkTheme ? 'bg-slate-900 border-slate-800' : 'bg-white border-slate-200'}`}>
            <h2 className="text-sm font-black mb-1 uppercase tracking-wide">Collect Recovery Funds</h2>
            <p className="text-5xs text-slate-400 mb-3 uppercase font-bold tracking-wider">Account name: {activeCustomerToPay.name} (Outstanding: ₹{activeCustomerToPay.pending})</p>

            <form onSubmit={handleCollectPaymentSubmit} className="space-y-3">
              <div>
                <label className="block text-5xs text-slate-400 font-black mb-0.5 uppercase tracking-wider">Enter Amount Recieved (₹)</label>
                <input 
                  type="number" 
                  name="amount" 
                  defaultValue={activeCustomerToPay.pending}
                  max={activeCustomerToPay.pending}
                  required 
                  className={`w-full p-2 text-xs rounded-lg outline-none border focus:ring-1 focus:ring-emerald-500 transition font-black ${darkTheme ? 'bg-slate-950 border-slate-850' : 'bg-slate-50 border-slate-200'}`}
                />
              </div>

              <div>
                <label className="block text-5xs text-slate-400 font-black mb-0.5 uppercase tracking-wider">Collected Payment Mode</label>
                <select name="mode" className={`w-full p-2 text-xs rounded-lg outline-none border focus:ring-1 focus:ring-emerald-500 transition font-black ${darkTheme ? 'bg-slate-950 border-slate-850' : 'bg-slate-50 border-slate-200'}`}>
                  <option value="UPI">UPI (GPay/PhonePe)</option>
                  <option value="Cash">Hard Cash (Physical)</option>
                  <option value="Card">Visa/RuPay Card</option>
                  <option value="Finance">Bajaj Store Finance</option>
                </select>
              </div>

              <div className="flex justify-end gap-2 pt-1.5">
                <button 
                  type="button" 
                  onClick={() => setShowPaymentCollect(false)}
                  className={`px-3 py-1.5 rounded-lg text-4xs font-black transition uppercase ${darkTheme ? 'bg-slate-950 hover:bg-slate-850' : 'bg-slate-100 hover:bg-slate-200'}`}
                >
                  Cancel
                </button>
                <button 
                  type="submit" 
                  className="bg-emerald-600 hover:bg-emerald-500 px-3.5 py-1.5 rounded-lg text-4xs font-black text-white transition uppercase shadow-md shadow-emerald-950/20"
                >
                  Log Payment
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

