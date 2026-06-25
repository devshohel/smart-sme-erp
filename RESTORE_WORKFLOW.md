# Restore Workflow

## Supported Modules

- Products
- Categories
- Brands
- Customers
- Suppliers
- Warehouses
- Users
- UOM
- Expense Categories

## Endpoints Used

- `GET /api/v1/{resource}`
- `GET /api/v1/{resource}/deleted`
- `PUT /api/v1/{resource}/{id}/restore`

## Permissions Used

- `PRODUCT_RESTORE`
- `CATEGORY_RESTORE`
- `BRAND_RESTORE`
- `CUSTOMER_RESTORE`
- `SUPPLIER_RESTORE`
- `WAREHOUSE_RESTORE`
- `USER_RESTORE`
- `UOM_RESTORE`
- `EXPENSE_CATEGORY_RESTORE`

## UI Behavior

- Active list is the default view.
- Show Deleted switches the list into recycle-bin mode.
- Restore actions are hidden unless the matching restore permission is present.
- Deleted rows are marked as deleted and are not editable from the list.

## Limitations

- Permanent delete is not implemented where no backend endpoint exists.
- Backend verification in this environment was limited to source-level inspection; Maven wrapper execution is unavailable here.
