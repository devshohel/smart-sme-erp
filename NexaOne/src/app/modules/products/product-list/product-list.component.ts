import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Product } from '../../../models/product.model';
import { ProductService } from '../../../services/product.service';

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.css']
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  loading = false;
  submitError = '';

  constructor(
    private productService: ProductService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.loading = true;
    this.productService.getAllProducts().subscribe({
      next: (data) => {
        this.products = data;
        this.loading = false;
      },
      error: () => {
        this.products = [];
        this.loading = false;
      }
    });
  }

  viewProduct(product: Product): void {
    if (product.id) {
      this.router.navigate(['/products/details', product.id]);
    }
  }

  editProduct(product: Product): void {
    if (product.id) {
      this.router.navigate(['/products/edit-product', product.id]);
    }
  }

  deleteProduct(id?: number): void {
    if (!id) {
      return;
    }

    if (confirm('Are you sure you want to delete this product?')) {
      this.productService.deleteProduct(id).subscribe({
        next: () => this.loadProducts(),
        error: () => this.submitError = 'Delete request failed.'
      });
    }
  }
}
