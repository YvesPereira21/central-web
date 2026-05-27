import { Component, inject, Input, OnInit, signal, computed } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { TagService } from '../../../features/tags/services/tag.service';
import { Tag } from '../../../features/models/tag';

@Component({
  selector: 'app-tag-selector',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './tag-selector.component.html',
})
export class TagSelectorComponent implements OnInit {
  @Input() technologyNames!: FormArray;

  private formBuilder = inject(FormBuilder);
  private tagService = inject(TagService);

  tags = signal<Tag[]>([]);
  isTagModalOpen = signal<boolean>(false);
  searchTagQuery = signal<string>('');

  filteredTags = computed(() => {
    const query = this.searchTagQuery().toLowerCase();
    return this.tags().filter(tag => tag.technologyName.toLowerCase().includes(query));
  });

  ngOnInit(): void {
    this.loadAllTags();
  }

  loadAllTags() {
    this.tagService.getAllTags().subscribe({
      next: (data) => {
        this.tags.set(data.content);
      },
      error: (erro) => {
        console.log('Erro ao procurar as tags');
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

  toggleTag(technologyName: string) {
    const index = this.technologyNames.controls.findIndex(control => control.value === technologyName);

    if (index === -1) {
      this.technologyNames.push(this.formBuilder.control(technologyName));
    } else {
      this.technologyNames.removeAt(index);
    }
  }

  isTagSelected(technologyName: string): boolean {
    return this.technologyNames.controls.some(control => control.value === technologyName);
  }
}
