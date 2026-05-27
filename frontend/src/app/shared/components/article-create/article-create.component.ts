import { Component, inject } from '@angular/core';
import { FormArray, FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { ArticleService } from '../../../features/articles/services/article.service';
import { ArticleCreate } from '../../../features/models/article';
import { TagSelectorComponent } from '../tag-selector/tag-selector.component';

@Component({
  selector: 'app-article-create',
  imports: [ReactiveFormsModule, TagSelectorComponent],
  templateUrl: './article-create.component.html',
  styleUrl: './article-create.component.css'
})
export class ArticleCreateComponent {
  private formBuilder = inject(FormBuilder);
  private articleService = inject(ArticleService);

  isSubmiting: boolean = false;

  articleForm = this.formBuilder.group({
    title: ['', Validators.required],
    content: ['', Validators.required],
    technologyNames: this.formBuilder.array([])
  })

  get technologyNames() {
    return this.articleForm.get('technologyNames') as FormArray;
  }

  onSubmit() {
    if (this.articleForm.invalid || this.technologyNames.length === 0) {
      alert('Preencha o formulário e selecione pelo menos uma tag.');
      return;
    }
    
    this.isSubmiting = true;

    const formValues = this.articleForm.value;
    const article: ArticleCreate = {
      title: formValues.title!,
      content: formValues.content!,
      technologyNames: formValues.technologyNames as string[]
    }

    this.articleService.createArticle(article).subscribe({
      next: (response) => {
        console.log('Novo artigo registrado com sucesso!')
        this.clearForm();
      },
      error: (error) => {
        console.log('Erro ao registrar artigo.', error);
        alert('Não foi possível registrar o artigo. Tente novamente.');
        this.isSubmiting = false;
      }
    })
  }

  clearForm() {
    this.articleForm.reset();
    this.technologyNames.clear();
    this.isSubmiting = false;
  }
}
