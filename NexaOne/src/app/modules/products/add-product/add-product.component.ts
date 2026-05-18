import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { Product } from '../../../models/product.model';
import { ProductService } from '../../../services/product.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';

@Component({
  selector: 'app-add-product',
  templateUrl: './add-product.component.html',
  styleUrls: ['./add-product.component.css']
})
export class AddProductComponent {
  isSubmitting = false;
  submitError = '';
  submitSuccess = '';

  constructor(
    private productService: ProductService,
    private router: Router
  ) {}

  saveProduct(payload: Product): void {
    this.isSubmitting = true;
    this.submitError = '';
    this.submitSuccess = '';

    this.productService.createProduct(payload).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.submitSuccess = 'Product created successfully.';
      },
      error: (error) => {
        this.isSubmitting = false;
        this.submitError = extractApiErrorMessage(error, 'Product could not be saved right now.');
        debugApiError('AddProductComponent.saveProduct', error);
      }
    });
  }

  goToProductList(): void {
    this.router.navigate(['/products/products']);
  }
}
