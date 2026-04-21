import { User } from './user';

export interface Profile {
    profileId: string,
    bio: string,
    username: string,
    expertise: string,
    level: string,
    reputationScore: number,
    professional: boolean
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
