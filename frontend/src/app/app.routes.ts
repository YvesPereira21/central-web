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

export const routes: Routes = [
    { path: '', component: HomePageComponent },
    { path: 'create-profile', component: ProfileCreateComponent },
    { path: 'create-article', component: ArticleCreateComponent },
    { path: 'create-question', component: QuestionCreateComponent },
    { path: 'create-qualification', component: QualificationCreateComponent },
    { path: 'articles', component: ArticleListComponent },
    { path: 'questions', component: QuestionListComponent },
    { path: 'articles/:id', component: ArticleDetailComponent },
    { path: 'questions/:id', component: QuestionDetailComponent },
    { path: 'profiles/:id', component: ProfileDetailComponent }
];
