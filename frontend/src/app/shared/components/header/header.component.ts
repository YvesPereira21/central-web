import { Component, inject } from '@angular/core';
import { RouterLink, Router } from "@angular/router";
import { ReactiveFormsModule, FormControl } from '@angular/forms';

@Component({
  selector: 'app-header',
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  private router = inject(Router);

  searchControl = new FormControl('');

  onSearch() {
    const query = this.searchControl.value?.trim();
    if (query) {
      this.router.navigate(['/'], { queryParams: { search: query }, queryParamsHandling: 'merge' });
    } else {
      this.router.navigate(['/'], { queryParams: { search: null }, queryParamsHandling: 'merge' });
    }
  }
}
