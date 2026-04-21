import { ProfileSimple } from "./profile"

export interface Answer {
    answerId: string,
    content: string,
    accepted: boolean,
    createdAt: string,
    profile: ProfileSimple,
    answerTotalLikes: number
}

export interface AnswerCreate {
    content: string,
    questionId: string
}

export interface AnswerAccepted {
    articleId: string,
    accepted: boolean
}
