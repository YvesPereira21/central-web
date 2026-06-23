import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CollectionService } from '../../services/collection.service';
import { Collection } from '../../../../core/models/collection';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';

@Component({
  selector: 'app-collection-list',
  imports: [RouterLink, PaginationComponent, ReactiveFormsModule],
  templateUrl: './collection-list.component.html',
  styleUrl: './collection-list.component.css'
})
export class CollectionListComponent implements OnInit {
  private collectionService = inject(CollectionService);
  private formBuilder = inject(FormBuilder);

  collections = signal<Collection[]>([]);
  isLoading = signal<boolean>(true);
  isCreateModalOpen = signal<boolean>(false);
  isCreating = signal<boolean>(false);

  createCollectionForm = this.formBuilder.group({
    name: ['', Validators.required]
  });

  isEmpty = signal<boolean>(false);
  isFirst = signal<boolean>(true);
  isLast = signal<boolean>(false);
  pageSize = signal<number>(0);
  currentPage = signal<number>(0);
  numberOfElements = signal<number>(0);
  totalElements = signal<number>(0);
  totalPages = signal<number>(0);

  ngOnInit(): void {
    this.loadData(0, 10);
  }

  loadData(page: number, size: number) {
    this.isLoading.set(true);
    this.collectionService.getMyCollections(page, size).subscribe({
      next: (response) => {
        this.collections.set(response.content);
        this.isEmpty.set(response.empty);
        this.isFirst.set(response.first);
        this.isLast.set(response.last);
        this.pageSize.set(response.size);
        this.currentPage.set(response.number);
        this.numberOfElements.set(response.numberOfElements);
        this.totalElements.set(response.totalElements);
        this.totalPages.set(response.totalPages);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  openCreateModal() {
    this.createCollectionForm.reset();
    this.isCreateModalOpen.set(true);
  }

  closeCreateModal() {
    this.isCreateModalOpen.set(false);
  }

  submitNewCollection() {
    if (this.createCollectionForm.invalid) return;
    this.isCreating.set(true);
    this.collectionService.createCollection({ name: this.createCollectionForm.value.name! }).subscribe({
      next: () => {
        this.isCreating.set(false);
        this.closeCreateModal();
        this.loadData(this.currentPage(), this.pageSize());
      },
      error: () => {
        this.isCreating.set(false);
        alert('Erro ao criar coleção.');
      }
    });
  }

  confirmDeleteCollection(event: Event, collectionId: string, name: string) {
    event.stopPropagation();
    event.preventDefault();
    
    if (confirm(`Tem certeza que deseja excluir a coleção "${name}"?`)) {
      this.collectionService.deleteCollection(collectionId).subscribe({
        next: () => {
          this.loadData(this.currentPage(), this.pageSize());
        },
        error: () => alert('Erro ao excluir coleção.')
      });
    }
  }
}
