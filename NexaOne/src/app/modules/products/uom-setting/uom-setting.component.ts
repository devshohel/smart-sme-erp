import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Status } from '../../../models/product.model';
import { Uom } from '../../../models/uom.model';
import { UomService } from '../../../services/uom.service';

declare var bootstrap: any;

@Component({
  selector: 'app-uom-setting',
  templateUrl: './uom-setting.component.html',
  styleUrls: ['./uom-setting.component.css']
})
export class UomSettingComponent implements OnInit {
  uoms: Uom[] = [];
  uomForm: FormGroup;
  loading = false;
  isEditMode = false;
  submitError = '';

  readonly statusList: Status[] = ['ACTIVE', 'INACTIVE', 'DRAFT'];

  constructor(
    private uomService: UomService,
    private fb: FormBuilder
  ) {
    this.uomForm = this.fb.group({
      id: [null],
      code: ['', [Validators.required, Validators.maxLength(50)]],
      name: ['', [Validators.required, Validators.maxLength(255)]],
      type: ['', [Validators.maxLength(100)]],
      conversionFactor: [1, [Validators.min(0.000001)]],
      status: ['ACTIVE', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadUoms();
  }

  loadUoms(): void {
    this.loading = true;
    this.uomService.getAllUoms().subscribe({
      next: (data) => {
        this.uoms = data;
        this.loading = false;
      },
      error: () => {
        this.uoms = [];
        this.loading = false;
      }
    });
  }

  addUom(): void {
    this.isEditMode = false;
    this.submitError = '';
    this.uomForm.reset({
      id: null,
      code: '',
      name: '',
      type: '',
      conversionFactor: 1,
      status: 'ACTIVE'
    });
    this.openModal();
  }

  editUom(uom: Uom): void {
    this.isEditMode = true;
    this.submitError = '';
    this.uomForm.reset({
      id: uom.id || null,
      code: uom.code,
      name: uom.name,
      type: uom.type || '',
      conversionFactor: uom.conversionFactor ?? 1,
      status: uom.status
    });
    this.openModal();
  }

  onSubmit(): void {
    if (this.uomForm.invalid) {
      this.uomForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.submitError = '';

    this.uomService.saveUom(this.uomForm.value).subscribe({
      next: () => {
        this.loading = false;
        this.closeModal();
        this.loadUoms();
      },
      error: () => {
        this.loading = false;
        this.submitError = 'UOM could not be saved.';
      }
    });
  }

  deleteUom(id?: number): void {
    if (!id) {
      return;
    }

    if (confirm('Are you sure you want to delete this UOM?')) {
      this.uomService.deleteUom(id).subscribe(() => this.loadUoms());
    }
  }

  openModal(): void {
    const modal = new bootstrap.Modal(document.getElementById('uomModal'));
    modal.show();
  }

  closeModal(): void {
    const modalEl = document.getElementById('uomModal');
    const instance = bootstrap.Modal.getInstance(modalEl);
    if (instance) {
      instance.hide();
    }
  }

  hasError(controlName: string, errorName: string): boolean {
    const control = this.uomForm.get(controlName);
    return !!control && control.touched && control.hasError(errorName);
  }
}
