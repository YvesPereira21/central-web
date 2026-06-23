import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AnswersQuestionComponent } from './answers-question.component';

describe('AnswersQuestionComponent', () => {
  let component: AnswersQuestionComponent;
  let fixture: ComponentFixture<AnswersQuestionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AnswersQuestionComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AnswersQuestionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
