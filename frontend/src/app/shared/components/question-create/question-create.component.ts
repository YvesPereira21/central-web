import { Component, inject } from '@angular/core';
import { QuestionService } from '../../../features/questions/services/question.service';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { QuestionCreate } from '../../../features/models/question';

@Component({
  selector: 'app-question-create',
  imports: [ReactiveFormsModule],
  templateUrl: './question-create.component.html',
  styleUrl: './question-create.component.css'
})
export class QuestionCreateComponent {
  private formBuilder = inject(FormBuilder);
  private questionService = inject(QuestionService);

  isSubmiting: boolean = false;
  questionForm = this.formBuilder.group({
    title: [''],
    content: [''],
    technologyNames: this.formBuilder.array([])
  })

  ngOnInit(): void {
    throw new Error('Method not implemented.');
  }

  onSubmit() {
    if (this.questionForm.invalid) return alert('Preencha o formulário com informações corretas.');
    this.isSubmiting = true;

    const formValues = this.questionForm.value;
    const question: QuestionCreate = {
      title: formValues.title!,
      content: formValues.content!,
      technologyNames: formValues.technologyNames as string[]
    }

    this.questionService.createQuestion(question).subscribe({
      next: (response) => {
        console.log('Nova pergunta registrado com sucesso!')
        this.clearForm();
      },
      error: (error) => {
        console.log('Erro ao registrar pergunta.');
        this.clearForm();
      }
    })
  }

  clearForm() {
    this.questionForm.reset();
    this.isSubmiting = false;
  }
}
