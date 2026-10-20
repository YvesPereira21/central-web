import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthenticationService } from '../../services/authentication.service';
import { LoginRequest } from '../../../../core/models/authentication';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private authenticationService = inject(AuthenticationService);
  private formBuilder = inject(FormBuilder);

  isSubmiting: boolean = false;
  showPassword = false;
  loginForm = this.formBuilder.group({
    email: ['', Validators.email],
    password: ['']
  })

  onSubmit() {
    if (this.loginForm.invalid) return alert('Email ou senha inválida');
    this.isSubmiting = true;

    const formValues = this.loginForm.value;
    const login: LoginRequest = {
      email: formValues.email!,
      password: formValues.password!
    }

    this.authenticationService.login(login).subscribe({
      next: () => {
        alert('Login realizado com sucesso!');
        this.clearForm();
      },
      error: () => {
        alert('Erro ao realizar login.');
        this.clearForm();
      }
    });
  }

  clearForm() {
    this.loginForm.reset();
    this.isSubmiting = false;
  }
}
