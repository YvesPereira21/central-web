import { ProfileSimple } from './profile';
import { Tag } from './tag';
import { Answer } from './answer';

export interface Question {
    questionId: string,
    title: string,
    content: string,
    published: boolean,
    solutioned: boolean,
    createdAt: string,
    profile: ProfileSimple,
    tags: Tag[],
    answers: Answer[],
    questionTotalLikes: number
}

export interface QuestionCreate {
    title: string,
    content: string,
    technologyNames: string[]
}

export interface QuestionList {
    questionId: string,
    title: string,
    content: string,
    published: boolean,
    solutioned: boolean,
    createdAt: string,
    profile: ProfileSimple,
    tags: Tag[],
    questionTotalLikes: number
}

export interface QuestionUpdate {
    title: string,
    content: string,
    technologyNames: string[]
}
