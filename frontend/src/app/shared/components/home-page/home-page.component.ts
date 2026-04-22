import { Component } from '@angular/core';
import { HeaderComponent } from '../header/header.component';
import { LeftBarComponent } from '../left-bar/left-bar.component';
import { RouterOutlet } from "@angular/router";

@Component({
  selector: 'app-home-page',
  imports: [HeaderComponent, LeftBarComponent, RouterOutlet],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.css'
})
export class HomePageComponent {

}
