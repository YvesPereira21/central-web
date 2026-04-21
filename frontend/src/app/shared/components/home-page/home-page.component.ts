import { Component } from '@angular/core';
import { HeaderComponent } from '../header/header.component';
import { LeftBarComponent } from '../left-bar/left-bar.component';

@Component({
  selector: 'app-home-page',
  imports: [HeaderComponent, LeftBarComponent],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.css'
})
export class HomePageComponent {

}
