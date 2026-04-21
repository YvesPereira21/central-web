import { Component, inject, OnInit } from '@angular/core';
import { ProfileService } from '../../../features/profiles/services/profile.service';
import { ActivatedRoute } from '@angular/router';
import { Profile } from '../../../features/models/profile';

@Component({
  selector: 'app-profile-detail',
  imports: [],
  templateUrl: './profile-detail.component.html',
  styleUrl: './profile-detail.component.css'
})
export class ProfileDetailComponent implements OnInit {
  private profileService = inject(ProfileService);
  private activatedRoute = inject(ActivatedRoute);

  profile: Profile | null = null;
  errorMessage: string = '';

  ngOnInit(): void {
    const profileId = this.activatedRoute.snapshot.paramMap.get('id');
    if (profileId) {
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
