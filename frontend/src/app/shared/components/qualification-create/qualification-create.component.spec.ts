import { ComponentFixture, TestBed } from '@angular/core/testing';

import { QualificationCreateComponent } from './qualification-create.component';

describe('QualificationCreateComponent', () => {
  let component: QualificationCreateComponent;
  let fixture: ComponentFixture<QualificationCreateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [QualificationCreateComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(QualificationCreateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
