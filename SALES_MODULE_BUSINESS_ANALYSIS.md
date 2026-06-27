# Smart SME ERP — Sales Module Business Analysis

**Review date:** 27 June 2026  
**Target:** Small and medium businesses, retail shops, pharmacies, mobile/electronics shops, wholesale and trading businesses  
**Design principle:** Maximum usability with minimum complexity

## 1. Executive assessment

Smart SME ERP has a stronger transaction engine than its current Sales user experience suggests. The backend already supports controlled document lifecycles, stock deduction/restoration, accounting posting, credit-limit enforcement, partial receipt allocation, customer aging, ledgers, audit logs, and permission-based actions. Those are valuable foundations.

The main weakness is the operational path. The application is designed like a document-control ERP, while the target users often need a fast counter-sale system. The visible Sales menu contains only Orders, Invoices, and Returns. Customer collections and due management are separated under Customers, there is no POS/Quick Sale, and ordinary users must understand drafts, submission, approval, posting, receipts, and allocations to complete a sale.

Most critically, several lifecycle actions exist in Angular component code and Spring Boot endpoints but are not rendered in the current Sales Order and Sales Invoice templates. Users can save documents but may be unable to submit, approve, post, convert, cancel, or initiate a return from the visible screen. This makes the workflow appear complete technically while remaining incomplete operationally.

### Scorecard

| Measure | Score | Interpretation |
|---|---:|---|
| Overall Sales Module | **5.8/10** | Sound ERP foundation, incomplete SME sales experience |
| Complexity | **7.2/10** | 10 means excessively complex; too many states for routine sales |
| User Friendliness | **5.2/10** | Consistent styling, but long pages, hidden actions and ERP terminology hinder use |
| SME Suitability | **6.0/10** | Better for controlled wholesale than fast retail |
| Transaction/Accounting Integrity | **7.8/10** | Posting, inventory, credit and allocation rules are comparatively strong |
| Retail Counter Readiness | **3.5/10** | No one-screen sale, barcode, walk-in customer, tender or change calculation |

### Verdict

Do not expand toward SAP/Oracle-style document complexity. Keep the existing controlled workflow as an optional **Advanced/Approval Mode**, and add a default **Quick Sale** workflow that creates, posts, receives payment, and prints in one operation.

## 2. Evidence and scope

This review covers the supplied six screenshots and the current Angular/Spring Boot implementation. It includes:

- Sales routes, templates, component logic, services and permissions.
- Sales Orders, Sales Invoices and Sales Returns.
- Customers, customer details/history, receipts, aging and ledgers.
- General dashboard sales metrics, sales reports and invoice settings.
- Backend document states, stock movements, accounting posting, credit checks and receipt allocation.

### Confirmed menu and feature inventory

| Capability | Current state |
|---|---|
| Dedicated Sales Dashboard | **Not present**; general Dashboard has sales metrics and recent sales |
| Sales Orders | Present |
| Sales Invoices | Present |
| Sales Returns | Present |
| Customers | Present as a separate top-level module |
| Payment Collection | Present as **Customer Receipts** under Customers |
| Due Management | Present through invoice due, Customer Aging, customer ledger and receipts; no unified Due Collection workspace |
| Quotations | **Not present** |
| Delivery Management | **Not present** |
| POS / Quick Sale | **Not present** |
| Sales Reports | Present under Reports |
| Customer Purchase History | Present in Customer Details |
| Invoice Settings | Present under Settings |

## 3. Phase 1 — Current-state module analysis

### 3.1 General Dashboard sales content

**Purpose and value:** Gives the owner a broad business summary, sales-versus-purchase trend and recent sales. It is useful for daily awareness but is not a Sales work queue.

**Complexity:** Medium. **Classification:** Recommended. **SME fit:** Good as a shared owner dashboard.

**Gaps:** No pending order count, draft invoice count, overdue receivables, top products today, cashier totals, low-stock-at-sale alert, or shortcuts to Quick Sale and Due Collection. Recent Sales opens the invoice list rather than the selected invoice.

### 3.2 Sales Orders

**Purpose:** Capture a customer request before invoicing, support approval, and convert an approved order to an invoice.

