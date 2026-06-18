export interface DashboardSummary {
  todaySales: number;
  todayPurchase: number;
  todayExpense: number;
  todayProfit: number;
  totalStockValue: number;
  customerDue: number;
  supplierDue: number;
  netProfit: number;
  totalCustomers: number;
  totalSuppliers: number;
  lowStockItemsCount: number;
  thisMonthProfit: number;
  expenseTaxAmount: number;
  postedExpenses: number;
  pendingApprovalExpenses: number;
  reversedExpenses: number;
  monthlyExpenseTrend: MonthlyExpense[];
  monthlySalesPurchase: MonthlySalesPurchase[];
  topSellingProducts: TopSellingProduct[];
  lowStockAlerts: LowStockAlert[];
  dueAlerts: DueAlert[];
  recentTransactions: RecentTransaction[];
}

export interface MonthlySalesPurchase {
  month: string;
  sales: number;
  purchase: number;
  profit: number;
}

export interface MonthlyExpense {
  month: string;
  amount: number;
  taxAmount: number;
}

export interface TopSellingProduct {
  productId: number | null;
  productName: string;
  quantity: number;
  amount: number;
}

export interface LowStockAlert {
  productId: number | null;
  productName: string;
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
