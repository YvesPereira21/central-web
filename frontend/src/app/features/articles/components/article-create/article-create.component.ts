import { Component, inject, OnInit } from '@angular/core';
import { FormArray, FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ArticleService } from '../../services/article.service';
import { ArticleCreate, ArticleUpdate } from '../../../../core/models/article';
import { TagSelectorComponent } from '../../../../shared/components/tag-selector/tag-selector.component';

@Component({
  selector: 'app-article-create',
  imports: [ReactiveFormsModule, TagSelectorComponent],
  templateUrl: './article-create.component.html',
  styleUrl: './article-create.component.css'
})
export class ArticleCreateComponent implements OnInit {
  private formBuilder = inject(FormBuilder);
  private articleService = inject(ArticleService);
  private activatedRoute = inject(ActivatedRoute);
  private router = inject(Router);

  isSubmiting: boolean = false;
  isEditMode: boolean = false;
  articleId: string | null = null;

  articleForm = this.formBuilder.group({
    title: ['', Validators.required],
    content: ['', Validators.required],
    technologyNames: this.formBuilder.array([])
  })

  get technologyNames() {
    return this.articleForm.get('technologyNames') as FormArray;
  }

  ngOnInit() {
    this.articleId = this.activatedRoute.snapshot.paramMap.get('id');
    if (this.articleId) {
      this.isEditMode = true;
      this.articleService.getArticle(this.articleId).subscribe({
        next: (article) => {
          this.articleForm.patchValue({
            title: article.title,
            content: article.content
          });
          article.tags?.forEach(tag => {
            this.technologyNames.push(this.formBuilder.control(tag.technologyName));
          });
        },
        error: () => {
          alert('Não foi possível carregar os dados do artigo para edição.');
          this.router.navigate(['/articles']);
        }
      });
    }
  }

  onSubmit() {
    if (this.articleForm.invalid || this.technologyNames.length === 0) {
      alert('Preencha o formulário e selecione pelo menos uma tag.');
      return;
    }

    this.isSubmiting = true;

    const formValues = this.articleForm.value;

    if (this.isEditMode) {
      const articleUpdate: ArticleUpdate = {
        title: formValues.title!,
        content: formValues.content!,
        technologyNames: formValues.technologyNames as string[]
      };

      this.articleService.updateArticle(this.articleId!, articleUpdate).subscribe({
        next: (response) => {
          alert('Artigo atualizado com sucesso!');
          this.router.navigate(['/articles', this.articleId]);
        },
        error: (error) => {
          console.log('Erro ao atualizar artigo.', error);
          alert('Não foi possível atualizar o artigo. Tente novamente.');
          this.isSubmiting = false;
        }
      });

    } else {
      const articleCreate: ArticleCreate = {
        title: formValues.title!,
        content: formValues.content!,
        technologyNames: formValues.technologyNames as string[]
      };

      this.articleService.createArticle(articleCreate).subscribe({
        next: (response) => {
          alert('Novo artigo registrado com sucesso!');
          this.clearForm();
        },
        error: (error) => {
          console.log('Erro ao registrar artigo.', error);
          alert('Não foi possível registrar o artigo. Tente novamente.');
          this.isSubmiting = false;
        }
      });
    }
  }

  clearForm() {
    this.articleForm.reset();
    this.technologyNames.clear();
    this.isSubmiting = false;
  }
}
