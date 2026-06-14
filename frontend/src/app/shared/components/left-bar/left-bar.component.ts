import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from "@angular/router";
import { ProfileService } from '../../../features/profiles/services/profile.service';
import { AuthenticationService } from '../../../features/authentications/services/authentication.service';

@Component({
  selector: 'app-left-bar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './left-bar.component.html',
  styleUrl: './left-bar.component.css'
})
export class LeftBarComponent implements OnInit {
  private profileService = inject(ProfileService);
  public authService = inject(AuthenticationService);
  
  myProfileId = signal<string | null>(null);

  ngOnInit() {
    if (this.authService.isAuthenticated() && this.authService.isPerson()) {
      this.profileService.getMyProfile().subscribe({
        next: (profile) => this.myProfileId.set(profile.profileId),
        error: () => console.error('Erro ao buscar o perfil do usuário logado')
      });
    }
  }
}
