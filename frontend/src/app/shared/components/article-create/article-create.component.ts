import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ArticleService } from '../../../features/articles/services/article.service';
import { TagService } from '../../../features/tags/services/tag.service';
import { ArticleCreate } from '../../../features/models/article';

@Component({
  selector: 'app-article-create',
  imports: [ReactiveFormsModule],
  templateUrl: './article-create.component.html',
  styleUrl: './article-create.component.css'
})
export class ArticleCreateComponent implements OnInit {
  private formBuilder = inject(FormBuilder);
  private articleService = inject(ArticleService);
  private tagService = inject(TagService);

  isSubmiting: boolean = false;
  articleForm = this.formBuilder.group({
    title: [''],
    content: [''],
    technologyNames: this.formBuilder.array([])
  })

  ngOnInit(): void {
    throw new Error('Method not implemented.');
  }

  onSubmit() {
    if (this.articleForm.invalid) return alert('Preencha o formulário com informações corretas.');
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
        console.log('Erro ao registrar artigo.');
        this.clearForm();
      }
    })
  }

  clearForm() {
    this.articleForm.reset();
    this.isSubmiting = false;
  }
}
