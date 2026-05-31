import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CollectionService } from '../../../features/collections/services/collection.service';
import { Collection } from '../../../features/models/collection';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-collection-detail',
  imports: [RouterLink, DatePipe],
  templateUrl: './collection-detail.component.html',
  styleUrl: './collection-detail.component.css'
})
export class CollectionDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private collectionService = inject(CollectionService);

  collection = signal<Collection | null>(null);
  isLoading = signal<boolean>(true);
  error = signal<string | null>(null);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.collectionService.getCollectionById(id).subscribe({
        next: (data) => {
          this.collection.set(data);
          this.isLoading.set(false);
        },
        error: (err) => {
          this.error.set('Não foi possível carregar a coleção.');
          this.isLoading.set(false);
        }
      });
    }
  }
}
