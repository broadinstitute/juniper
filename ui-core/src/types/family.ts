import {
  Enrollee,
  EnrolleeRelation
} from 'src/types/user'

export type Family = {
    id: string
    createdAt: number
    lastUpdatedAt: number
    probandEnrolleeId: string
    shortcode: string
    studyEnvironmentId: string
    members?: Enrollee[]
    proband?: Enrollee
    relations?: EnrolleeRelation[]
}

export type FamilyEnrollee = {
    familyId: string
    enrolleeId: string
    createdAt: number
}
