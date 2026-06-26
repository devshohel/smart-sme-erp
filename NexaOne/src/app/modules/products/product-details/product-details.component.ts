import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ActivityLog } from '../../auth/auth.model';
import { AuthService } from '../../auth/auth.service';
import { Product } from '../../../models/product.model';
import { ProductService } from '../../../services/product.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-product-details',
  templateUrl: './product-details.component.html',
  styleUrls: ['./product-details.component.css']
})
export class ProductDetailsComponent implements OnInit {
  product: Product | null = null;
  history: ActivityLog[] = [];
  loading = false;
  historyLoading = false;
  errorMessage = '';
  historyErrorMessage = '';
  activeTab: 'details' | 'history' = 'details';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) {
      this.errorMessage = 'Invalid product id.';
      return;
    }

    this.loading = true;
    this.productService.getProductById(id).subscribe({
      next: (data) => {
        this.product = data;
        this.loading = false;
        this.loadHistory(data.id);
      },
      error: (error) => {
        this.errorMessage = extractApiErrorMessage(error, 'Product details could not be loaded.');
        this.loading = false;
        debugApiError('ProductDetailsComponent.loadProduct', error);
      }
    });
  }

  get imageUrl(): string {
    return this.productService.resolveImageUrl(this.product?.imageUrl);
  }

  backToList(): void {
    this.router.navigate(['/products/products']);
  }

  editProduct(): void {
    if (this.product?.id) {
      this.router.navigate(['/products/edit-product', this.product.id]);
    }
  }

  showTab(tab: 'details' | 'history'): void {
    this.activeTab = tab;
  }

  private loadHistory(productId?: number): void {
    if (!productId || !this.canViewHistory()) {
      return;
    }
    this.historyLoading = true;
    this.historyErrorMessage = '';
    this.authService.getActivityHistory('Products', productId).subscribe({
      next: history => {
        this.history = history;
        this.historyLoading = false;
      },
      error: error => {
        this.historyErrorMessage = extractApiErrorMessage(error, 'Product history could not be loaded.');
        this.historyLoading = false;
        debugApiError('ProductDetailsComponent.loadHistory', error);
      }
    });
  }

  canViewHistory(): boolean {
    return this.authService.hasAnyPermission(['AUDIT_VIEW', 'ACTIVITY_VIEW', 'ACTIVITY_LOG_VIEW']);
  }
}
