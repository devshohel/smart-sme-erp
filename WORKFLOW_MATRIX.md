# Workflow Matrix

Smart SME ERP transactional records follow a state-machine model. Draft and approved records must not update stock or financial balances. Stock and accounting effects happen at `POST`, and reversal creates compensating movements rather than deleting history.

## Sales

| Module | Lifecycle | Inventory Impact | Accounting Impact | Reversal Rule |
| --- | --- | --- | --- | --- |
| Sales Order | DRAFT -> SUBMITTED -> APPROVED -> CANCELLED | None | None | Order itself is not reversed; invoices created from it control stock/accounting. |
| Sales Invoice | DRAFT -> SUBMITTED -> APPROVED -> POSTED/PARTIAL_PAID/PAID -> REVERSED | POST decreases stock. Draft/approved do not affect stock. | POST creates receivable/income journal. | Reverse only posted unpaid invoices; creates reversal journal and stock-in compensation. |
| Sales Return | DRAFT -> SUBMITTED -> APPROVED -> POSTED -> REVERSED | POST increases stock. | POST reduces receivable/income. | Reverse only posted returns; creates reversal journal and stock-out compensation. |

## Purchases

| Module | Lifecycle | Inventory Impact | Accounting Impact | Reversal Rule |
| --- | --- | --- | --- | --- |
| Purchase Order / Receive | DRAFT -> SUBMITTED -> APPROVED -> RECEIVED/PARTIAL_RECEIVED -> PAID/PARTIAL_PAID | Receiving/posting increases stock. | Purchase posting creates payable/cost journal. | Full purchase reversal is not implemented; purchase returns handle stock/payable compensation. |
| Purchase Return | DRAFT -> SUBMITTED -> APPROVED -> POSTED -> REVERSED | POST decreases stock. | POST reduces payable/purchase cost. | Reverse only posted returns; creates reversal journal and stock-in compensation. |

## Payments

| Module | Lifecycle | Inventory Impact | Accounting Impact | Reversal Rule |
| --- | --- | --- | --- | --- |
| Customer Receipt | DRAFT -> POSTED -> REVERSED/CANCELLED | None | POST reduces receivable and records cash/bank. | Reverse posted receipt through compensating journal and allocation rollback. |
| Supplier Payment | DRAFT -> POSTED -> REVERSED/CANCELLED | None | POST reduces payable and records cash/bank outflow. | Reverse posted payment through compensating journal and allocation rollback. |

## Expenses

| Module | Lifecycle | Inventory Impact | Accounting Impact | Reversal Rule |
| --- | --- | --- | --- | --- |
| Expense | DRAFT -> SUBMITTED -> APPROVED -> POSTED -> REVERSED/CANCELLED | None | POST records expense/tax/payment journal. | Reverse posted expense through compensating journal. |

## Inventory

| Module | Lifecycle | Inventory Impact | Accounting Impact | Reversal Rule |
| --- | --- | --- | --- | --- |
| Stock Adjustment | DRAFT -> APPROVED -> POSTED -> REVERSED/CANCELLED | POST applies quantity change. Draft/approved do not affect stock. | None currently. | Reverse posted adjustment by applying equal opposite stock adjustment. |
| Stock Transfer | DRAFT/PENDING -> APPROVED -> IN_TRANSIT -> RECEIVED -> REVERSED/CANCELLED | SEND moves stock out of source; RECEIVE moves stock into destination. | None currently. | Reverse in-transit transfer by restoring source stock; reverse received transfer by moving stock back from destination to source. |

## Integrity Rules

- Posting requires the expected prior status and rejects duplicate posting.
- Approval requires the expected prior status and rejects duplicate approval.
- Reverse requires a posted/received state and rejects duplicate reversal.
- Stock-out operations validate available stock and use locked stock rows plus optimistic versioning.
- Financial reversal creates compensating journal entries with separate source types.
- Audit/activity logging records workflow actions through explicit service logs and service-level audit fallback.

## Remaining Business Risks

- Purchase order full reversal is not implemented because received purchases may have payments, returns, and downstream supplier allocation history.
- Stock transfer accounting is not implemented because transfers are quantity-only in the current model.
- Stock adjustment accounting is not implemented because no inventory valuation account workflow exists for adjustments yet.
