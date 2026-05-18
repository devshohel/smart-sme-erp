import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Product } from '../../../models/product.model';
import { ProductService } from '../../../services/product.service';

@Component({
  selector: 'app-edit-product',
  templateUrl: './edit-product.component.html',
  styleUrls: ['./edit-product.component.css']
})
export class EditProductComponent implements OnInit {
  product: Product | null = null;
  loading = false;
  submitting = false;
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
    this.loadProduct(id);
  }

  loadProduct(id: number): void {
    this.loading = true;
    this.productService.getProductById(id).subscribe({
      next: (data) => {
        this.product = data;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Product could not be loaded.';
        this.loading = false;
      }
    });
  }

  saveProduct(payload: Product): void {
    this.submitting = true;
    this.errorMessage = '';
    this.productService.saveProduct(payload).subscribe({
      next: () => {
        this.submitting = false;
        this.router.navigate(['/products/products']);
      },
      error: () => {
        this.submitting = false;
        this.errorMessage = 'Product could not be saved.';
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/products/products']);
  }
}
