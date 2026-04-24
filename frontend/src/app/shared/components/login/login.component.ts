import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthenticationService } from '../../../features/authentications/services/authentication.service';
import { LoginRequest } from '../../../features/models/authentication';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private authenticationService = inject(AuthenticationService);
  private formBuilder = inject(FormBuilder);
  private router = inject(Router)

  isSubmiting: boolean = false;
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
      next: (response) => {
        console.log('Login realizado com sucesso!')
        this.clearForm();
        this.router.navigate([''])
      },
      error: (erro) => {
        console.log('Erro ao realizar login.');
        this.clearForm();
      }
    });
  }

  clearForm() {
    this.loginForm.reset();
    this.isSubmiting = false;
  }
}
