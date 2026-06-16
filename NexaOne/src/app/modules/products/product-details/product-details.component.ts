import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
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
  loading = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private productService: ProductService
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
}