**Business value:** High for wholesale, distribution and trading; low for walk-in retail.

**Current workflow capability:** Draft → Submitted → Approved/Rejected → Converted or Cancelled. Conversion creates a draft invoice.

**Complexity:** High for an SME because approval and conversion are mandatory in the controlled path. **Classification:** Recommended for wholesale/trading; Optional for retail; approval/rejection is Enterprise Only unless enabled by policy.

**UI findings:**

- The list and create/edit form share one very long page. Even the `/create` route loads and displays the list above the form, forcing substantial scrolling.
- No prominent **New Order** button is visible at the page header.
- The list loads all records and filters in the browser; it has no pagination.
- Dates display raw timestamps such as `2026-06-23T00:00:00`.
- The action named **Delete** actually invokes business cancellation. This is misleading and dangerous.
- Component methods exist for Submit, Reject, Cancel, Convert and Print, but the template exposes only View, Edit, Cancel-as-Delete and conditional Approve. Most of the lifecycle is unreachable from the visible page.
- The form includes a read-only Document Status that adds visual weight without helping entry.
- Order line logic supports discount and tax internally, but the order table does not show those fields. Editable summary totals alter hidden values on the last line, which is surprising and weakens audit clarity.

**Suitability:**

- Retail / Pharmacy: Low; skip the order for normal counter sales.
- Mobile / Electronics: Medium for reservations or special orders; low for immediate sales.
- Wholesale / Distribution / Trading: High business relevance, but workflow should be configurable and actions made visible.

### 3.3 Sales Invoices

**Purpose:** Create direct invoices or invoices based on approved orders, track totals/due, post inventory and accounting, and print.

**Business value:** Core revenue document. **Classification:** Essential.

**Current backend workflow:** Draft → Submitted → Approved → Posted / Partial Paid / Paid. Posting checks customer credit, deducts stock, posts accounting, and creates a notification. Unposted documents can be cancelled.

**Complexity:** High for retail, medium for wholesale. The backend control is good, but the default path is too formal.

**UI findings:**

- The list appears above the create form on the same long page, with no primary **New Invoice/Quick Sale** header action.
- Invoice lifecycle methods exist in TypeScript, but Submit, Approve, Post, Cancel and Return-from-Invoice buttons are absent from the template. A user can save a draft and then has no visible way to post it.
- The list provides only View and Print. Printing a draft is possible unless business rules are added to the UI.
- `paidAmount` is read-only and labelled **Receipt Allocation**. Payment cannot be taken during invoice creation.
- Discounts and taxes are line-level numeric amounts only; no percentage option, order-level discount, named tax rule, or reason/authorization.
- Summary totals are editable duplicates of line calculations. Editing a total silently pushes the difference into the last item's price, discount or tax. This is confusing and can distort margin, tax, and line history. Summary totals should be calculated, never used as hidden adjustment controls.
- Product selection is a conventional dropdown; there is no barcode focus, SKU search, keyboard-first flow, stock-on-hand display, batch/expiry, serial/IMEI or warranty capture.
- Warehouse and date require attention on every sale although both can normally default from user/store and today.
- Dates in the list are raw ISO timestamps. There is no pagination, saved filter, export button, or quick payment action.
- `window.print()` is used instead of a transaction receipt layout and printer choice.

**Suitability:**

- Retail / Pharmacy: Low in its current form; routine sale requires too much document handling.
- Mobile / Electronics: Medium; invoice basis is useful, but serial/IMEI and warranty are missing.
- Wholesale / Distribution / Trading: Medium-high; credit and order linkage help, but delivery terms and fulfillment are absent.

### 3.4 Sales Returns

**Purpose:** Return items from an invoice, restore stock, post the accounting effect, and reduce invoice due.

**Business value:** Essential for all target sectors. **Classification:** Essential; multi-level approval is Optional/Enterprise Only.

**Current workflow:** Draft → Submitted → Approved/Rejected → Posted or Cancelled. Posting validates sold and previously returned quantities, restores stock and adjusts invoice due/payment state.

**Complexity:** Medium-high.

**UI and business findings:**

