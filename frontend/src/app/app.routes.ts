import { Routes } from '@angular/router';
import { ArticleCreateComponent } from './shared/components/article-create/article-create.component';
import { QuestionCreateComponent } from './shared/components/question-create/question-create.component';
import { ProfileCreateComponent } from './shared/components/profile-create/profile-create.component';
import { ArticleListComponent } from './shared/components/article-list/article-list.component';
import { QuestionListComponent } from './shared/components/question-list/question-list.component';
import { ArticleDetailComponent } from './shared/components/article-detail/article-detail.component';
import { QuestionDetailComponent } from './shared/components/question-detail/question-detail.component';
import { ProfileDetailComponent } from './shared/components/profile-detail/profile-detail.component';
import { QualificationCreateComponent } from './shared/components/qualification-create/qualification-create.component';
import { HomePageComponent } from './shared/components/home-page/home-page.component';
import { LoginComponent } from './shared/components/login/login.component';
import { authGuard } from './core/guards/auth.guard';
import { AppComponent } from './app.component';

export const routes: Routes = [
    { path: '', component: HomePageComponent, canActivate: [authGuard] },
    { path: 'login', component: LoginComponent },
    { path: 'create-profile', component: ProfileCreateComponent },
    { path: 'create-article', component: ArticleCreateComponent, canActivate: [authGuard] },
    { path: 'create-question', component: QuestionCreateComponent, canActivate: [authGuard] },
    { path: 'create-qualification', component: QualificationCreateComponent, canActivate: [authGuard] },
    { path: 'articles', component: ArticleListComponent, canActivate: [authGuard] },
    { path: 'questions', component: QuestionListComponent, canActivate: [authGuard] },
    { path: 'articles/:id', component: ArticleDetailComponent, canActivate: [authGuard] },
    { path: 'questions/:id', component: QuestionDetailComponent, canActivate: [authGuard] },
    { path: 'profiles/:id', component: ProfileDetailComponent, canActivate: [authGuard] }
];
