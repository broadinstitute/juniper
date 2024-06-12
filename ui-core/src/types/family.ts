import { Enrollee } from 'src/types/user'

export type Family = {
    id: string
    createdAt: number
    lastUpdatedAt: number
    probandEnrolleeId: string
    shortcode: string
    studyEnvironmentId: string
    members?: Enrollee[]
    proband?: Enrollee
    familyMembers?: Enrollee[]
}
