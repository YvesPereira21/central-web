import { Component, inject } from '@angular/core';
import { RouterOutlet, Router } from '@angular/router';
import { HeaderComponent } from './shared/components/header/header.component';
import { LeftBarComponent } from './shared/components/left-bar/left-bar.component';
import { AuthenticationService } from './features/authentications/services/authentication.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, HeaderComponent, LeftBarComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  authenticationService = inject(AuthenticationService);
  private router = inject(Router);

  title = 'frontend';

  get showSidebars(): boolean {
    const url = this.router.url;
    return !url.includes('/create-article') && 
           !url.includes('/create-question') && 
           !url.includes('/create-qualification') &&
           !url.includes('/edit-article') &&
           !url.includes('/edit-question');
  }
}
