import { Component, inject, OnInit, signal, OnDestroy } from '@angular/core';
import { RouterLink, Router } from "@angular/router";
import { ReactiveFormsModule, FormControl } from '@angular/forms';
import { AuthenticationService } from '../../../features/authentications/services/authentication.service';
import { ProfileService } from '../../../features/profiles/services/profile.service';
import { environment } from '../../../environments/environment';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-header',
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit, OnDestroy {
  private router = inject(Router);
  public authService = inject(AuthenticationService);
  private profileService = inject(ProfileService);

  public mediaUrl = environment.mediaUrl;

  searchControl = new FormControl('');
  userName = signal<string | null>(null);
  userPhotoUrl = signal<string | null>(null);
  private profileSubscription?: Subscription;

  ngOnInit() {
    this.loadProfile();
    this.profileSubscription = this.profileService.profileUpdated$.subscribe(() => {
      this.loadProfile();
    });
  }

  loadProfile() {
    if (this.authService.isAuthenticated() && this.authService.isPerson()) {
      this.profileService.getMyProfile().subscribe({
        next: (profile) => {
          // Extrai o primeiro nome do nome completo
          const firstName = profile.name.split(' ')[0];
          this.userName.set(firstName);
          if (profile.photoUrl) {
            this.userPhotoUrl.set(profile.photoUrl);
          }
        },
        error: () => console.error('Erro ao buscar o nome do usuário para o header')
      });
    }
  }

  ngOnDestroy() {
    this.profileSubscription?.unsubscribe();
  }

  onSearch() {
    const query = this.searchControl.value?.trim();
    if (query) {
      this.router.navigate(['/'], { queryParams: { search: query }, queryParamsHandling: 'merge' });
    } else {
      this.router.navigate(['/'], { queryParams: { search: null }, queryParamsHandling: 'merge' });
    }
  }
}
