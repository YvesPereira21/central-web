export interface Qualification {
    qualificationId: string;
    jobTitle: string;
    experienceLevel: string;
    institution: string;
    startDate: string;
    endDate: string;
    verified?: boolean;
    profileId?: string;
    profileName?: string;
}

export interface QualificationCreate {
    jobTitle: string;
    experienceLevel: string;
    institution: string;
    startDate: string;
    endDate: string;
}
