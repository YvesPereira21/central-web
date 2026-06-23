import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CommentsAnswerComponent } from './comments-answer.component';

describe('CommentsAnswerComponent', () => {
  let component: CommentsAnswerComponent;
  let fixture: ComponentFixture<CommentsAnswerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommentsAnswerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CommentsAnswerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
