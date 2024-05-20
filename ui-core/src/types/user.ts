import { KitRequest } from 'src/types/kits'
import { ParticipantTask } from 'src/types/task'
import { PreregistrationResponse, SurveyResponse } from 'src/types/forms'
import { MailingAddress } from 'src/types/address'

export type ParticipantNote = {
    id: string
    createdAt: number
    lastUpdatedAt: number
    enrolleeId: string
    text: string
    kitRequestId?: string
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
    participantNotes: ParticipantNote[]
    participantUserId: string
    preRegResponse?: PreregistrationResponse
    preEnrollmentResponse?: PreregistrationResponse
    profile: Profile
    profileId: string
    shortcode: string
    studyEnvironmentId: string
    surveyResponses: SurveyResponse[]
}

export type HubResponse = {
    enrollee: Enrollee
    tasks: ParticipantTask[]
    response: SurveyResponse
    profile: Profile
}

export type Profile = {
    id?: string
    givenName?: string,
    familyName?: string,
    contactEmail?: string,
    doNotEmail?: boolean,
    doNotEmailSolicit?: boolean,
    mailingAddress?: MailingAddress,
    phoneNumber?: string,
    birthDate?: number[],
    sexAtBirth?: string,
    preferredLanguage?: string,
}

