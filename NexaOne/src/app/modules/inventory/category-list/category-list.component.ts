import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProductCategoryService } from '../../../services/product-category.service';
import { ProductCategory, Status } from '../../../models/category.model';

declare var bootstrap: any;

@Component({
  selector: 'app-category-list',
  templateUrl: './category-list.component.html',
  styleUrls: ['./category-list.component.css']
})
export class CategoryListComponent implements OnInit {

  categories: ProductCategory[] = [];
  loading: boolean = false;
  categoryForm!: FormGroup;
  isEditMode: boolean = false;
  categoryModal: any;

  statusList = Object.values(Status);

  constructor(
    private categoryService: ProductCategoryService,
    private fb: FormBuilder
  ) {
    this.initForm();
  }

  ngOnInit(): void {
    this.loadCategories();
  }

  initForm(): void {
    this.categoryForm = this.fb.group({
      id: [null],
      code: ['', Validators.required],
      categoryName: ['', Validators.required],
      description: [''],
      status: ['ACTIVE', Validators.required],
      parentCategory: [null]
    });
  }

  loadCategories(): void {
    this.loading = true;
    this.categoryService.getAllCategories().subscribe({
      next: (data) => {
        this.categories = data;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
      }
    });
  }

  addCategory(): void {
    this.isEditMode = false;
    this.categoryForm.reset();
    this.categoryForm.patchValue({ status: 'ACTIVE' });
    this.openModal();
  }

  editCategory(cat: ProductCategory): void {
    this.isEditMode = true;
    this.categoryForm.patchValue({
      ...cat,
      parentCategory: cat.parentCategory || null
    });
    this.openModal();
  }

  onSubmit(): void {
    if (this.categoryForm.invalid) return;

    this.loading = true;

    const formData: ProductCategory = this.categoryForm.value;

    this.categoryService.saveCategory(formData).subscribe({
      next: () => {
        this.loading = false;
        this.closeModal();
        this.loadCategories();
        alert('Category saved successfully!');
      },
      error: (err) => {
        console.error(err);
        this.loading = false;
      }
    });
  }

  deleteCategory(id?: number): void {
    if (!id) return;

    if (confirm('Are you sure to delete this category?')) {
      this.categoryService.deleteCategory(id).subscribe(() => {
        this.loadCategories();
      });
    }
  }

  openModal(): void {
    this.categoryModal = new bootstrap.Modal(
      document.getElementById('categoryModal')
    );
    this.categoryModal.show();
  }

  closeModal(): void {
    const modalEl = document.getElementById('categoryModal');
    const instance = bootstrap.Modal.getInstance(modalEl);
    if (instance) instance.hide();
  }
}