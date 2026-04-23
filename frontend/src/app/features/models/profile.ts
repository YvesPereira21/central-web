import { ArticleSimple } from './article';
import { Qualification } from './qualification';
import { User } from './user';

export interface Profile {
    profileId: string,
    name: string,
    bio: string,
    expertise: string,
    level: string,
    reputationScore: number,
    professional: boolean,
    articles: ArticleSimple[],
    qualifications: Qualification[]
}

export interface ProfileCreate {
    name: string,
    bio: string,
    profileType: string,
    user: User
}

export interface ProfileUpdate {
    name: string,
    bio: string,
    expertise: string
}

export interface ProfileSimple {
    profileId: string,
    name: string,
    expertise: string,
    level: string,
    professional: boolean
}
