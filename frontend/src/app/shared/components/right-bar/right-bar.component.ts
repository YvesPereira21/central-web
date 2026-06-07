import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TagService } from '../../../features/tags/services/tag.service';
import { Tag } from '../../../features/models/tag';

@Component({
  selector: 'app-right-bar',
  imports: [],
  templateUrl: './right-bar.component.html',
  styleUrl: './right-bar.component.css'
})
export class RightBarComponent implements OnInit {
  private tagService = inject(TagService);
  private router = inject(Router);
  private activatedRoute = inject(ActivatedRoute);

  tags = signal<Tag[]>([]);
  selectedTags = signal<string[]>([]);
  isLoading = signal<boolean>(true);

  isTagModalOpen = signal<boolean>(false);
  searchTagQuery = signal<string>('');

  filteredTags = computed(() => {
    const query = this.searchTagQuery().toLowerCase();
    return this.tags().filter(tag => tag.technologyName.toLowerCase().includes(query));
  });

  ngOnInit() {
    this.activatedRoute.queryParamMap.subscribe(params => {
      const tagsParam = params.get('tags');
      if (tagsParam) {
        this.selectedTags.set(tagsParam.split(','));
      } else {
        this.selectedTags.set([]);
      }
    });

    this.loadTags();
  }

  loadTags() {
    this.tagService.getAllTags().subscribe({
      next: (response) => {
        this.tags.set(response.content);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        console.error('Erro ao carregar tags');
      }
    });
  }

  openTagModal() {
    this.isTagModalOpen.set(true);
  }

  closeTagModal() {
    this.isTagModalOpen.set(false);
    this.searchTagQuery.set('');
  }

  updateSearchQuery(event: Event) {
    const target = event.target as HTMLInputElement;
    this.searchTagQuery.set(target.value);
  }

  toggleTag(tagName: string) {
    const currentTags = this.selectedTags();
    let newTags: string[];

    if (currentTags.includes(tagName)) {
      newTags = currentTags.filter(t => t !== tagName);
    } else {
      newTags = [...currentTags, tagName];
    }

    this.selectedTags.set(newTags);

    const tagsParam = newTags.length > 0 ? newTags.join(',') : null;

    this.router.navigate([], {
      relativeTo: this.activatedRoute,
      queryParams: { tags: tagsParam },
      queryParamsHandling: 'merge'
    });
  }
}
