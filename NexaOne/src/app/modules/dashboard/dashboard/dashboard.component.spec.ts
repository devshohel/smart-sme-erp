import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';
import { AuthService } from '../../auth/auth.service';
import { DashboardService } from '../dashboard.service';
import { DashboardComponent } from './dashboard.component';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DashboardComponent],
      imports: [FormsModule, RouterTestingModule],
      providers: [
        {
          provide: DashboardService,
          useValue: {
            getSummary: () => of({
              period: 'month',
              fromDate: '',
              toDate: '',
              generatedAt: '',
              periodSales: 0,
              periodPurchase: 0,
              periodExpense: 0,
              netProfit: 0,
              totalStockValue: 0,
              customerReceivable: 0,
              supplierPayable: 0,
              cashBankBalance: 0,
              lowStockItemsCount: 0,
              pendingApprovalsCount: 0,
              trialBalanceDifference: 0,
              budgetUtilization: 0,
              todaySales: 0,
              todayPurchase: 0,
              todayExpense: 0,
              todayProfit: 0,
              customerDue: 0,
              supplierDue: 0,
              pendingApprovalExpenses: 0,
              monthlyIncomeExpense: [],
              expenseByCategory: [],
              warehouseStockValue: [],
              cashBankTrend: [],
              salesPurchaseTrend: [],
              monthlySalesPurchase: [],
              topSellingProducts: [],
              lowStockAlerts: [],
              dueAlerts: [],
              recentTransactions: [],
              pendingApprovals: [],
              recentSales: [],
              recentPurchases: []
            })
          }
        },
        {
          provide: AuthService,
          useValue: {
            hasPermission: () => true,
            hasAnyPermission: () => true
          }
        }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
