import { ArticleSimple } from './article';
import { Qualification } from './qualification';
import { User } from './user';

export interface Profile {
    profileId: string,
    bio: string,
    username: string,
    expertise: string,
    level: string,
    reputationScore: number,
    professional: boolean,
    articles: ArticleSimple[],
    qualifications: Qualification[]
}

export interface ProfileCreate {
    bio: string,
    profileType: string,
    expertise: string,
    level: string,
    user: User
}

export interface ProfileUpdate {
    bio: string,
    expertise: string
}

export interface ProfileSimple {
    profileId: string,
    username: string,
    expertise: string,
    level: string,
    professional: boolean
}