- Selecting an invoice loads every sold line into the return. Users must edit quantities/remove lines instead of explicitly choosing returned items.
- Customer is independently selectable even though the invoice determines the customer. It should be auto-filled and locked; duplicate entry risks inconsistency and confusion.
- Unit price is editable. For a normal invoice return it should default and lock to the net refundable invoice price, with manager override only.
- No return reason per item, condition (resellable/damaged), restock decision, refund method, exchange option, or credit note display.
- The backend currently blocks a posted return when return value exceeds current invoice due. Therefore a fully paid invoice cannot be refunded through the standard return process until credit memo/refund support exists. This is a major real-world gap.
- Return lifecycle actions are visible in the Selected Return card, unlike order/invoice actions, but users must select a row and then look below the list.
- There is no direct invoice context hand-off: `newReturnFromInvoice()` navigates to return create without passing the invoice ID.
- No reversal endpoint is exposed in controllers/UI even though internal service code contains reversal logic.

**Suitability:** Functionally relevant to all sectors, but inadequate for fast retail refunds and exchanges.

### 3.5 Customers

**Purpose:** Maintain customer master data, credit limits, balances and contact information.

**Business value:** Essential for credit and relationship sales. **Classification:** Essential, with a default Walk-in Customer.

**Complexity:** Medium. The form has approximately 14 business fields. It is acceptable for B2B customers but too much for a cash buyer.

**Keep visible by default:** Name and phone. **Optional:** company, email, address. **Advanced:** tax number, credit limit, opening balance, postal details and status. Customer code/current balance should remain auto/read-only.

**Positive:** Customer details provide overview, invoice history, receipts, ledger, statement and actions for new receipt/aging. This is a strong base for customer purchase history.

**Problem:** Creating a customer is a separate sidebar destination named **Customer Form**, a technical label. Users should see **Customers** and **Add Customer**, with inline Quick Add available from a sale.

### 3.6 Customer Receipts / Payment Collection

**Purpose:** Record later payments, allocate them automatically to oldest invoices or manually to selected invoices, and post accounting.

**Business value:** High for wholesale and credit sales. **Classification:** Essential when credit sales are allowed; Recommended otherwise.

**Strengths:** Cash, bank, mobile banking, cheque and other payment methods; auto/manual allocation; partial payment support; save draft or save-and-post; pagination, sorting, filters, accounting journal link and unapplied amount.

**Complexity:** Medium-high due to accounting language.

**UX issues:**

- **Customer Receipts** is not a natural term for many shop users; **Receive Payment** or **Due Collection** is clearer.
- Allocation Mode, Manual Allocation, Unapplied, Post and Journal are accountant-facing concepts. Default to automatic allocation and hide manual allocation under Advanced.
- It is separated from invoice and due views, creating navigation and repeated customer search.
- Only one payment method exists per receipt; no split cash/card/mobile payment.
- A posted receipt cannot be reversed through the current workflow.
- Print permission exists but the current details page lacks a visible receipt Print action.

### 3.7 Customer Aging / Due Management

**Purpose:** Show current, 1–30, 31–60, 61–90 and 90+ day due amounts.

**Business value:** High for wholesale/distribution; moderate for retail credit. **Classification:** Recommended.

**Strengths:** Search, date filters, totals, paging and CSV export.

**Gaps:** It is a report, not a collection workspace. Rows do not visibly offer Receive Payment, call/WhatsApp, statement, promise date, overdue priority or customer drill-down. It should feed a simple **Due Collection** queue.

### 3.8 Sales reports and invoice settings

Sales summary, invoice detail, sales returns, product sales and customer sales reports are present. They are **Recommended** and appropriate for owners/accountants. Keep them outside the daily cashier menu. Invoice settings are **Recommended** and should be administered once, not exposed during transaction entry.

### 3.9 Feature classification summary

