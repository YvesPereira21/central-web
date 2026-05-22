import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-pagination',
  imports: [],
  templateUrl: './pagination.component.html',
  styleUrl: './pagination.component.css'
})
export class PaginationComponent {
  @Input() isEmpty: boolean = false;
  @Input() isFirst: boolean = true;
  @Input() isLast: boolean = false;
  @Input() pageSize: number = 0;
  @Input() currentPage: number = 0;
  @Input() numberOfElements: number = 0;
  @Input() totalElements: number = 0;
  @Input() totalPages: number = 0;

  @Output() pageChange = new EventEmitter<number>();

  onPageChange(page: number) {
    this.pageChange.emit(page);
  }
}
