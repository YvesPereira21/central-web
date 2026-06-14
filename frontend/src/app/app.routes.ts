import { Routes } from '@angular/router';
import { ArticleCreateComponent } from './shared/components/article-create/article-create.component';
import { QuestionCreateComponent } from './shared/components/question-create/question-create.component';
import { ProfileCreateComponent } from './shared/components/profile-create/profile-create.component';
import { ProfileEditComponent } from './shared/components/profile-edit/profile-edit.component';
import { ArticleListComponent } from './shared/components/article-list/article-list.component';
import { QuestionListComponent } from './shared/components/question-list/question-list.component';
import { ArticleDetailComponent } from './shared/components/article-detail/article-detail.component';
import { QuestionDetailComponent } from './shared/components/question-detail/question-detail.component';
import { ProfileDetailComponent } from './shared/components/profile-detail/profile-detail.component';
import { QualificationCreateComponent } from './shared/components/qualification-create/qualification-create.component';
import { HomePageComponent } from './shared/components/home-page/home-page.component';
import { LoginComponent } from './shared/components/login/login.component';
import { roleGuard } from './core/guards/role.guard';
import { CollectionListComponent } from './shared/components/collection-list/collection-list.component';
import { CollectionDetailComponent } from './shared/components/collection-detail/collection-detail.component';

export const routes: Routes = [
    { path: '', component: HomePageComponent, canActivate: [roleGuard] },
    { path: 'login', component: LoginComponent },
    { path: 'create-profile', component: ProfileCreateComponent },
    { path: 'edit-profile/:id', component: ProfileEditComponent, canActivate: [roleGuard] },
    { path: 'create-article', component: ArticleCreateComponent, canActivate: [roleGuard], data: { roles: ['PERSON'] } },
    { path: 'edit-article/:id', component: ArticleCreateComponent, canActivate: [roleGuard], data: { roles: ['PERSON'] } },
    { path: 'create-question', component: QuestionCreateComponent, canActivate: [roleGuard], data: { roles: ['PERSON'] } },
    { path: 'edit-question/:id', component: QuestionCreateComponent, canActivate: [roleGuard], data: { roles: ['PERSON'] } },
    { path: 'create-qualification', component: QualificationCreateComponent, canActivate: [roleGuard], data: { roles: ['PERSON'] } },
    { path: 'articles', component: ArticleListComponent, canActivate: [roleGuard] },
    { path: 'articles/:id/profile', component: ArticleListComponent, canActivate: [roleGuard] },
    { path: 'questions', component: QuestionListComponent, canActivate: [roleGuard] },
    { path: 'questions/:id/profile', component: QuestionListComponent, canActivate: [roleGuard] },
    { path: 'articles/:id', component: ArticleDetailComponent, canActivate: [roleGuard] },
    { path: 'questions/:id', component: QuestionDetailComponent, canActivate: [roleGuard] },
    { path: 'profiles/:id', component: ProfileDetailComponent, canActivate: [roleGuard] },
    { path: 'collections', component: CollectionListComponent, canActivate: [roleGuard] },
    { path: 'collections/:id', component: CollectionDetailComponent, canActivate: [roleGuard] },
];
