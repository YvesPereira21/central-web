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
    questionTotalLikes: number,
    liked: boolean,
    saved: boolean
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
    questionTotalLikes: number,
    liked: boolean,
    saved: boolean
}

export interface QuestionUpdate {
    title: string,
    content: string,
    technologyNames: string[]
}

export interface QuestionListSimpleCollection {
    questionId: string,
    title: string,
    createdAt: string,
    solutioned: boolean
}