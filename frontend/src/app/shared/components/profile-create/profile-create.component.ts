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
  profileForm = this.formBuilder.group({
    name: [''],
    email: [''],
    password: [''],
    bio: [''],
    profileType: ['']
  });

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
      next: (response) => {
        alert('Conta criada com sucesso! Por favor, faça o login.');
        this.clearForm();
        this.router.navigate(['/login']);
      },
      error: (error) => {
        console.log('Erro ao registrar artigo.');
        this.clearForm();
      }
    })
  }

  clearForm() {
    this.profileForm.reset();
    this.isSubmiting = false;
  }
}
