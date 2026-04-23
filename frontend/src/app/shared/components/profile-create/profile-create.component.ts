import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ProfileService } from '../../../features/profiles/services/profile.service';
import { ProfileCreate } from '../../../features/models/profile';

@Component({
  selector: 'app-profile-create',
  imports: [ReactiveFormsModule],
  templateUrl: './profile-create.component.html',
  styleUrl: './profile-create.component.css'
})
export class ProfileCreateComponent {
  private formBuilder = inject(FormBuilder);
  private profileService = inject(ProfileService);

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
        console.log('Conta criada com sucesso!')
        this.clearForm();
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
