import { Component, inject, OnInit, signal } from '@angular/core';
import { ProfileService } from '../../../features/profiles/services/profile.service';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Profile } from '../../../features/models/profile';
import { ArticleListComponent } from '../article-list/article-list.component';
import { QuestionListComponent } from '../question-list/question-list.component';
import { AuthenticationService } from '../../../features/authentications/services/authentication.service';
import { LevelStageComponent } from '../level-stage/level-stage.component';

@Component({
  selector: 'app-profile-detail',
  imports: [RouterLink, ArticleListComponent, QuestionListComponent, LevelStageComponent],
  templateUrl: './profile-detail.component.html',
  styleUrl: './profile-detail.component.css'
})
export class ProfileDetailComponent implements OnInit {
  private profileService = inject(ProfileService);
  private activatedRoute = inject(ActivatedRoute);
  private authenticationService = inject(AuthenticationService);

  profileId = signal<string>('');
  isOwner = signal<boolean>(false);
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
        
        const userIsOwner = this.authenticationService.isOwner(this.profile.userId);
        if (userIsOwner) {
          this.isOwner.set(true);
        }
      },
      error: (error) => {
        this.errorMessage = 'Erro ao carregar perfil.';
        alert(this.errorMessage);
      }
    });
  }
}
