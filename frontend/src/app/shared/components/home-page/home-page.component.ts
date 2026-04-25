import { Component } from '@angular/core';
import { HeaderComponent } from '../header/header.component';
import { LeftBarComponent } from '../left-bar/left-bar.component';
import { RouterOutlet } from "@angular/router";
import { ArticleListComponent } from '../article-list/article-list.component';

@Component({
  selector: 'app-home-page',
  imports: [RouterOutlet, ArticleListComponent],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.css'
})
export class HomePageComponent {

}
