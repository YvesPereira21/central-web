import { ProfileSimple } from './profile';
import { Tag } from './tag';

export interface Article {
    articleId: string,
    title: string,
    content: string,
    createdAt: string,
    tags: Tag[],
    profile: ProfileSimple,
    articleTotalLikes: number
}

export interface ArticleCreate {
    title: string,
    content: string,
    technologyNames: string[]
}

export interface ArticleUpdate {
    title: string,
    content: string,
    technologyNames: string[]
}

export interface ArticleSimple {
    articleId: string,
    title: string,
    content: string,
    tags: Tag[],
    articleTotalLikes: number
}