| Capability | Classification | Current assessment |
|---|---|---|
| Quick Sale/POS | Essential for target retail sectors | Missing |
| Sales Invoice | Essential | Present, workflow UI incomplete |
| Sales Return | Essential | Present, refund/credit gap |
| Walk-in Customer | Essential | Missing |
| Customers | Essential | Present |
| Receive Payment / partial payment | Essential for credit sales | Present separately as Customer Receipts |
| Due list | Recommended | Aging exists; collection workflow missing |
| Sales Orders | Recommended for B2B; Optional for retail | Present |
| Customer history | Recommended | Present |
| Sales reports | Recommended | Present |
| Quotations | Recommended for wholesale/trading; Optional elsewhere | Missing |
| Delivery Management | Recommended for distribution; Optional elsewhere | Missing |
| Approval chains | Enterprise Only / configurable | Applied too broadly |
| Manual allocation and journal detail | Enterprise/Accountant only | Present and visually prominent |
| Editable calculated summaries | Unnecessary and risky | Present |

### 3.10 Screen, form, table and dialog inventory

| Surface | Complexity | Clutter / click assessment | SME recommendation |
|---|---:|---|---|
| Sales Order list | Medium | Readable columns, but all rows load at once and actions wrap; list sits above entry form | Keep as a focused paged list with New Order and next-action menu |
| Sales Order create/edit/details form | High | Long scroll, status noise, hidden lifecycle, duplicated summary manipulation | Separate page; basic header + line grid; collapse Advanced |
| Sales Invoice list | Medium | Useful due/payment columns; raw dates; only View/Print; no quick collect | Keep, add quick filters, Receive Payment and server pagination |
| Sales Invoice create/edit/details form | High | Too many line/summary controls; no visible completion actions | Replace normal use with Quick Sale; retain as Advanced Invoice |
| Sales Return list + selected card | Medium-high | Three stacked regions require selection then scrolling | Separate list and focused return wizard/drawer |
| Sales Return form | High | Invoice and customer duplicated; every line copied; refund data absent | Invoice-led line selector with reason, disposition and refund |
| Customer list | Medium | Appropriate master list | Keep; add Quick Add from sales |
| Customer form | Medium | Too many fields for casual cash buyer | Two-field quick form + expandable business/accounting details |
| Customer details / tabs | Medium-high | Information-rich but justified for credit/customer service work | Keep for managers/accountants; surface Recent Purchases in sale |
| Customer Aging table | Medium | Report is readable, but no row-level collection action | Convert into actionable Due Collection list |
| Customer Receipt list | Medium-high | Strong filtering/paging; many accounting columns and icon actions | Role-tailor columns; rename Receive Payment |
| Customer Receipt form | Medium-high | Manual allocation terminology increases training | Default auto-allocation; hide manual mode under Advanced |
| Receipt details | Medium | Useful summary/allocation/audit data | Hide audit fields by default; add Print/Reverse where permitted |

There are no purpose-built Sales modals. Rejection reasons use the browser's native `window.prompt`, which lacks validation, consistent styling and contextual guidance. Cancellation actions do not consistently capture a reason. Replace these with a small confirmation dialog containing the document number, consequence, required reason where appropriate, and a safe default focus.

### 3.11 Suitability by business type

Legend: **High** = ready/relevant, **Medium** = useful with simplification, **Low** = poor current fit, **N/A** = generally not needed.

| Capability | Retail | Pharmacy | Mobile | Electronics | Wholesale | Distribution | Trading |
|---|---|---|---|---|---|---|---|
| General Dashboard sales content | Medium | Medium | Medium | Medium | Medium | Medium | Medium |
| Sales Orders | Low | Low | Medium | Medium | High | High | High |
| Sales Invoices | Medium | Low | Medium | Medium | High | High | High |
| Sales Returns | Medium | Medium | Medium | Medium | High | High | High |
| Customer master | Medium | Medium | High | High | High | High | High |
| Customer Receipts | Medium | Medium | Medium | Medium | High | High | High |
| Customer Aging / Dues | Medium | Medium | Medium | Medium | High | High | High |
| Customer Details / History | Medium | Medium | High | High | High | High | High |
| Sales Reports | High | High | High | High | High | High | High |
| Current approval workflow | Low | Low | Low | Low | Medium | Medium | Medium |
| Current product entry | Low | Low | Low | Low | Medium | Medium | Medium |

The low Pharmacy score is not because invoice/return concepts are unsuitable; it is because batch, expiry, FEFO and fast barcode operation are absent. Mobile/electronics remain medium until serial/IMEI and warranty tracking are added. Wholesale/distribution/trading score higher because the existing customer, order, credit, aging and accounting concepts match their work, though delivery and fulfillment remain gaps.

