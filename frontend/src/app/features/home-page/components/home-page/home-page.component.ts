import { Component } from '@angular/core';
import { HeaderComponent } from '../../../../shared/components/header/header.component';
import { LeftBarComponent } from '../../../../shared/components/left-bar/left-bar.component';
import { RouterOutlet } from "@angular/router";
import { ArticleListComponent } from '../../../../features/articles/components/article-list/article-list.component';

@Component({
  selector: 'app-home-page',
  imports: [RouterOutlet, ArticleListComponent],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.css'
})
export class HomePageComponent {

}
