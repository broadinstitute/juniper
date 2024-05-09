import { KitRequest } from 'src/types/kits'
import { ParticipantTask } from 'src/types/task'
import { PreregistrationResponse, SurveyResponse } from 'src/types/forms'
import { Profile } from 'src/types/address'

export type ParticipantNote = {
    id: string,
    createdAt: number,
    lastUpdatedAt: number,
    enrolleeId: string,
    text: string,
    kitRequestId?: string,
    creatingAdminUserId: string
}

export type Enrollee = {
    id: string
    consented: boolean
    subject: boolean
    createdAt: number
    kitRequests: KitRequest[]
    lastUpdatedAt: number
    participantTasks: ParticipantTask[]
    participantNotes: ParticipantNote[], //todo: optional?
    participantUserId: string
    preRegResponse?: PreregistrationResponse,
    preEnrollmentResponse?: PreregistrationResponse
    profile: Profile
    profileId: string
    shortcode: string
    studyEnvironmentId: string
    surveyResponses: SurveyResponse[]
}

export type HubResponse = {
    enrollee: Enrollee,
    tasks: ParticipantTask[],
    response: object,
    profile: Profile
}
