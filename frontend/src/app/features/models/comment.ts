import { Profile } from "./profile";

export interface Comment {
    commentId: string;
    content: string;
    createdAt: string;
    profile: Profile;
}

export interface CommentCreate {
    content: string;
    answerId: string;
}

export interface CommentUpdate {
    content: string;
}
