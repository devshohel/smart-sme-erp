# CRUD Compliance Report

## Scope
Audit of frontend Angular services and backend Spring Boot REST endpoints for CRUD verb alignment, soft delete behavior, restore readiness, permissions, validation, and audit logging.

## Summary
- Frontend build verified successfully after converting the main create/update services to `PUT` when `id` is present.
- Backend restore infrastructure was added for soft-deleted resources.
- Existing response wrapping already exists through `ApiResponseBodyAdvice`; no mass response-shape rewrite was applied to avoid breaking API contracts.

## Services Converted From `POST` To `PUT`
- `ProductCategoryService.saveCategory(...)` now uses `PUT /api/v1/categories/{id}` when `id` exists.
- `ProductBrandService.saveBrand(...)` now uses `PUT /api/v1/brands/{id}` when `id` exists.
- `UomService.saveUom(...)` now uses `PUT /api/v1/uoms/{id}` when `id` exists.
- `InventoryWarehouseService.saveWarehouse(...)` now uses `PUT /api/v1/warehouses/{id}` when `id` exists.

## Controllers Fixed
- `ProductController`
  - Added `PUT /api/v1/products/{id}/restore`
- `ProductCategoryController`
  - Added `PUT /api/v1/categories/{id}/restore`
- `ProductBrandController`
  - Added `PUT /api/v1/brands/{id}/restore`
- `UomController`
  - Added `PUT /api/v1/uoms/{id}/restore` as a placeholder; UOM is not soft-deleted yet
- `WarehouseController`
  - Added `PUT /api/v1/warehouses/{id}`
  - Added `PUT /api/v1/warehouses/{id}/restore`
- `CustomerController`
  - Added `PUT /api/v1/customers/{id}/restore`
- `SupplierController`
  - Added `PUT /api/v1/suppliers/{id}/restore`
- `UserController`
  - Added `PUT /api/v1/users/{id}/restore`

## Permission Corrections
- Added restore permissions to controller guards:
  - `PRODUCT_RESTORE`
  - `CATEGORY_RESTORE`
  - `BRAND_RESTORE`
  - `UOM_RESTORE`
  - `WAREHOUSE_RESTORE`
  - `CUSTOMER_RESTORE`
  - `SUPPLIER_RESTORE`
  - `USER_RESTORE`
- Warehouse update now uses `INVENTORY_EDIT`.
- Restore endpoints are permission-gated and do not expose deleted records without authorization.

## Soft Delete Status By Module
| Module | Soft Delete | Restore Ready |
|---|---:|---:|
| Product | Yes | Yes |
| Category | Yes | Yes |
| Brand | Yes | Yes |
| UOM | No | No |
| Warehouse | Yes | Yes |
| Customer | Yes | Yes |
| Supplier | Yes | Yes |
| User | Yes | Yes |
| Expense Category | No | No |

## Hard Delete Locations Found
- `UomServiceImpl.delete(Long id)` physically deletes because `Uom` has no soft-delete mapping.
- `ExpenseCategoryServiceImpl.delete(Long id)` physically deletes because `ExpenseCategory` has no soft-delete mapping.

Soft-delete-backed deletes that remain safe:
- Product
- Product Category
- Product Brand
- Warehouse
- Customer
- Supplier
- User

## REST Violations / Non-REST Action Endpoints
These remain by design as workflow/state-transition endpoints:
- `POST /submit`
- `POST /approve`
- `POST /reject`
- `POST /post`
- `POST /cancel`
- `POST /reverse`
- `POST /receive`
- `POST /convert-to-invoice`
- `POST /deactivate`

These are not CRUD endpoints and were not converted because changing them would risk breaking business workflows.

## Validation Notes
- Existing DTO validation is present for the audited CRUD resources:
  - `@NotBlank`
  - `@NotNull`
  - `@Size`
  - `@Email`
  - `@Positive`
- No new frontend/backend validation mismatch was introduced in the audited CRUD paths.

## Audit Logging Notes
- Existing audit logging already covers:
  - Product create/update/delete
  - Customer create/update/delete
  - Supplier create/update/delete
  - User create/update/delete/deactivate
- Restore actions now log for:
  - Product
  - Customer
  - Supplier
  - User
- Category, Brand, Warehouse, and UOM restore paths were added without new audit wiring because those modules did not already have a shared audit service in place.

## Response Standardization Notes
- The backend already has `ApiResponseBodyAdvice` and `ApiResponse`.
- Because current clients expect direct DTO/entity payloads unless wrapped explicitly, I did not force a global response format change.

## Remaining Risks
- Backend compilation could not be fully verified in this environment because the Maven wrapper failed to start in the local shell.
- UOM restore is only a placeholder until soft delete is introduced for that entity.
- Action-style endpoints are still non-REST by design for workflow transitions.

## Recommended Next Phase
1. Introduce a consistent `restore` implementation for UOM if soft delete is added to the entity.
2. Standardize workflow action endpoints with a documented action-contract pattern.
3. Align controller responses gradually behind `X-Wrap-Response` for clients that opt in.
4. Add integration tests for CRUD + restore + permission checks on Product, Category, Brand, Warehouse, Customer, Supplier, and User.