## 4. Phase 2 — Role-based user experience

| Role | Daily tasks and frequent screens | Current friction | Training-reducing improvement |
|---|---|---|---|
| Shop Owner | Dashboard, sales totals, dues, profit, exceptions | General dashboard lacks sales actions and exception queues; profit not shown at sale | Owner dashboard with Today Sales, Gross Profit, Cash Collected, Due, Returns, low stock and pending approvals |
| Sales Executive | Customer search, quotation/order, price/discount, follow-up | No quotation; order lifecycle actions hidden; repeated customer/warehouse/date entry | Customer 360, quick quote/order, visible next action, saved price list and recent items |
| Cashier | Barcode/product search, payment, change, receipt, hold/resume | No POS, walk-in buyer, payment tender, split payment, hold or keyboard workflow | One-screen Quick Sale; barcode focus; default walk-in; cash/mobile/card buttons; save+post+print |
| Accountant | Posted invoices, receipts, allocation, aging, ledger, reconciliation | Strong receipt/ledger tools, but invoice posting actions are inaccessible and reversals incomplete | Accountant work queue, clear Posted date/journal, controlled reversal and credit note workflow |
| Store Manager | Approvals, stock availability, discounts, returns, cashier closing | Approval logic exists but actions are inconsistent; no stock warning or return condition | Exception-based approvals only, stock-at-line display, discount override alerts, return disposition and shift summary |

### Repetition and frustration inventory

- Customer is searched again across order, invoice, return and receipt.
- Warehouse and transaction date are repeatedly selected instead of defaulted.
- Wholesale path duplicates the same lines from order to invoice, then requires another invoice lifecycle.
- Payment requires leaving Sales for Customers → Customer Receipts.
- A form is below a potentially long list, so scrolling replaces navigation.
- Status terminology is excessive: Draft, Submitted, Approved, Converted, Posted, Partial Paid, Paid, Rejected, Cancelled and Reversed.
- Users cannot infer the next valid action because buttons are missing or scattered.
- Dropdown product selection is slow for large catalogs.
- Raw timestamps, tiny controls at high-density layouts and wide minimum-width tables reduce readability.

## 5. Phase 3 — Real-world workflow review

### Scenario 1: One medicine sold to a walk-in customer

**Expected:** Scan/search → quantity → payment → print. About 4 actions on one screen.

**Current practical path:** Open Invoices → scroll through invoice list → find/create named customer → select warehouse → select product → save draft → lifecycle actions are not visible → separately create/post receipt → return to invoice → print.

**Assessment:** Too complex, too many screens and presently blocked at draft. Missing walk-in customer, barcode, tender, change, batch/expiry selection and one-click post/print.

### Scenario 2: Large wholesale order

**Expected:** Customer/order → validate stock and credit → approve only if policy requires → fulfill/invoice → collect now or record due.

**Current designed path:** Create Order → Save Draft → Submit → Approve → Convert → Invoice Draft → Submit → Approve → Post → create/post receipt.

**Assessment:** 8–10 transitions are excessive for most SMEs. Order approval and invoice approval duplicate control. Choose one approval gate. Delivery/partial fulfillment/backorder is missing.

### Scenario 3: Customer buys on credit

**Current strength:** Posting enforces customer credit limit and creates due; aging/ledger can later report it.

**Weakness:** There is no pre-sale available-credit indicator or warning before the final Post. The hard error arrives late. No due date/payment terms are stored on the invoice.

**Assessment:** Business foundation is good; user guidance and terms are missing.

### Scenario 4: Customer partially pays later

**Current path:** Customers → Customer Receipts → New Receipt → search customer → amount → choose auto/manual allocation → Save & Post.

**Assessment:** Functionally supported and one of the module's stronger workflows. Simplify the name and default to automatic allocation. Add **Receive Payment** directly from invoice, customer and due list.

### Scenario 5: Sales return

**Current designed path:** Select invoice → all lines copied → adjust lines → save draft → submit → approve → post.

