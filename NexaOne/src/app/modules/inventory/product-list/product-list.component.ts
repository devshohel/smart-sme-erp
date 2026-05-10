import { Component, OnInit } from '@angular/core';
import { ProductService } from '../services/product.service';
import { Product } from '../../../models/product.model';

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.css']
})
export class ProductListComponent implements OnInit {
  
  products: Product[] = [];
  selectedProduct: Product = {} as Product; 
  modalTitle: string = '';
  isReadOnly: boolean = false;

  constructor(private productService: ProductService) { }

  ngOnInit(): void {
    this.loadProducts();
  }

  loadProducts(): void {
    this.productService.getAllProducts().subscribe(data => {
      this.products = data;
    });
  }

  viewProduct(product: Product): void {
    this.modalTitle = 'Product Details';
    this.isReadOnly = true;
    this.selectedProduct = { ...product };
  }

  editProduct(product: Product): void {
    this.modalTitle = 'Edit Product';
    this.isReadOnly = false;
    this.selectedProduct = { ...product };
  }

  openAddModal(): void {
    this.modalTitle = 'Add New Product';
    this.isReadOnly = false;
    this.selectedProduct = {} as Product;
  }

  // ডিলিট এপিআই কল করার সঠিক নিয়ম
  deleteProduct(id: number | undefined): void {
    if (!id) return;
    if (confirm('Are you sure you want to delete this product?')) {
      this.productService.deleteProduct(id).subscribe(() => {
        alert('Product deleted successfully');
        this.loadProducts(); // ডিলিট হওয়ার পর লিস্ট আপডেট হবে
      }, error => {
        console.error('Delete error:', error); // আইডি ১ না থাকলে এখানে এরর আসবে
      });
    }
  }

  // সেভ বা আপডেটের জন্য এপিআই কল
  saveProduct(): void {
    if (this.selectedProduct.id) {
      // এখানে ভবিষ্যতে আপডেট এপিআই কল হবে
      console.log('Update logic goes here');
    } else {
      this.productService.createProduct(this.selectedProduct).subscribe(() => {
        alert('Product saved successfully');
        this.loadProducts(); // নতুন ডাটা আসার পর টেবিল রিফ্রেশ হবে
      });
    }
  }
}



/*
import { Component, OnInit } from '@angular/core';
import { ProductService } from '../services/product.service';
import { Product } from '../../../models/product.model';

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.css']
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  selectedProduct: Product = {} as Product;  // মোডাল ডাটা বাইন্ডিংয়ের জন্য
  modalTitle: string = '';                  //[NB:] modal = popup/dialog box (Bootstrap modal) and model = data structure (MVC model)[]
  isReadOnly: boolean = false;              // ভিউ মোডের জন্য

  constructor(private productService: ProductService) { }

  ngOnInit(): void {
    this.loadProducts(); 
  }

  loadProducts(): void { 
    this.productService.getAllProducts().subscribe(data => {
      this.products = data; // ডাটাবেজ থেকে আসা ডাটা এখানে জমা হচ্ছে
    });
  }
  // =========================
  // VIEW FUNCTION (চোখের আইকন)
  // =========================
  viewProduct(product: Product): void{
    this.modalTitle = 'Product Details';
    this.isReadOnly = true;
    this.selectedProduct = { ...product };
  }

  // =========================
  // EDIT FUNCTION (পেন্সিল আইকন)
  // =========================
  editProduct(product: Product): void {
    this.modalTitle = 'Edit Product';
    this.isReadOnly = false;
    this.selectedProduct = { ...product };
  }

  // =========================
  // ADD FUNCTION (New Product বাটন)
  // =========================
  openAddModal(): void {
    this.modalTitle = 'Add New Product';
    this.isReadOnly = false;
    this.selectedProduct = {};      // নতুন অবজেক্ট
  }

  // =========================
  // DELETE FUNCTION (ট্র্যাশ আইকন)
  // =========================
  deleteProduct(id: number | undefined): void {
    if (!id) return;

    if (confirm('Are you sure you want to delete this product?')) {
      console.log('Deleting product ID:', id);
      // এখানে Service.delete API কল হবে
      // Future: this.productService.deleteProduct(id).subscribe(...)
    }
  }

  // =========================
  // SAVE/UPDATE FUNCTION 
  // =========================
  saveProduct(): void {
    if (this.selectedProduct.id) {
      console.log('Updating Product:', this.selectedProduct);
      // update API
    } else {
        console.log('Creating Product:', this.selectedProduct);
      // create API
    }
  }
}
*/