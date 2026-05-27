import { Component, inject } from '@angular/core';
import { QuestionService } from '../../../features/questions/services/question.service';
import { FormArray, FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { QuestionCreate } from '../../../features/models/question';
import { TagSelectorComponent } from '../tag-selector/tag-selector.component';

@Component({
  selector: 'app-question-create',
  imports: [ReactiveFormsModule, TagSelectorComponent],
  templateUrl: './question-create.component.html',
  styleUrl: './question-create.component.css'
})
export class QuestionCreateComponent {
  private formBuilder = inject(FormBuilder);
  private questionService = inject(QuestionService);

  isSubmiting: boolean = false;

  questionForm = this.formBuilder.group({
    title: ['', Validators.required],
    content: ['', Validators.required],
    technologyNames: this.formBuilder.array([])
  })

  get technologyNames() {
    return this.questionForm.get('technologyNames') as FormArray;
  }

  onSubmit() {
    if (this.questionForm.invalid || this.technologyNames.length === 0) {
      alert('Preencha o formulário e selecione pelo menos uma tag.');
      return;
    }
    
    this.isSubmiting = true;

    const formValues = this.questionForm.value;
    const question: QuestionCreate = {
      title: formValues.title!,
      content: formValues.content!,
      technologyNames: formValues.technologyNames as string[]
    }

    this.questionService.createQuestion(question).subscribe({
      next: (response) => {
        console.log('Nova pergunta registrada com sucesso!')
        this.clearForm();
      },
      error: (error) => {
        console.log('Erro ao registrar pergunta.', error);
        alert('Não foi possível registrar a pergunta. Tente novamente.');
        this.isSubmiting = false;
      }
    })
  }

  clearForm() {
    this.questionForm.reset();
    this.technologyNames.clear();
    this.isSubmiting = false;
  }
}
