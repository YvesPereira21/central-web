import { Component, inject, signal } from '@angular/core';
import { QuestionService } from '../../../features/questions/services/question.service';
import { FormArray, FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { QuestionCreate } from '../../../features/models/question';
import { Tag } from '../../../features/models/tag';
import { TagService } from '../../../features/tags/services/tag.service';

@Component({
  selector: 'app-question-create',
  imports: [ReactiveFormsModule],
  templateUrl: './question-create.component.html',
  styleUrl: './question-create.component.css'
})
export class QuestionCreateComponent {
  private formBuilder = inject(FormBuilder);
  private questionService = inject(QuestionService);
  private tagService = inject(TagService);

  tags = signal<Tag[]>([]);
  isSubmiting: boolean = false;
  questionForm = this.formBuilder.group({
    title: [''],
    content: [''],
    technologyNames: this.formBuilder.array([])
  })

  ngOnInit(): void {
    this.loadAllTags();
  }

  loadAllTags() {
    this.tagService.getAllTags().subscribe({
      next: (data) => {
        this.tags.set(data);
      },
      error: (erro) => {
        console.log('Erro procurar as tags');
      }
    })
  }

  get technologyNames() {
    return this.questionForm.get('technologyNames') as FormArray;
  }

  toggleTag(technologyName: string) {
    const index = this.technologyNames.controls.findIndex(control => control.value === technologyName);

    if (index === -1) {
      this.technologyNames.controls.push(this.formBuilder.control(technologyName));
    } else {
      this.technologyNames.removeAt(index);
    }
  }

  isTagSelected(technologyName: string): boolean {
    return this.technologyNames.controls.some(control => control.value === technologyName);
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