**Assessment:** Too many steps for a small cash return. There is no refund/exchange handling, and returns against fully paid invoices are blocked because credit memo/refund support is absent. Use a two-level policy: cashier requests and manager approves only above thresholds or for restricted items.

## 6. Phase 4 — Simplification strategy

### Quick Sale / Direct Invoice

**Current:** Invoice list → scroll to form → customer → warehouse → date → item → edit line/summary → save draft → submit → approve → post → receipt → print.

**Recommended:** Scan/search item → select Walk-in/known customer → choose payment (or Due) → **Complete & Print**.

- Auto-fill store warehouse, today's date, cashier, product price, tax rule and Walk-in Customer.
- Complete & Print should atomically create/post invoice, deduct stock, post accounting, post payment and print.
- Put notes, order reference, manual price, tax override, salesperson and cost/profit under Advanced.
- Keep Save Draft/Hold as secondary actions.

### Sales Orders

**Current:** Create → save → submit → approve → convert to draft invoice → submit → approve → post.

**Recommended:** Create order → Confirm (optional approval by rule) → Fulfill & Invoice.

- Remove duplicate invoice submission/approval for an already approved order.
- Auto-fill warehouse/date/salesperson and carry discounts/taxes exactly into invoice.
- Hide status field, audit metadata and approval controls until relevant.
- Add stock status, promised date and partial fulfillment only for wholesale/distribution mode.

### Sales Invoices

**Current:** Save draft and manage downstream payment elsewhere.

**Recommended:** Draft only when requested; otherwise **Post Sale**, then accept payment in the same panel.

- Make totals read-only.
- Allow line discount amount/percentage; put tax and override controls in Advanced.
- Add Receive Payment, Return, Duplicate and Print in a clear action menu.
- Default filter to recent 30 days; use server pagination.

### Sales Returns

**Current:** Select invoice and customer → copy every line → save → submit → approve → post.

**Recommended:** Search/scan invoice → select returned lines and quantities → choose reason/disposition/refund method → Confirm Return.

- Customer, warehouse, original price and tax should be auto-filled and locked.
- Show already-returned and remaining quantities.
- Auto-restock only Resellable items; route Damaged items to quarantine/adjustment.
- Approval should be rule-based by amount, age, no-receipt status or product type.

### Customers

**Current:** Open Customer Form → enter master fields → return to sale and search again.

**Recommended:** Inline **Quick Add** with name + phone → continue sale. Full customer profile is optional later.

- Auto-generate code and default status/country/credit limit.
- Move company, address, tax, credit and opening balance to Advanced/Accounting.

### Payments and due collection

**Current:** Separate Receipt list/form → customer → amount → allocation mode → payment method → save/post.

**Recommended:** Invoice/customer/due row → Receive Payment → amount defaults to due → method → Save & Print.

- Default allocation to selected invoice or oldest due.
- Hide Allocation Mode, manual allocations, journal number and unapplied amount under Advanced.
- Support partial and split payments, payment reference conditionally, and posted receipt reversal.

## 7. Phase 5 — Missing feature analysis

| Commercial feature | Status | Decision |
|---|---|---|
| Quick Sale / POS | Missing | Must Have |
| Walk-in Customer | Missing | Must Have |
| Barcode Sales | Missing from Sales UI | Must Have |
| Hold Sale | Missing | Must Have |
| Draft Invoice | Present | Keep as secondary action |
| Partial Payment | Present through Customer Receipts | Must Have; integrate into sale |
| Due Collection | Partially present through Aging + Receipts | Must Have as a unified workspace |
| Multiple payment methods | Several single methods present; split tender missing | Must Have |
| Print Receipt | Generic browser print only | Must Have proper templates |
| Customer Purchase History | Present in Customer Details | Recommended; surface contextually |
| Recent Transactions | Present on general dashboard/customer detail | Recommended; add to sale and due workspaces |
| Credit Limit Warning | Hard backend block at posting | Must Have earlier warning |
| Stock Availability Warning | Late stock-out validation; no clear pre-sale line indicator | Must Have |
| Profit Preview | Missing | Nice to Have, permission-restricted |
| Discount Options | Fixed line amount present | Must Have percentage/fixed and authorization rules |
| Tax Calculation | Fixed line tax amount present | Must Have configured automatic tax rules |
| Return from Invoice | Component method exists but is not rendered/contextual | Must Have |

