import { Component, inject, OnInit, signal } from '@angular/core';
import { QuestionService } from '../../../features/questions/services/question.service';
import { Question, QuestionList } from '../../../features/models/question';

@Component({
  selector: 'app-question-list',
  imports: [],
  templateUrl: './question-list.component.html',
  styleUrl: './question-list.component.css'
})
export class QuestionListComponent implements OnInit {
  private questionService = inject(QuestionService);

  questions = signal<QuestionList[]>([]);

  ngOnInit(): void {
    this.loadAllQuestions();
  }

  loadAllQuestions() {
    this.questionService.getAllPublishedQuestions().subscribe({
      next: (data) => {
        this.questions.set(data);
      },
      error: (error) => {
        alert("Não foi possível encontrar todos os pontos");
      }
    })
  }
}
