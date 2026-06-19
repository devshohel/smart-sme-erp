export type DashboardPeriod = 'today' | 'month' | 'year' | 'custom';

export interface DashboardFilter {
  period: DashboardPeriod;
  fromDate?: string;
  toDate?: string;
}

export interface DashboardChartPoint {
  label: string;
  value: number;
  secondaryValue: number;
}

export interface DashboardSalesPurchasePoint {
  label: string;
  sales: number;
  purchase: number;
}

export interface TopSellingProduct {
  productId: number | null;
  productName: string;
  sku?: string | null;
  quantity: number;
  amount: number;
}

export interface LowStockAlert {
  productId: number | null;
  productName: string;
  sku?: string | null;
  warehouseName: string;
  quantity: number;
  reorderLevel: number;
}

export interface DueAlert {
  type: string;
  referenceNo: string;
  partyName: string;
  dueAmount: number;
  date: string;
}

export interface RecentTransaction {
  type: string;
  referenceNo: string;
  partyName: string;
  description?: string | null;
  amount: number;
  status: string;
  date: string;
}

export interface PendingApproval {
  type: string;
  referenceNo: string;
  partyName: string;
  amount: number;
  status: string;
  date: string;
}

export interface RecentDocument {
  referenceNo: string;
  partyName: string;
  amount: number;
  status: string;
  date: string;
}

export interface DashboardSummary {
  period: DashboardPeriod;
  fromDate: string;
  toDate: string;
  generatedAt: string;
  periodSales: number;
  periodPurchase: number;
  periodExpense: number;
  netProfit: number;
  totalStockValue: number;
  customerReceivable: number;
  supplierPayable: number;
  cashBankBalance: number;
  lowStockItemsCount: number;
  pendingApprovalsCount: number;
  trialBalanceDifference: number;
  budgetUtilization: number;
  todaySales: number;
  todayPurchase: number;
  todayExpense: number;
  todayProfit: number;
  customerDue: number;
  supplierDue: number;
  pendingApprovalExpenses: number;
  monthlyIncomeExpense: DashboardChartPoint[];
  expenseByCategory: DashboardChartPoint[];
  warehouseStockValue: DashboardChartPoint[];
  cashBankTrend: DashboardChartPoint[];
  salesPurchaseTrend: DashboardSalesPurchasePoint[];
  monthlySalesPurchase: DashboardSalesPurchasePoint[];
  topSellingProducts: TopSellingProduct[];
  lowStockAlerts: LowStockAlert[];
  dueAlerts: DueAlert[];
  recentTransactions: RecentTransaction[];
  pendingApprovals: PendingApproval[];
  recentSales: RecentDocument[];
  recentPurchases: RecentDocument[];
}
