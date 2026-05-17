import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UomSettingComponent } from './uom-setting.component';

describe('UomSettingComponent', () => {
  let component: UomSettingComponent;
  let fixture: ComponentFixture<UomSettingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UomSettingComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UomSettingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
