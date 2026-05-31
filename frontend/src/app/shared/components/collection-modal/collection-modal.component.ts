import { Component, EventEmitter, inject, Input, OnInit, Output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CollectionService } from '../../../features/collections/services/collection.service';
import { Collection } from '../../../features/models/collection';

@Component({
  selector: 'app-collection-modal',
  imports: [ReactiveFormsModule],
  templateUrl: './collection-modal.component.html',
  styleUrl: './collection-modal.component.css'
})
export class CollectionModalComponent implements OnInit {
  @Input({ required: true }) isOpen!: boolean;
  @Input() articleId?: string;
  @Input() questionId?: string;
  @Output() closeModal = new EventEmitter<void>();
  @Output() savedSuccess = new EventEmitter<void>();

  private collectionService = inject(CollectionService);
  private formBuilder = inject(FormBuilder);

  collections = signal<Collection[]>([]);
  isCreating = signal<boolean>(false);
  isLoading = signal<boolean>(true);

  newCollectionForm = this.formBuilder.group({
    name: ['', Validators.required]
  });

  ngOnInit() {
    this.loadCollections();
  }

  loadCollections() {
    this.isLoading.set(true);
    this.collectionService.getMyCollections(0, 50).subscribe({
      next: (data) => {
        this.collections.set(data.content);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  createCollection() {
    if (this.newCollectionForm.invalid) return;
    this.isCreating.set(true);
    this.collectionService.createCollection({ name: this.newCollectionForm.value.name! }).subscribe({
      next: (newColl) => {
        this.collections.update(c => [...c, newColl]);
        this.newCollectionForm.reset();
        this.isCreating.set(false);
      },
      error: () => this.isCreating.set(false)
    });
  }

  addToCollection(collectionId: string) {
    if (this.articleId) {
      this.collectionService.addArticleToCollection(collectionId, this.articleId).subscribe({
        next: () => {
          alert('Artigo salvo com sucesso!');
          this.savedSuccess.emit();
          this.close();
        },
        error: () => alert('Erro ao salvar artigo.')
      });
    } else if (this.questionId) {
      this.collectionService.addQuestionToCollection(collectionId, this.questionId).subscribe({
        next: () => {
          alert('Pergunta salva com sucesso!');
          this.savedSuccess.emit();
          this.close();
        },
        error: () => alert('Erro ao salvar pergunta.')
      });
    }
  }

  close() {
    this.closeModal.emit();
  }
}
