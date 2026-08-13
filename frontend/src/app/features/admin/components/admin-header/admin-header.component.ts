import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthenticationService } from '../../../authentications/services/authentication.service';

@Component({
  selector: 'app-admin-header',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './admin-header.component.html',
  styleUrl: './admin-header.component.css'
})
export class AdminHeaderComponent {
  public authService = inject(AuthenticationService);
}
