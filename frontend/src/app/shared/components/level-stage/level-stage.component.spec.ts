import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LevelStageComponent } from './level-stage.component';

describe('LevelStageComponent', () => {
  let component: LevelStageComponent;
  let fixture: ComponentFixture<LevelStageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LevelStageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LevelStageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
