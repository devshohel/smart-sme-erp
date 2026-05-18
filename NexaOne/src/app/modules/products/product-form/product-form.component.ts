import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Brand } from '../../../models/brand.model';
import { ProductCategory } from '../../../models/category.model';
import { Product, ProductType, Status } from '../../../models/product.model';
import { Uom } from '../../../models/uom.model';
import { ProductBrandService } from '../../../services/product-brand.service';
import { ProductCategoryService } from '../../../services/product-category.service';
import { UomService } from '../../../services/uom.service';

@Component({
  selector: 'app-product-form',
  templateUrl: './product-form.component.html',
  styleUrls: ['./product-form.component.css']
})
export class ProductFormComponent implements OnInit, OnChanges {
  @Input() initialValue: Product | null = null;
  @Input() isSubmitting = false;
  @Input() submitLabel = 'Save Product';
  @Input() cancelRoute = '/products/products';
  @Output() formSubmit = new EventEmitter<Product>();
  @Output() cancelClick = new EventEmitter<void>();

  form: FormGroup;
  categories: ProductCategory[] = [];
  brands: Brand[] = [];
  uoms: Uom[] = [];
  loadingOptions = false;

  readonly productTypes: ProductType[] = ['STORABLE', 'SERVICE', 'CONSUMABLE'];
  readonly statusList: Status[] = ['ACTIVE', 'INACTIVE', 'DRAFT'];

  constructor(
    private fb: FormBuilder,
    private categoryService: ProductCategoryService,
    private brandService: ProductBrandService,
    private uomService: UomService
  ) {
    this.form = this.createForm();
  }

  ngOnInit(): void {
    this.loadOptions();
    this.patchFromInput();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['initialValue'] && !changes['initialValue'].firstChange) {
      this.patchFromInput();
    }
  }

  createForm(): FormGroup {
    return this.fb.group({
      id: [null],
      productName: ['', [Validators.required, Validators.maxLength(255)]],
      sku: ['', [Validators.required, Validators.maxLength(100)]],
      barcode: ['', [Validators.maxLength(100)]],
      type: ['STORABLE'],
      purchasePrice: [null, [Validators.required, Validators.min(0)]],
      salePrice: [null, [Validators.required, Validators.min(0)]],
      taxPercentage: [0, [Validators.min(0)]],
      reorderLevel: [0, [Validators.min(0)]],
      imageUrl: ['', [Validators.maxLength(500)]],
      status: ['ACTIVE'],
      categoryId: [null],
      brandId: [null],
      uomId: [null]
    });
  }

  loadOptions(): void {
    this.loadingOptions = true;

    this.categoryService.getAllCategories().subscribe({
      next: (data) => this.categories = data,
      error: () => this.categories = []
    });

    this.brandService.getAllBrands().subscribe({
      next: (data) => this.brands = data,
      error: () => this.brands = []
    });

    this.uomService.getAllUoms().subscribe({
      next: (data) => {
        this.uoms = data;
        this.loadingOptions = false;
      },
      error: () => {
        this.uoms = [];
        this.loadingOptions = false;
      }
    });
  }

  patchFromInput(): void {
    const product = this.initialValue;

    this.form.reset({
      id: product?.id || null,
      productName: product?.productName || '',
      sku: product?.sku || '',
      barcode: product?.barcode || '',
      type: product?.type || 'STORABLE',
      purchasePrice: product?.purchasePrice ?? null,
      salePrice: product?.salePrice ?? null,
      taxPercentage: product?.taxPercentage ?? 0,
      reorderLevel: product?.reorderLevel ?? 0,
      imageUrl: product?.imageUrl || '',
      status: product?.status || 'ACTIVE',
      categoryId: product?.categoryId ?? null,
      brandId: product?.brandId ?? null,
      uomId: product?.uomId ?? null
    });
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.value;
    this.formSubmit.emit({
      id: value.id || undefined,
      productName: value.productName.trim(),
      sku: value.sku.trim(),
      barcode: value.barcode ? value.barcode.trim() : undefined,
      type: value.type,
      purchasePrice: Number(value.purchasePrice),
      salePrice: Number(value.salePrice),
      taxPercentage: value.taxPercentage === null || value.taxPercentage === '' ? null : Number(value.taxPercentage),
      reorderLevel: value.reorderLevel === null || value.reorderLevel === '' ? null : Number(value.reorderLevel),
      imageUrl: value.imageUrl ? value.imageUrl.trim() : undefined,
      status: value.status,
      categoryId: value.categoryId ? Number(value.categoryId) : null,
      brandId: value.brandId ? Number(value.brandId) : null,
      uomId: value.uomId ? Number(value.uomId) : null
    });
  }

  reset(): void {
    this.patchFromInput();
  }

  hasError(controlName: string, errorName: string): boolean {
    const control = this.form.get(controlName);
    return !!control && control.touched && control.hasError(errorName);
  }
}
