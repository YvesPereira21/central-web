import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProfileService } from '../../services/profile.service';
import { ProfileUpdate } from '../../../../core/models/profile';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-profile-edit',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './profile-edit.component.html',
  styleUrl: './profile-edit.component.css'
})
export class ProfileEditComponent implements OnInit {
  private formBuilder = inject(FormBuilder);
  private profileService = inject(ProfileService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  isSubmiting = false;
  selectedFile: File | null = null;
  profileId: string | null = null;
  profileForm = this.formBuilder.group({
    name: ['', Validators.required],
    bio: ['', Validators.required]
  });

  ngOnInit(): void {
    this.profileId = this.route.snapshot.paramMap.get('id');
    if (this.profileId) {
      this.profileService.getProfile(this.profileId).subscribe({
        next: (profile) => {
          this.profileForm.patchValue({
            name: profile.name,
            bio: profile.bio
          });
        },
        error: () => {
          alert('Erro ao carregar perfil.');
          this.router.navigate(['/']);
        }
      });
    }
  }

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  onSubmit() {
    if (this.profileForm.invalid || !this.profileId) return alert('Preencha o formulário corretamente.');
    this.isSubmiting = true;

    const profileUpdate: ProfileUpdate = {
      name: this.profileForm.value.name!,
      bio: this.profileForm.value.bio!
    };

    this.profileService.updateProfile(this.profileId, profileUpdate).subscribe({
      next: () => {
        if (this.selectedFile) {
          this.profileService.uploadAvatar(this.profileId!, this.selectedFile).subscribe({
            next: () => {
              alert('Perfil e foto atualizados com sucesso!');
              this.finishSubmit();
            },
            error: () => {
              alert('Perfil atualizado, mas houve um erro ao enviar a nova foto.');
              this.finishSubmit();
            }
          });
        } else {
          alert('Perfil atualizado com sucesso!');
          this.finishSubmit();
        }
      },
      error: () => {
        alert('Erro ao atualizar perfil.');
        this.isSubmiting = false;
      }
    });
  }

  finishSubmit() {
    this.isSubmiting = false;
    this.router.navigate(['/profiles', this.profileId]);
  }
}