### Must Have

- Quick Sale/POS with keyboard and touch-friendly layout.
- Default Walk-in Customer.
- Barcode/SKU search and persistent scan focus.
- Complete Sale / Save & Post / Print Receipt in one transaction.
- Payment at sale: Cash, Card, Mobile Banking, Bank and Due.
- Partial and split payment; cash received and change due.
- Hold/Resume sale and draft invoice.
- Stock-on-hand and insufficient-stock warning before completion.
- Credit available/limit warning before posting, plus invoice due date and payment terms.
- Return directly from invoice with line selection, reason, refund/credit/exchange.
- Proper thermal/A4 print templates and reprint marker.
- Server-side pagination and formatted dates.
- For pharmacy: batch/lot and expiry-aware sale/return, FEFO suggestion.
- For mobile/electronics: serial/IMEI and warranty capture.

### Nice to Have

- Customer purchase history and recent transactions in the sale panel (backend data already exists).
- Percentage/fixed discount, order discount, discount reason and manager threshold.
- Tax-inclusive/exclusive rules and named tax profiles.
- Gross-profit preview for authorized users only.
- Favorite products, recent products and category tiles.
- Quotation → order/invoice conversion.
- Delivery note, promised date and delivery status for wholesale/distribution.
- Price lists, wholesale tiers and customer-specific prices.
- SMS/WhatsApp/email invoice or due reminder.
- Cashier shift/open-close and daily tender reconciliation.

### Enterprise Features

- Multi-level approvals and segregation of duties.
- Partial shipment, backorder and allocation/reservation.
- Salesperson commission, territory and route sales.
- Credit memo approval, return authorization and advanced refund controls.
- Contract pricing, promotions engine and complex tax jurisdiction.
- Multi-branch fulfillment and centralized credit control.

## 8. Phase 6 — UI/UX recommendations

### Sidebar and naming

Current Sales menu is clean but incomplete for daily work, while collections are hidden under Customers. Rename technical labels:

- Orders → **Sales Orders**
- Invoices → **Sales History** or **Invoices**
- Returns → **Sales Returns**
- Customer Form → **Add Customer**
- Customer Receipts → **Receive Payment**
- Customer Aging → **Customer Dues**

Do not add every report to Sales. Keep daily actions in Sales and analytical reports under Reports.

### Button placement and actions

- Put one primary action at the top right: **New Sale**, **New Order**, **Receive Payment**.
- Use a sticky bottom action bar on transaction screens: Hold, Cancel, Complete & Print.
- Present only the valid next action prominently; place Cancel/Reverse in an overflow menu with confirmation and reason.
- Never label Cancel as Delete.
- Use text plus icon for consequential actions; icon-only buttons need tooltips and accessible labels.

### Tables

- Use server pagination, default 25 rows, sticky headers and responsive column priority.
- Show formatted local date (`27 Jun 2026`), currency symbol/configuration and right-aligned amounts.
- On small screens, show document, customer, total/status; move remaining fields to an expandable row/card.
- Replace repeated View/Edit/Delete button clusters with row click plus a compact action menu.
- Add quick filters: Today, Unpaid, Draft, Pending Approval, Posted.

### Forms and search

- Do not place the create form below the full list. Use separate focused routes or a right-side drawer for quick entry.
- Make product search autocomplete by barcode, SKU and name; show price and available quantity in suggestions.
- Keep calculated fields read-only.
- Use progressive disclosure: Basic Sale first, Advanced Details collapsed.
- Preserve keyboard order and shortcuts: F2 product, F4 customer, F8 hold, F9 payment, F10 complete.
- Make touch targets at least 44 px and avoid tables with fixed minimum widths as the only mobile strategy.

### Visual defects observed

- Duplicate breadcrumb wording (`Sales > Sales > ...`).
- Large empty spaces and full-width cards make forms long despite relatively few fields.
- Raw ISO timestamps reduce scanability.
- At desktop zoom/density, table content and action buttons become cramped; at smaller width, columns wrap awkwardly.
- Status and payment badges are useful, but too many document states increase cognitive load.

