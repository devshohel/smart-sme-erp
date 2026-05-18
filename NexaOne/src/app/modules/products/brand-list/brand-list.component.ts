import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Brand } from '../../../models/brand.model';
import { Status } from '../../../models/product.model';
import { ProductBrandService } from '../../../services/product-brand.service';

declare var bootstrap: any;

@Component({
  selector: 'app-brand-list',
  templateUrl: './brand-list.component.html',
  styleUrls: ['./brand-list.component.css']
})
export class BrandListComponent implements OnInit {
  brands: Brand[] = [];
  brandForm: FormGroup;
  loading = false;
  isEditMode = false;
  submitError = '';

  readonly statusList: Status[] = ['ACTIVE', 'INACTIVE', 'DRAFT'];

  constructor(
    private brandService: ProductBrandService,
    private fb: FormBuilder
  ) {
    this.brandForm = this.fb.group({
      id: [null],
      code: ['', [Validators.required, Validators.maxLength(50)]],
      brandName: ['', [Validators.required, Validators.maxLength(255)]],
      status: ['ACTIVE', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadBrands();
  }

  loadBrands(): void {
    this.loading = true;
    this.brandService.getAllBrands().subscribe({
      next: (data) => {
        this.brands = data;
        this.loading = false;
      },
      error: () => {
        this.brands = [];
        this.loading = false;
      }
    });
  }

  addBrand(): void {
    this.isEditMode = false;
    this.submitError = '';
    this.brandForm.reset({
      id: null,
      code: '',
      brandName: '',
      status: 'ACTIVE'
    });
    this.openModal();
  }

  editBrand(brand: Brand): void {
    this.isEditMode = true;
    this.submitError = '';
    this.brandForm.reset({
      id: brand.id || null,
      code: brand.code,
      brandName: brand.brandName,
      status: brand.status
    });
    this.openModal();
  }

  onSubmit(): void {
    if (this.brandForm.invalid) {
      this.brandForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.submitError = '';

    this.brandService.saveBrand(this.brandForm.value).subscribe({
      next: () => {
        this.loading = false;
        this.closeModal();
        this.loadBrands();
      },
      error: () => {
        this.loading = false;
        this.submitError = 'Brand could not be saved.';
      }
    });
  }

  deleteBrand(id?: number): void {
    if (!id) {
      return;
    }

    if (confirm('Are you sure you want to delete this brand?')) {
      this.brandService.deleteBrand(id).subscribe(() => this.loadBrands());
    }
  }

  openModal(): void {
    const modal = new bootstrap.Modal(document.getElementById('brandModal'));
    modal.show();
  }

  closeModal(): void {
    const modalEl = document.getElementById('brandModal');
    const instance = bootstrap.Modal.getInstance(modalEl);
    if (instance) {
      instance.hide();
    }
  }

  hasError(controlName: string, errorName: string): boolean {
    const control = this.brandForm.get(controlName);
    return !!control && control.touched && control.hasError(errorName);
  }
}
