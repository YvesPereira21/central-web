import { Component, inject, OnInit, signal } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ArticleService } from '../../../features/articles/services/article.service';
import { TagService } from '../../../features/tags/services/tag.service';
import { ArticleCreate } from '../../../features/models/article';
import { Tag } from '../../../features/models/tag';

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

  tags = signal<Tag[]>([]);
  isSubmiting: boolean = false;
  articleForm = this.formBuilder.group({
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
        this.tags.set(data)
      },
      error: (erro) => {
        console.log('Erro ao carregar as tags');
      }
    })
  }

  get techonologyNames() {
    return this.articleForm.get('technologyNames') as FormArray;
  }

  toggleTag(technologyName: string) {
    const index = this.techonologyNames.controls.findIndex(control => control.value === technologyName);

    if (index === -1) {
      this.techonologyNames.controls.push(this.formBuilder.control(technologyName));
    } else {
      this.techonologyNames.removeAt(index);
    }
  }

  isTagSelected(technologyName: string): boolean {
    return this.techonologyNames.controls.some(control => control.value === technologyName);
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