## 9. Phase 7 — Recommended final structure

### Default SME menu

1. **Sales**
   - Quick Sale
   - Sales History
   - Sales Returns
   - Sales Orders *(show only when enabled)*
2. **Customers**
   - Customers
   - Customer Dues
   - Receive Payment
3. **Reports**
   - Sales Summary
   - Product Sales
   - Customer Sales
   - Sales Returns

Optional feature flags can add Quotations and Deliveries for wholesale/distribution businesses. Approval queues should appear only for roles that approve.

### Recommended workflow structure

**Cash sale:** Quick Sale → items → payment → Complete & Print.  
**Credit sale:** Quick Sale → known customer → items → payment = Due/Partial → confirm credit → Complete & Print.  
**Later collection:** Customer Dues → Receive Payment → amount/method → Save & Print.  
**Wholesale:** Quotation (optional) → Order → Confirm/Approve by rule → Fulfill & Invoice → collect or due.  
**Return:** Find invoice → select lines/reason/disposition → refund/credit/exchange → Confirm & Print.

### Remove

- Editable summary totals that secretly rewrite the last invoice/order line.
- Duplicate Sales breadcrumb.
- Read-only status fields from entry forms.
- Mandatory approval steps for ordinary retail sales.
- Full sales list above create/edit/details forms.
- Misleading Delete label for cancellation.

### Merge

- Customer Aging + unpaid invoices + Customer Receipts into **Customer Dues / Receive Payment**.
- Invoice creation + immediate payment + posting + receipt printing into **Quick Sale**.
- Approved order conversion and invoice confirmation into **Fulfill & Invoice** where policy permits.
- Customer history, ledger and dues into one Customer 360 page (current details page is already close).

### Add

- POS/Quick Sale, walk-in customer, barcode, hold/resume, tender/change and split payment.
- Visible valid lifecycle actions and contextual next-step guidance.
- Refund/credit note/exchange workflow.
- Stock and credit warnings before final action.
- Pharmacy batch/expiry and electronics serial/IMEI support.
- Optional quotation and delivery flows.
- Dedicated sales/collection work queues and cashier shift summary.

### Simplify

- Default warehouse/date/customer/payment allocation automatically.
- Hide accounting and approval terminology from cashier/sales roles.
- Use rule-based approvals only for credit excess, high discount, large return or manager-defined thresholds.
- Use one primary action per screen and progressive disclosure for advanced fields.
- Keep status model internally for control, but present plain-language next actions to users.

## 10. Prioritized implementation roadmap

### P0 — Make existing business workflows operable

1. Expose valid Submit/Approve/Post/Convert/Cancel/Return actions in Order and Invoice views, permission-aware.
2. Separate list and form rendering so create/edit/details routes do not display the full list first.
3. Remove editable summary overrides; totals must derive transparently from lines.
4. Rename Delete to Cancel and add confirmation/reason.
5. Format dates/currency and add server-side pagination.
6. Pass invoice context into Return and lock invoice-derived customer/price.

### P1 — Deliver the SME fast path

1. Quick Sale/POS with Walk-in Customer, barcode and default warehouse.
2. Integrated immediate/partial/due payment and Complete & Print.
3. Customer Dues workspace with direct Receive Payment.
4. Stock/credit warnings before completion.
5. Return refund/credit/exchange support, including fully paid invoices.

### P2 — Vertical capability

1. Pharmacy batch/expiry/FEFO.
2. Mobile/electronics serial, IMEI and warranty.
3. Wholesale quotation, price list, delivery, partial fulfillment and backorder.
4. Cashier shift/tender reconciliation and owner sales dashboard.

## Final recommendation

Preserve the backend's audit, posting, stock and accounting controls, but stop making every user operate them manually. The ideal Smart SME ERP Sales experience has two modes:

- **Simple Mode (default):** Quick Sale, Sales History, Returns, Customer Dues; one-screen completion and plain language.
- **Controlled Mode (optional):** Orders, approvals, posting queues, manual allocations, quotations and delivery.

This split serves a one-counter pharmacy and a growing distributor without forcing either business into the other's workflow. It is the clearest route to maximum usability with minimum complexity.
