import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ProfileService } from '../../../features/profiles/services/profile.service';
import { ProfileCreate } from '../../../features/models/profile';
import { Router } from '@angular/router';

@Component({
  selector: 'app-profile-create',
  imports: [ReactiveFormsModule],
  templateUrl: './profile-create.component.html',
  styleUrl: './profile-create.component.css'
})
export class ProfileCreateComponent {
  private formBuilder = inject(FormBuilder);
  private profileService = inject(ProfileService);
  private router = inject(Router);

  isSubmiting: boolean = false;
  selectedFile: File | null = null;
  
  profileForm = this.formBuilder.group({
    name: [''],
    email: [''],
    password: [''],
    bio: [''],
    profileType: ['']
  });

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
    }
  }

  onSubmit() {
    if (this.profileForm.invalid) return alert('Preencha o formulário com informações corretas.');
    this.isSubmiting = true;

    const formValues = this.profileForm.value;
    const profile: ProfileCreate = {
      name: formValues.name!,
      bio: formValues.bio!,
      profileType: formValues.profileType!,
      user: {
        email: formValues.email!,
        password: formValues.password!
      }
    }

    this.profileService.createProfile(profile).subscribe({
      next: (response: any) => {
        if (this.selectedFile && response.profileId) {
          this.profileService.uploadAvatar(response.profileId, this.selectedFile).subscribe({
            next: () => {
              alert('Conta e foto criadas com sucesso! Por favor, faça o login.');
              this.finishSubmit();
            },
            error: () => {
              alert('Conta criada, mas houve um erro ao enviar a foto. Por favor, faça o login.');
              this.finishSubmit();
            }
          });
        } else {
          alert('Conta criada com sucesso! Por favor, faça o login.');
          this.finishSubmit();
        }
      },
      error: (error) => {
        console.log('Erro ao registrar perfil.', error);
        this.clearForm();
      }
    })
  }

  finishSubmit() {
    this.clearForm();
    this.router.navigate(['/login']);
  }

  clearForm() {
    this.profileForm.reset();
    this.selectedFile = null;
    this.isSubmiting = false;
  }
}
