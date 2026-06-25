import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ProductCategory } from '../../../models/category.model';
import { Status } from '../../../models/product.model';
import { ProductCategoryService } from '../../../services/product-category.service';
import { debugApiError, extractApiErrorMessage } from '../../../shared/utils/api-error.util';
import { AuthService } from '../../auth/auth.service';

declare var bootstrap: any;

@Component({
  selector: 'app-category-list',
  templateUrl: './category-list.component.html',
  styleUrls: ['./category-list.component.css']
})
export class CategoryListComponent implements OnInit {
  categories: ProductCategory[] = [];
  showDeleted = false;
  loading = false;
  categoryForm: FormGroup;
  isEditMode = false;
  submitError = '';

  readonly statusList: Status[] = ['ACTIVE', 'INACTIVE', 'DRAFT'];

  constructor(
    private categoryService: ProductCategoryService,
    private fb: FormBuilder,
    private authService: AuthService
  ) {
    this.categoryForm = this.fb.group({
      id: [null],
      code: ['', [Validators.required, Validators.maxLength(50)]],
      categoryName: ['', [Validators.required, Validators.maxLength(255)]],
      description: ['', [Validators.maxLength(1000)]],
      status: ['ACTIVE', Validators.required],
      parentCategoryId: [null]
    });
  }

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading = true;
    const request$ = this.showDeleted ? this.categoryService.getDeletedCategories() : this.categoryService.getAllCategories();
    request$.subscribe({
      next: (data) => {
        this.categories = data;
        this.loading = false;
      },
      error: (error) => {
        this.categories = [];
        this.loading = false;
        debugApiError('CategoryListComponent.loadCategories', error);
      }
    });
  }

  toggleDeletedView(): void {
    this.showDeleted = !this.showDeleted;
    this.loadCategories();
  }

  get availableParentCategories(): ProductCategory[] {
    const currentId = this.categoryForm.get('id')?.value;
    return this.categories.filter(category => category.id !== currentId);
  }

  addCategory(): void {
    this.isEditMode = false;
    this.submitError = '';
    this.categoryForm.reset({
      id: null,
      code: '',
      categoryName: '',
      description: '',
      status: 'ACTIVE',
      parentCategoryId: null
    });
    this.openModal();
  }

  editCategory(category: ProductCategory): void {
    if (this.showDeleted) {
      return;
    }
    this.isEditMode = true;
    this.submitError = '';
    this.categoryForm.reset({
      id: category.id || null,
      code: category.code,
      categoryName: category.categoryName,
      description: category.description || '',
      status: category.status,
      parentCategoryId: category.parentCategoryId || null
    });
    this.openModal();
  }

  onSubmit(): void {
    if (this.categoryForm.invalid) {
      this.categoryForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.submitError = '';

    this.categoryService.saveCategory(this.categoryForm.value).subscribe({
      next: () => {
        this.loading = false;
        this.closeModal();
        this.loadCategories();
      },
      error: (error) => {
        this.loading = false;
        this.submitError = extractApiErrorMessage(error, 'Category could not be saved.');
        debugApiError('CategoryListComponent.onSubmit', error);
      }
    });
  }

  deleteCategory(id?: number): void {
    if (!id) {
      return;
    }

    if (confirm('Are you sure to delete this category?')) {
      this.categoryService.deleteCategory(id).subscribe(() => this.loadCategories());
    }
  }

  restoreCategory(id?: number): void {
    if (!id || !confirm('Restore this category?')) {
      return;
    }
    this.categoryService.restoreCategory(id).subscribe(() => this.loadCategories());
  }

  openModal(): void {
    const modal = new bootstrap.Modal(document.getElementById('categoryModal'));
    modal.show();
  }

  closeModal(): void {
    const modalEl = document.getElementById('categoryModal');
    const instance = bootstrap.Modal.getInstance(modalEl);
    if (instance) {
      instance.hide();
    }
  }

  hasError(controlName: string, errorName: string): boolean {
    const control = this.categoryForm.get(controlName);
    return !!control && control.touched && control.hasError(errorName);
  }

  hasPermission(permission: string): boolean {
    return this.authService.hasPermission(permission);
  }
}
