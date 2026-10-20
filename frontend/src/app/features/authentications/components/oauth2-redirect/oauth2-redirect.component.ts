import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from '../../services/authentication.service';

@Component({
  selector: 'app-oauth2-redirect',
  standalone: true,
  template: `
    <div class="min-h-[calc(100vh-10rem)] flex items-center justify-center text-white">
      <div class="bg-[#161B22] p-8 rounded-xl border border-[#30363d] text-center shadow-lg">
        <div class="animate-spin rounded-full h-10 w-10 border-b-2 border-emerald-500 mx-auto mb-4"></div>
        <p class="text-gray-300 font-medium">Autenticando via Google, aguarde...</p>
      </div>
    </div>
  `
})
export class Oauth2RedirectComponent implements OnInit {
  private router = inject(Router);
  private authService = inject(AuthenticationService);

  ngOnInit(): void {
    this.authService.isAuthenticated.set(true);
    this.authService.userRole.set('PERSON');
    this.router.navigate(['/articles']);
  }
}
