import { Component, inject, OnInit, signal } from '@angular/core';
import { ProfileService } from '../../../features/profiles/services/profile.service';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Profile } from '../../../features/models/profile';
import { ArticleListComponent } from '../article-list/article-list.component';

@Component({
  selector: 'app-profile-detail',
  imports: [RouterLink, ArticleListComponent],
  templateUrl: './profile-detail.component.html',
  styleUrl: './profile-detail.component.css'
})
export class ProfileDetailComponent implements OnInit {
  private profileService = inject(ProfileService);
  private activatedRoute = inject(ActivatedRoute);

  profileId = signal<string>('');
  profile: Profile | null = null;
  errorMessage: string = '';

  ngOnInit(): void {
    const profileId = this.activatedRoute.snapshot.paramMap.get('id');
    if (profileId) {
      this.profileId.set(profileId);
      this.loadProfile(profileId);
    }
  }

  loadProfile(profileId: string) {
    this.profileService.getProfile(profileId).subscribe({
      next: (response) => {
        this.profile = response;
      },
      error: (error) => {
        this.errorMessage = 'Erro ao carregar perfil.';
        alert(this.errorMessage);
      }
    });
  }
}
