import React, {
  createContext,
  useContext
} from 'react'
import {
  EnvironmentName,
  StudyEnvParams
} from 'src/types/study'
import { HubResponse } from 'src/types/user'
import {
  AddressValidationResult,
  MailingAddress
} from 'src/types/address'
import { SurveyResponseWithJustification } from 'src/types/forms'
import { SystemSettings } from 'src/types/maintenance'
import { ParticipantFile } from 'src/types/participantFile'

export type ImageUrlFunc = (cleanFileName: string, version: number) => string
export type SubmitMailingListContactFunc = (name: string, email: string) => Promise<object>
export type GetLanguageTextsFunc = (selectedLanguage: string,
                                    portalShortcode?: string,
                                    environmentName?: EnvironmentName) => Promise<Record<string, string>>
export type UpdateSurveyResponseFunc = ({
  studyEnvParams, stableId, version, enrolleeShortcode, response, taskId,
  alertErrors
}: {
  studyEnvParams: StudyEnvParams, stableId: string, version: number,
  response: SurveyResponseWithJustification, enrolleeShortcode: string, taskId: string, alertErrors?: boolean
}) => Promise<HubResponse>
export type LoadSystemSettingsFunc = () => Promise<SystemSettings>
export type ValidateAddressFunc = (address: MailingAddress) => Promise<AddressValidationResult>

export type ListParticipantFilesFunc = ({ studyEnvParams, enrolleeShortcode }: {
  studyEnvParams: StudyEnvParams,
  enrolleeShortcode: string
}) => Promise<ParticipantFile[]>

export type UploadParticipantFileFunc = ({ studyEnvParams, enrolleeShortcode, file }: {
  studyEnvParams: StudyEnvParams,
  enrolleeShortcode: string,
  file: File
}) => Promise<ParticipantFile>

export type DownloadParticipantFileFunc = ({ studyEnvParams, enrolleeShortcode, fileName }: {
  studyEnvParams: StudyEnvParams,
  enrolleeShortcode: string,
  fileName: string
}) => Promise<Response>

export type DeleteParticipantFileFunc = ({ studyEnvParams, enrolleeShortcode, fileName }: {
  studyEnvParams: StudyEnvParams,
  enrolleeShortcode: string,
  fileName: string
}) => Promise<Response>

/**
 * represents a minimal set of api functions needed to make the participant ui functional outside of the
 * main participant ui app.
 */
export type ApiContextT = {
  getImageUrl: ImageUrlFunc,
  submitMailingListContact: SubmitMailingListContactFunc,
  getLanguageTexts: GetLanguageTextsFunc,
  updateSurveyResponse: UpdateSurveyResponseFunc,
  validateAddress: ValidateAddressFunc,
  listParticipantFiles: ListParticipantFilesFunc,
  uploadParticipantFile: UploadParticipantFileFunc,
  downloadParticipantFile: DownloadParticipantFileFunc,
  deleteParticipantFile: DeleteParticipantFileFunc
  loadSystemSettings: LoadSystemSettingsFunc
}

export const emptyApi: ApiContextT = {
  getImageUrl: () => '',
  submitMailingListContact: () => Promise.resolve({}),
  getLanguageTexts: () => Promise.resolve({}),
  updateSurveyResponse: () => Promise.resolve({} as HubResponse),
  validateAddress: () => Promise.resolve({} as AddressValidationResult),
  listParticipantFiles: () => Promise.resolve([]),
  uploadParticipantFile: () => Promise.resolve({} as ParticipantFile),
  downloadParticipantFile: () => Promise.resolve({} as Response),
  deleteParticipantFile: () => Promise.resolve({} as Response),
  loadSystemSettings: () => Promise.resolve({} as SystemSettings)
}

const ApiContext = createContext<ApiContextT>(emptyApi)
/** helper function for using the api context */
export const useApiContext = () => {
  return useContext(ApiContext)
}

/**
 * provider for an Api object -- this enables the same participant UI components to be plugged into different API
 * endpoints depending on whether they are being rendered in the participant UI, or in preview-mode on the admin tool
 * Unlike many other providers, this does not do anything asynchronously or make server requests, it's just a container
 */
export const ApiProvider = ({ api, children }: {api: ApiContextT, children: React.ReactNode}) => {
  return <ApiContext.Provider value={api}>
    {children}
  </ApiContext.Provider>
}
