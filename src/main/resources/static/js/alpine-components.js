/**
 * Alpine.js CSP-compatible component definitions.
 *
 * All x-data objects must be registered here to avoid using Function() constructor
 * which requires 'unsafe-eval' in Content Security Policy.
 *
 * Usage in templates:
 *   OLD: <div x-data="{ open: false }">
 *   NEW: <div x-data="toggleState">
 */

// Simple toggle state (open/closed)
Alpine.data('toggleState', () => ({
    open: false
}))

// Toggle state with hasQuery flag (for search filters)
Alpine.data('searchFilterState', () => ({
    open: false,
    hasQuery: false
}))

// Sidebar state
Alpine.data('sidebarState', () => ({
    sidebarOpen: false
}))

// Expandable section
Alpine.data('expandableState', () => ({
    expanded: false
}))

// Show/hide state
Alpine.data('showState', () => ({
    show: false
}))

// ID type selector
Alpine.data('idTypeSelector', () => ({
    idType: ''
}))

// Void transaction form
Alpine.data('voidForm', () => ({
    voidReason: '',
    confirmVoid: false
}))

// Percentage toggle for salary components
Alpine.data('percentageToggle', (initialValue = false) => ({
    isPercentage: initialValue
}))

// Persistent navigation state for accounting section
Alpine.data('navAkuntansi', () => ({
    open: Alpine.$persist(true).as('nav-akuntansi')
}))

// Persistent navigation state for reports section
Alpine.data('navLaporan', () => ({
    open: Alpine.$persist(false).as('nav-laporan')
}))

// Persistent navigation state for projects section
Alpine.data('navProyek', () => ({
    open: Alpine.$persist(false).as('nav-proyek')
}))

// Persistent navigation state for inventory section
Alpine.data('navInventori', () => ({
    open: Alpine.$persist(false).as('nav-inventori')
}))

// Persistent navigation state for payroll section
Alpine.data('navPayroll', () => ({
    open: Alpine.$persist(false).as('nav-payroll')
}))

// Persistent navigation state for master data section
Alpine.data('navMaster', () => ({
    open: Alpine.$persist(false).as('nav-master')
}))

// Open by default navigation section
Alpine.data('navOpenDefault', () => ({
    open: true
}))

// Closed by default navigation section
Alpine.data('navClosedDefault', () => ({
    open: false
}))
