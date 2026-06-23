export interface Qualification {
    qualificationId: string,
    jobTitle: string,
    experienceLevel: string,
    institution: string,
    startDate: string,
    endDate: string
}

export interface QualificationCreate {
    jobTitle: string,
    experienceLevel: string,
    institution: string,
    startDate: string,
    endDate: string
}
