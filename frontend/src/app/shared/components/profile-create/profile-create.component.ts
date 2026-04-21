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
    username: [''],
    email: [''],
    password: [''],
    bio: [''],
    profileType: [''],
    expertise: [''],
    level: ['']
  });

  onSubmit() {
    if (this.profileForm.invalid) return alert('Preencha o formulário com informações corretas.');
    this.isSubmiting = true;

    const formValues = this.profileForm.value;
    const profile: ProfileCreate = {
      bio: formValues.bio!,
      profileType: formValues.profileType!,
      expertise: formValues.expertise!,
      level: formValues.level!,
      user: {
        username: formValues.username!,
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
