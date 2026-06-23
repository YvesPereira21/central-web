import { Component, inject, OnInit } from '@angular/core';
import { QuestionService } from '../../services/question.service';
import { FormArray, FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { QuestionCreate, QuestionUpdate } from '../../../../core/models/question';
import { TagSelectorComponent } from '../../../../shared/components/tag-selector/tag-selector.component';

@Component({
  selector: 'app-question-create',
  imports: [ReactiveFormsModule, TagSelectorComponent],
  templateUrl: './question-create.component.html',
  styleUrl: './question-create.component.css'
})
export class QuestionCreateComponent implements OnInit {
  private formBuilder = inject(FormBuilder);
  private questionService = inject(QuestionService);
  private activatedRoute = inject(ActivatedRoute);
  private router = inject(Router);

  isSubmiting: boolean = false;
  isEditMode: boolean = false;
  questionId: string | null = null;

  questionForm = this.formBuilder.group({
    title: ['', Validators.required],
    content: ['', Validators.required],
    technologyNames: this.formBuilder.array([])
  })

  get technologyNames() {
    return this.questionForm.get('technologyNames') as FormArray;
  }

  ngOnInit() {
    this.questionId = this.activatedRoute.snapshot.paramMap.get('id');
    if (this.questionId) {
      this.isEditMode = true;
      this.questionService.getQuestion(this.questionId).subscribe({
        next: (question) => {
          this.questionForm.patchValue({
            title: question.title,
            content: question.content
          });
          question.tags?.forEach(tag => {
            this.technologyNames.push(this.formBuilder.control(tag.technologyName));
          });
        },
        error: () => {
          alert('Não foi possível carregar os dados da pergunta para edição.');
          this.router.navigate(['/questions']);
        }
      });
    }
  }

  onSubmit() {
    if (this.questionForm.invalid || this.technologyNames.length === 0) {
      alert('Preencha o formulário e selecione pelo menos uma tag.');
      return;
    }
    
    this.isSubmiting = true;

    const formValues = this.questionForm.value;
    
    if (this.isEditMode) {
      const questionUpdate: QuestionUpdate = {
        title: formValues.title!,
        content: formValues.content!,
        technologyNames: formValues.technologyNames as string[]
      };

      this.questionService.updateQuestion(this.questionId!, questionUpdate).subscribe({
        next: (response) => {
          alert('Pergunta atualizada com sucesso!');
          this.router.navigate(['/questions', this.questionId]);
        },
        error: (error) => {
          console.log('Erro ao atualizar pergunta.', error);
          alert('Não foi possível atualizar a pergunta. Tente novamente.');
          this.isSubmiting = false;
        }
      });
    } else {
      const questionCreate: QuestionCreate = {
        title: formValues.title!,
        content: formValues.content!,
        technologyNames: formValues.technologyNames as string[]
      };

      this.questionService.createQuestion(questionCreate).subscribe({
        next: (response) => {
          alert('Nova pergunta registrada com sucesso!');
          this.clearForm();
        },
        error: (error) => {
          console.log('Erro ao registrar pergunta.', error);
          alert('Não foi possível registrar a pergunta. Tente novamente.');
          this.isSubmiting = false;
        }
      });
    }
  }

  clearForm() {
    this.questionForm.reset();
    this.technologyNames.clear();
    this.isSubmiting = false;
  }
}
