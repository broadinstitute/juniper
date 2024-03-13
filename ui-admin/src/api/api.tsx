import {
  AddressValidationResult,
  AlertTrigger,
  ConsentForm,
  ConsentResponse,
  EnvironmentName,
  MailingAddress,
  ParticipantDashboardAlert,
  ParticipantTask,
  ParticipantTaskType,
  Portal,
  PortalEnvironment,
  PortalEnvironmentConfig,
  PreregistrationResponse,
  SiteContent,
  Study,
  StudyEnvironmentConfig,
  StudyEnvironmentConsent,
  StudyEnvironmentSurvey,
  Survey,
  SurveyResponse,
  Trigger
} from '@juniper/ui-core'
import { FacetOption, FacetType, FacetValue, facetValuesToString } from './enrolleeSearch'
import { StudyEnvParams } from '../study/StudyEnvironmentRouter'
import queryString from 'query-string'
import { AdminUser, NewAdminUser } from './adminUser'

export type {
  Answer,
  AddressValidationResult,
  ConsentForm,
  ConsentResponse,
  HtmlPage,
  HtmlSection,
  LocalSiteContent,
  NavbarItem,
  NavbarItemInternal,
  NavbarItemInternalAnchor,
  NavbarItemMailingList,
  NavbarItemExternal,
  MailingAddress,
  Trigger,
  ParticipantTask,
  Portal,
  PortalEnvironment,
  PortalEnvironmentConfig,
  PortalStudy,
  PreEnrollmentResponse,
  PreregistrationResponse,
  SectionConfig,
  SectionType,
  SiteContent,
  Study,
  StudyEnvironment,
  StudyEnvironmentConfig,
  StudyEnvironmentConsent,
  StudyEnvironmentSurvey,
  Survey,
  SurveyResponse,
  VersionedForm
} from '@juniper/ui-core'

export type StudyEnvironmentUpdate = {
  id: string,
  preEnrollSurveyId: string
}

export type EnrolleeSearchFacet = {
  keyName: string,
  category: string,
  label: string,
  facetType: FacetType,
  entities: FacetOption[]
  options: FacetOption[]
}

export type EnrolleeSearchResult = {
  enrollee: Enrollee,
  profile: Profile,
  participantUser: {
    lastLogin: number,
    username: string
  }
  mostRecentKitStatus: string | null
}

export type Enrollee = {
  id: string,
  shortcode: string,
  createdAt: number,
  participantUserId: string,
  surveyResponses: SurveyResponse[],
  consentResponses: ConsentResponse[],
  preRegResponse?: PreregistrationResponse,
  preEnrollmentResponse?: PreregistrationResponse,
  participantTasks: ParticipantTask[],
  participantNotes: ParticipantNote[],
  kitRequests: KitRequest[],
  consented: boolean,
  profile: Profile
}

export type Profile = {
  givenName: string,
  familyName: string,
  contactEmail: string,
  doNotEmail: boolean,
  doNotEmailSolicit: boolean,
  mailingAddress: MailingAddress,
  phoneNumber: string,
  birthDate?: number[]
}

export type ProfileUpdateDto = {
  justification: string,
  profile: Profile
}

export type Notification = {
  id: string,
  triggerId: string,
  deliveryStatus: string,
  deliveryType: string,
  sentTo: string,
  createdAt: number,
  lastUpdatedAt: number,
  retries: number,
  trigger?: Trigger
}

export type Event = {
  id: string,
  createdAt: number,
  lastUpdatedAt: number,
  eventClass: string,
  studyEnvironmentId: string,
  portalEnvironmentId: string,
  enrolleeId: string
}

export type DataChangeRecord = {
  id: string,
  createdAt: number,
  modelName: string,
  fieldName?: string,
  oldValue: string,
  newValue: string,
  responsibleUserId?: string,
  responsibleAdminUserId?: string,
  justification?: string
}

export type KitType = {
  id: string,
  name: string,
  displayName: string,
  description: string
}

export type PepperKit = {
  kitId: string,
  currentStatus: string,
  labelDate: string,
  scanDate: string,
  receiveDate: string,
  trackingNumber: string,
  returnTrackingNumber: string,
  errorMessage: string
}

export type AdminTaskListDto = {
  tasks: AdminTask[]
  enrollees: Enrollee[]
  participantNotes: ParticipantNote[]
}

export type AdminTaskStatus = 'NEW' | 'COMPLETE' | 'REJECTED'

export type AdminTask = {
  id: string
  createdAt: number
  completedAt?: number
  status: AdminTaskStatus
  studyEnvironmentId: string
  enrolleeId?: string
  participantNoteId?: string
  creatingAdminUserId?: string
  assignedAdminUserId?: string
  description?: string
  dispositionNote?: string
}

export type SiteMediaMetadata = {
  id: string,
  createdAt: number,
  cleanFileName: string,
  version: number
}

export type KitRequest = {
  id: string,
  createdAt: number,
  kitType: KitType,
  status: string,
  sentToAddress: string,
  labeledAt?: number,
  sentAt?: number,
  receivedAt?: number,
  trackingNumber?: string,
  returnTrackingNumber?: string,
  errorMessage?: string,
  details?: string,
  enrolleeShortcode?: string,
  skipAddressValidation: boolean
}

export type Config = {
  b2cTenantName: string,
  b2cClientId: string,
  b2cPolicyName: string,
  participantUiHostname: string,
  participantApiHostname: string,
  adminUiHostname: string,
  adminApiHostname: string,
  deploymentZone: string
}

export type MailingListContact = {
  id: string,
  name: string,
  email: string,
  createdAt: number
}


export type PortalEnvironmentChange = {
  siteContentChange: VersionedEntityChange,
  configChanges: ConfigChange[],
  preRegSurveyChanges: VersionedEntityChange,
  triggerChanges: ListChange<Trigger, VersionedConfigChange>
  participantDashboardAlertChanges: ParticipantDashboardAlertChange[],
  studyEnvChanges: StudyEnvironmentChange[]
}

export type StudyEnvironmentChange = {
  studyShortcode: string,
  configChanges: ConfigChange[],
  preEnrollSurveyChanges: VersionedEntityChange,
  consentChanges: ListChange<StudyEnvironmentConsent, VersionedConfigChange>,
  surveyChanges: ListChange<StudyEnvironmentSurvey, VersionedConfigChange>,
  triggerChanges: ListChange<Trigger, VersionedConfigChange>
}

export type VersionedEntityChange = {
  changed: true,
  oldStableId: string,
  newStableId: string,
  oldVersion: number,
  newVersion: number
} | {
  changed: false
}

type ConfigChangeValue = object | string | boolean
export type ConfigChange = {
  propertyName: string,
  oldValue: ConfigChangeValue,
  newValue: ConfigChangeValue
}

export type ListChange<T, CT> = {
  addedItems: T[],
  removedItems: T[],
  changedItems: CT[]
}

export type VersionedConfigChange = {
  sourceId: string,
  configChanges: ConfigChange[],
  documentChange: VersionedEntityChange
}

export type ParticipantDashboardAlertChange = {
  trigger: AlertTrigger,
  changes: ConfigChange[]
}

export type ExportOptions = {
  fileFormat: string,
  splitOptionsIntoColumns?: boolean,
  stableIdsForOptions?: boolean,
  onlyIncludeMostRecent?: boolean,
  limit?: number
}

export type ExportData = {
  columnKeys: string[],
  headerRowValues: string[],
  subHeaderRowValues: string[],
  valueMaps: Record<string, string>[]
}

export type BasicMetricDatum = {
  time: number,
  name: string,
  subcategory?: string
}

export type DatasetDetails = {
  id: string,
  createdAt: number,
  createdBy: string,
  lastUpdatedAt: number,
  studyEnvironmentId: string,
  tdrDatasetId: string,
  datasetName: string,
  description: string,
  status: string,
  lastExported: number
}

export type DatasetJobHistory = {
  id: string,
  createdAt: number,
  lastUpdatedAt: number,
  studyEnvironmentId: string,
  tdrJobId: string,
  datasetName: string,
  datasetId: string,
  status: string
  jobType: string
}

export type ParticipantNote = {
  id: string,
  createdAt: number,
  lastUpdatedAt: number,
  enrolleeId: string,
  text: string,
  kitRequestId?: string,
  creatingAdminUserId: string
}

export type KitRequestListResponse = {
  kitRequests: KitRequest[]
  exceptions: { message: string }[]
}

export type InternalConfig = {
  pepperDsmConfig: Record<string, string>
  addrValidationConfig: Record<string, string>
}

export type ParticipantTaskUpdateDto = {
  updates: TaskUpdateSpec[]
  portalParticipantUserIds?: string[]
  updateAll: boolean // if true, the portalParticipantUserIds list will be ignored and all participants will be updated
}

export type ParticipantTaskAssignDto = {
  taskType: ParticipantTaskType
  targetStableId: string
  targetAssignedVersion: number
  enrolleeIds?: string[]
  // if true, the enrolleeIds list will be ignored and tasks will be assigned to all enrollees
  // not already having the task in the duplicate window
  assignAllUnassigned: boolean
}

export type TaskUpdateSpec = {
  targetStableId: string
  updateToVersion: number
  updateFromVersion?: number // if absent, any other versions will be updated
  newStatus?: string // if specified, will change the status -- if, e.g. you want to make the updated tasks incomplete
}

export type StudyTemplate = 'BASIC' | undefined

export type StudyCreationDto = {
  shortcode: string,
  name: string,
  template: StudyTemplate
}


let bearerToken: string | null = null
export const API_ROOT = '/api'

export default {
  getInitHeaders() {
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    }
    if (bearerToken !== null) {
      headers['Authorization'] = `Bearer ${bearerToken}`
    }
    return headers
  },

  getGetInit() {
    return {
      headers: this.getInitHeaders(),
      method: 'GET'
    }
  },

  async processJsonResponse(response: Response) {
    const obj = await response.json()
    if (response.ok) {
      return obj
    }
    return Promise.reject(obj)
  },

  async processResponse(response: Response) {
    if (response.ok) {
      return response
    }
    return Promise.reject(response)
  },

  async getConfig(): Promise<Config> {
    const response = await fetch(`/config`)
    return await this.processJsonResponse(response)
  },

  async unauthedLogin(username: string): Promise<AdminUser> {
    const url = `${API_ROOT}/current-user/v1/unauthed/login?${new URLSearchParams({
      username
    })}`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders()
    })
    const loginResult = await this.processJsonResponse(response)
    const user: AdminUser = {
      ...loginResult.user,
      portalPermissions: loginResult.portalPermissions
    }
    return user
  },

  async refreshUnauthedLogin(token: string): Promise<AdminUser> {
    const url = `${API_ROOT}/current-user/v1/unauthed/refresh`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify({ token })
    })
    const loginResult = await this.processJsonResponse(response)
    const user: AdminUser = {
      ...loginResult.user,
      portalPermissions: loginResult.portalPermissions
    }
    return user
  },

  async tokenLogin(token: string): Promise<AdminUser> {
    const url = `${API_ROOT}/current-user/v1/login`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify({ token })
    })
    const loginResult = await this.processJsonResponse(response)
    const user: AdminUser = {
      ...loginResult.user,
      portalPermissions: loginResult.portalPermissions
    }
    return user
  },

  async refreshLogin(token: string): Promise<AdminUser> {
    const url = `${API_ROOT}/current-user/v1/refresh`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify({ token })
    })
    const loginResult = await this.processJsonResponse(response)
    const user: AdminUser = {
      ...loginResult.user,
      portalPermissions: loginResult.portalPermissions
    }
    return user
  },

  async getPortals(): Promise<Portal[]> {
    const response = await fetch(`${API_ROOT}/portals/v1`, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async getPortal(portalShortcode: string): Promise<Portal> {
    const response = await fetch(`${API_ROOT}/portals/v1/${portalShortcode}`, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async getLanguageTexts(selectedLanguage?: string): Promise<Record<string, string>> {
    const url = `${API_ROOT}/i18n/v1${selectedLanguage ? `?language=${selectedLanguage}` : ''}`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async createStudy(portalShortcode: string, study: StudyCreationDto): Promise<Study> {
    const url = `${API_ROOT}/portals/v1/${portalShortcode}/studies`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(study)
    })
    return await this.processJsonResponse(response)
  },

  async deleteStudy(portalShortcode: string, studyShortcode: string): Promise<Response> {
    const url = `${basePortalUrl(portalShortcode)}/studies/${studyShortcode}`
    const response = await fetch(url, {
      method: 'DELETE',
      headers: this.getInitHeaders()
    })
    return await this.processResponse(response)
  },

  async getPortalMedia(portalShortcode: string): Promise<SiteMediaMetadata[]> {
    const response = await fetch(`${API_ROOT}/portals/v1/${portalShortcode}/siteMedia`, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async uploadPortalMedia(portalShortcode: string, uploadFileName: string, version: number, file: File):
    Promise<SiteMediaMetadata> {
    const url = `${API_ROOT}/portals/v1/${portalShortcode}/siteMedia/upload/${uploadFileName}/${version}`
    const headers = this.getInitHeaders()
    delete headers['Content-Type'] // browsers will auto-add the correct type for the multipart file
    const formData = new FormData()
    formData.append('file', file)
    const response = await fetch(url, {
      method: 'POST',
      headers,
      body: formData
    })
    return await this.processJsonResponse(response)
  },

  async deletePortalMedia(portalShortcode: string, id: string): Promise<Response> {
    const url = `${API_ROOT}/portals/v1/${portalShortcode}/siteMedia/${id}`
    const response = await fetch(url, {
      method: 'DELETE',
      headers: this.getInitHeaders()
    })
    return await this.processResponse(response)
  },

  async getSurvey(portalShortcode: string, stableId: string, version: number): Promise<Survey> {
    const url = `${API_ROOT}/portals/v1/${portalShortcode}/surveys/${stableId}/${version}`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async createNewSurvey(portalShortcode: string, survey: Survey): Promise<Survey> {
    const url = `${API_ROOT}/portals/v1/${portalShortcode}/surveys`

    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(survey)
    })
    return await this.processJsonResponse(response)
  },

  async deleteSurvey(portalShortcode: string, stableId: string): Promise<Response> {
    const url = `${API_ROOT}/portals/v1/${portalShortcode}/surveys/${stableId}`
    const response = await fetch(url, {
      method: 'DELETE',
      headers: this.getInitHeaders()
    })
    return await this.processResponse(response)
  },

  async createNewSurveyVersion(portalShortcode: string, survey: Survey): Promise<Survey> {
    const url = `${API_ROOT}/portals/v1/${portalShortcode}/surveys/${survey.stableId}/newVersion`

    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(survey)
    })
    return await this.processJsonResponse(response)
  },

  async replaceConfiguredSurvey(portalShortcode: string, studyShortcode: string, environmentName: string, oldId: string,
    configuredSurvey: StudyEnvironmentSurvey): Promise<StudyEnvironmentSurvey> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, environmentName)}`
      + `/configuredSurveys/${oldId}/replace`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(configuredSurvey)
    })
    return await this.processJsonResponse(response)
  },

  async getConsentForm(portalShortcode: string, stableId: string, version: number): Promise<Survey> {
    const url = `${API_ROOT}/portals/v1/${portalShortcode}/consentForms/${stableId}/${version}`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async getConsentFormVersions(portalShortcode: string, stableId: string): Promise<Survey[]> {
    const response = await fetch(`${API_ROOT}/portals/v1/${portalShortcode}/consentForms/${stableId}/metadata`,
      this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async updateConfiguredConsent(portalShortcode: string, studyShortcode: string, environmentName: string,
    configuredConsent: StudyEnvironmentConsent): Promise<StudyEnvironmentConsent> {
    const url = `${API_ROOT}/portals/v1/${portalShortcode}/studies/${studyShortcode}` +
      `/env/${environmentName}/configuredConsents/${configuredConsent.id}`

    const response = await fetch(url, {
      method: 'PATCH',
      headers: this.getInitHeaders(),
      body: JSON.stringify(configuredConsent)
    })
    return await this.processJsonResponse(response)
  },

  async createNewConsentForm(portalShortcode: string, consentForm: ConsentForm): Promise<ConsentForm> {
    const url = `${API_ROOT}/portals/v1/${portalShortcode}/consentForms/`
      + `${consentForm.stableId}`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(consentForm)
    })
    return await this.processJsonResponse(response)
  },

  async createNewConsentVersion(portalShortcode: string, consentForm: ConsentForm): Promise<ConsentForm> {
    const url = `${API_ROOT}/portals/v1/${portalShortcode}/consentForms/`
      + `${consentForm.stableId}/${consentForm.version}/newVersion`

    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(consentForm)
    })
    return await this.processJsonResponse(response)
  },

  async createConfiguredConsent(portalShortcode: string, studyShortcode: string, envName: string,
    configuredConsent: StudyEnvironmentConsent): Promise<StudyEnvironmentConsent> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/configuredConsents`

    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(configuredConsent)
    })
    return await this.processJsonResponse(response)
  },

  async updateStudyEnvironment(portalShortcode: string, studyShortcode: string, envName: string,
    studyEnvUpdate: StudyEnvironmentUpdate): Promise<StudyEnvironmentUpdate> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}`
    const response = await fetch(url, {
      method: 'PATCH',
      headers: this.getInitHeaders(),
      body: JSON.stringify(studyEnvUpdate)
    })
    return await this.processJsonResponse(response)
  },

  async updateStudyEnvironmentConfig(portalShortcode: string, studyShortcode: string, envName: string,
    studyEnvConfigUpdate: StudyEnvironmentConfig): Promise<StudyEnvironmentConfig> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/config`
    const response = await fetch(url, {
      method: 'PATCH',
      headers: this.getInitHeaders(),
      body: JSON.stringify(studyEnvConfigUpdate)
    })
    return await this.processJsonResponse(response)
  },

  async getSurveyVersions(portalShortcode: string, stableId: string): Promise<Survey[]> {
    const response = await fetch(`${API_ROOT}/portals/v1/${portalShortcode}/surveys/${stableId}/metadata`,
      this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async findConfiguredSurveys(portalShortcode: string, studyShortcode: string,
    envName?: EnvironmentName, active?: boolean, stableId?: string): Promise<StudyEnvironmentSurvey[]> {
    const params = queryString.stringify({ envName, active, stableId })
    const url = `${basePortalUrl(portalShortcode)}/studies/${studyShortcode}`
      + `/configuredSurveys/findWithNoContent?${params}`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async createConfiguredSurvey(portalShortcode: string, studyShortcode: string, envName: string,
    configuredSurvey: StudyEnvironmentSurvey): Promise<StudyEnvironmentSurvey> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/configuredSurveys`

    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(configuredSurvey)
    })
    return await this.processJsonResponse(response)
  },

  async removeConfiguredSurvey(portalShortcode: string, studyShortcode: string, envName: string,
    configuredSurveyId: string): Promise<Response> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/configuredSurveys/${configuredSurveyId}`
    const response = await fetch(url, {
      method: 'DELETE',
      headers: this.getInitHeaders()
    })
    return await this.processResponse(response)
  },

  async updateConfiguredSurvey(portalShortcode: string, studyShortcode: string, envName: string,
    configuredSurvey: StudyEnvironmentSurvey): Promise<StudyEnvironmentSurvey> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/configuredSurveys/${configuredSurvey.id}`

    const response = await fetch(url, {
      method: 'PATCH',
      headers: this.getInitHeaders(),
      body: JSON.stringify(configuredSurvey)
    })
    return await this.processJsonResponse(response)
  },

  async findTasksForStableId(portalShortcode: string, studyShortcode: string,
    envName: string, targetStableId: string): Promise<ParticipantTask[]> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/participantTasks/findAll` +
      `?${queryString.stringify({ targetStableId })}`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async updateParticipantTaskVersions(studyEnvParams: StudyEnvParams,
    update: ParticipantTaskUpdateDto): Promise<ParticipantTask[]> {
    const url = `${baseStudyEnvUrlFromParams(studyEnvParams)}/participantTasks/updateAll`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(update)
    })
    return await this.processJsonResponse(response)
  },

  async getSiteContent(portalShortcode: string, stableId: string, version: number, language?: string) {
    const baseUrl = `${basePortalUrl(portalShortcode)}/siteContents/${stableId}/${version}`
    const response = await fetch(language ? `${baseUrl}?language=${language}` : baseUrl, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async assignParticipantTasksToEnrollees(studyEnvParams: StudyEnvParams,
    assignDto: ParticipantTaskAssignDto): Promise<ParticipantTask[]> {
    const url = `${baseStudyEnvUrlFromParams(studyEnvParams)}/participantTasks/assignToEnrollees`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(assignDto)
    })
    return await this.processJsonResponse(response)
  },

  async createNewSiteContentVersion(portalShortcode: string, stableId: string, siteContent: SiteContent) {
    const response = await fetch(`${basePortalUrl(portalShortcode)}/siteContents/${stableId}`, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(siteContent)
    })
    return await this.processJsonResponse(response)
  },

  async getSiteContentVersions(portalShortcode: string, stableId: string) {
    const response = await fetch(`${basePortalUrl(portalShortcode)}/siteContents/${stableId}`,
      this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async getSearchFacets(portalShortcode: string, studyShortcode: string, envName: string):
    Promise<EnrolleeSearchFacet[]> {
    const url =`${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/enrollee/search/facets`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async searchEnrollees(portalShortcode: string, studyShortcode: string, envName: string, facetValues: FacetValue[]):
    Promise<EnrolleeSearchResult[]> {
    const facetString = encodeURIComponent(facetValuesToString(facetValues))
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/enrollees?facets=${facetString}`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async getEnrollee(portalShortcode: string, studyShortcode: string, envName: string, enrolleeShortcode: string):
    Promise<Enrollee> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/enrollees/${enrolleeShortcode}`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async fetchEnrolleeNotifications(portalShortcode: string, studyShortcode: string, envName: string,
    enrolleeShortcode: string): Promise<Notification[]> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)
    }/enrollees/${enrolleeShortcode}/notifications`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },
  async fetchEnrolleeEvents(portalShortcode: string, studyShortcode: string, envName: string,
    enrolleeShortcode: string): Promise<Event[]> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/enrollees/${enrolleeShortcode}/events`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async withdrawEnrollee(portalShortcode: string, studyShortcode: string, envName: string,
    enrolleeShortcode: string): Promise<object> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)
    }/enrollees/${enrolleeShortcode}/withdraw`
    const response = await fetch(url, { method: 'POST', headers: this.getInitHeaders() })
    return await this.processJsonResponse(response)
  },

  async fetchEnrolleeChangeRecords(portalShortcode: string, studyShortcode: string, envName: string,
    enrolleeShortcode: string): Promise<DataChangeRecord[]> {
    const url =
      `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/enrollees/${enrolleeShortcode}/changeRecords`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async fetchEnrolleeAdminTasks(portalShortcode: string, studyShortcode: string, envName: string,
    enrolleeShortcode: string): Promise<AdminTask[]> {
    const url =
      `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/enrollees/${enrolleeShortcode}/adminTasks`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async updateProfileForEnrollee(
    portalShortcode: string, studyShortcode: string, envName: string,
    enrolleeShortcode: string, profile: ProfileUpdateDto
  ):
    Promise<Profile> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/profiles/byEnrollee/${enrolleeShortcode}`
    const response = await fetch(url, {
      method: 'PUT',
      body: JSON.stringify(profile),
      headers: this.getInitHeaders()
    })
    return await this.processJsonResponse(response)
  },

  async validateAddress(
    address: MailingAddress
  ): Promise<AddressValidationResult> {
    const url = `${baseAddressUrl()}/validate`
    const response = await fetch(url, {
      method: 'PUT',
      body: JSON.stringify(address),
      headers: this.getInitHeaders()
    })
    return await this.processJsonResponse(response)
  },

  async createParticipantNote(portalShortcode: string,
    studyShortcode: string,
    envName: string,
    enrolleeShortcode: string,
    note: { text: string, assignedAdminUserId?: string }): Promise<ParticipantNote> {
    const url =
      `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/enrollees/${enrolleeShortcode}/participantNote`
    const response = await fetch(url, {
      method: 'POST',
      body: JSON.stringify(note),
      headers: this.getInitHeaders()
    })
    return await this.processJsonResponse(response)
  },

  async fetchEnrolleesWithKits(
    portalShortcode: string,
    studyShortcode: string,
    envName: string
  ): Promise<Enrollee[]> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/enrolleesWithKits`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async fetchKitsByStudyEnvironment(
    portalShortcode: string,
    studyShortcode: string,
    envName: string
  ): Promise<KitRequest[]> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/kits`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async createKitRequest(
    portalShortcode: string,
    studyShortcode: string,
    envName: string,
    enrolleeShortcode: string,
    kitOptions: { kitType: string, skipAddressValidation: boolean}
  ): Promise<string> {
    const url =
      `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/enrollees/${enrolleeShortcode}/requestKit`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(kitOptions)
    })
    return await this.processJsonResponse(response)
  },

  async requestKits(
    portalShortcode: string,
    studyShortcode: string,
    envName: string,
    enrolleeShortcodes: string[],
    kitOptions: { kitType: string, skipAddressValidation: boolean }
  ): Promise<KitRequestListResponse> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/requestKits`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify({
        creationDto: kitOptions,
        enrolleeShortcodes
      })
    })
    return await this.processJsonResponse(response)
  },

  async refreshKitStatuses(
    portalShortcode: string,
    studyShortcode: string,
    envName: string) {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/kits/refreshKitStatuses`
    return await this.processResponse(await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders()
    }))
  },

  async fetchEnrolleeKitRequests(
    portalShortcode: string,
    studyShortcode: string,
    envName: string,
    enrolleeShortcode: string
  ): Promise<KitRequest[]> {
    const url =
      `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/enrollees/${enrolleeShortcode}/kitRequests`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async fetchKitTypes(studyEnvParams: StudyEnvParams): Promise<KitType[]> {
    const url = `${baseStudyEnvUrlFromParams(studyEnvParams)}/kitTypes`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async updateTrigger(portalShortcode: string, envName: string, studyShortcode: string,
    oldConfigId: string, updatedConfig: Trigger): Promise<Trigger> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/triggers/${oldConfigId}`
    const response = await fetch(url, {
      method: 'PATCH',
      headers: this.getInitHeaders(),
      body: JSON.stringify(updatedConfig)
    })
    return await this.processJsonResponse(response)
  },

  async createTrigger(studyEnvParams: StudyEnvParams,
    config: Trigger): Promise<Trigger> {
    const url = `${baseStudyEnvUrlFromParams(studyEnvParams)}/triggers`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(config)
    })
    return await this.processJsonResponse(response)
  },

  async testTrigger(portalShortcode: string, studyShortcode: string, envName: string,
    triggerId: string, enrolleeRuleData: object): Promise<Response> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/triggers/${triggerId}`
      + `/test`
    return await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(enrolleeRuleData)
    })
  },

  async deleteTrigger(portalShortcode: string, studyShortcode: string, envName: string,
    configId: string): Promise<Response> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/triggers/${configId}`
    return await fetch(url, {
      method: 'DELETE',
      headers: this.getInitHeaders()
    })
  },


  async fetchMetric(portalShortcode: string, studyShortcode: string, envName: string, metricName: string):
    Promise<BasicMetricDatum[]> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/metrics/${metricName}`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  exportEnrollees(portalShortcode: string, studyShortcode: string,
    envName: string, exportOptions: ExportOptions):
    Promise<Response> {
    const exportOptionsParams = exportOptions as Record<string, unknown>
    let url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/export/data?`
    const searchParams = new URLSearchParams()
    for (const prop in exportOptionsParams) {
      searchParams.set(prop, (exportOptionsParams[prop] as string | boolean).toString())
    }
    url += searchParams.toString()
    return fetch(url, this.getGetInit())
  },

  exportDictionary(portalShortcode: string, studyShortcode: string,
    envName: string, exportOptions: ExportOptions):
    Promise<Response> {
    const exportOptionsParams = exportOptions as Record<string, unknown>
    let url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/export/dictionary?`
    const searchParams = new URLSearchParams()
    for (const prop in exportOptionsParams) {
      searchParams.set(prop, (exportOptionsParams[prop] as string | boolean).toString())
    }
    url += searchParams.toString()
    return fetch(url, this.getGetInit())
  },

  async findTrigger(portalShortcode: string, studyShortcode: string, envName: string, id: string):
Promise<Trigger> {
    const url =`${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/triggers/${id}`
    const response = await fetch(url,  this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async findTriggersForStudyEnv(portalShortcode: string, studyShortcode: string, envName: string):
    Promise<Trigger[]> {
    const url =`${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/triggers`
    const response = await fetch(url,  this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async sendAdHocNotification({
    portalShortcode, studyShortcode, envName, enrolleeShortcodes,
    customMessages, triggerId
  }: {
      portalShortcode: string,
      studyShortcode: string,
      envName: string,
      enrolleeShortcodes: string[],
      customMessages: Record<string, string>,
      triggerId: string
  }): Promise<Response> {
    const url =`${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/notifications/adhoc`
    return await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify({
        triggerId,
        enrolleeShortcodes,
        customMessages
      })
    })
  },

  async listDatasetsForStudyEnvironment(portalShortcode: string, studyShortcode: string,
    envName: string):
    Promise<DatasetDetails[]> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/datarepo/datasets`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async getJobHistoryForDataset(portalShortcode: string, studyShortcode: string,
    envName: string, datasetName: string):
    Promise<DatasetJobHistory[]> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/datarepo/datasets/${datasetName}/jobs`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async createDatasetForStudyEnvironment(portalShortcode: string, studyShortcode: string,
    envName: string, createDataset: { name: string, description: string }):
    Promise<Response> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/datarepo/datasets`
    return await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(createDataset)
    })
  },

  async deleteDatasetForStudyEnvironment(portalShortcode: string, studyShortcode: string,
    envName: string, datasetName: string):
    Promise<Response> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/datarepo/datasets/${datasetName}`
    return await fetch(url, {
      method: 'DELETE',
      headers: this.getInitHeaders()
    })
  },

  async fetchMailingList(portalShortcode: string, envName: string): Promise<MailingListContact[]> {
    const url = `${basePortalEnvUrl(portalShortcode, envName)}/mailingList`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async deleteMailingListContact(portalShortcode: string, envName: string, contactId: string): Promise<Response> {
    const url = `${basePortalEnvUrl(portalShortcode, envName)}/mailingList/${contactId}`
    const response = await fetch(url, {
      method: 'DELETE',
      headers: this.getInitHeaders()
    })
    return await this.processResponse(response)
  },

  async fetchAdminUsers(): Promise<AdminUser[]> {
    const url = `${API_ROOT}/adminUsers/v1`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async fetchAdminUser(adminUserId: string, portalShortcode?: string): Promise<AdminUser> {
    let url = `${API_ROOT}/adminUsers/v1/${adminUserId}`
    if (portalShortcode) {
      url += `?portalShortcode=${portalShortcode}`
    }
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async fetchAdminTasksByStudyEnv(portalShortcode: string, studyShortcode: string,
    envName: string, include: string[]): Promise<AdminTaskListDto> {
    let url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/adminTasks`
    if (include.length) {
      url = `${url}?include=${include.join(',')}`
    }
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async updateAdminTask(portalShortcode: string, studyShortcode: string,
    envName: string, task: AdminTask): Promise<AdminTask> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/adminTasks/${task.id}`
    const response = await fetch(url, {
      method: 'PATCH',
      headers: this.getInitHeaders(),
      body: JSON.stringify(task)
    })
    return await this.processJsonResponse(response)
  },

  async fetchAdminUsersByPortal(portalShortcode: string): Promise<AdminUser[]> {
    const url = `${API_ROOT}/portals/v1/${portalShortcode}/adminUsers`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async createUser(adminUser: NewAdminUser): Promise<AdminUser> {
    const url = `${API_ROOT}/adminUsers/v1`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(adminUser)
    })
    return await this.processJsonResponse(response)
  },

  async removePortalUser(adminUser: AdminUser, portalShortcode: string): Promise<Response> {
    const url = `${API_ROOT}/portals/v1/${portalShortcode}/adminUser/${adminUser.id}`
    const response = await fetch(url, {
      method: 'DELETE',
      headers: this.getInitHeaders()
    })
    return await this.processResponse(response)
  },

  async updatePortalEnv(portalShortcode: string, envName: string, update: PortalEnvironment) {
    const url = `${basePortalEnvUrl(portalShortcode, envName)}`
    const response = await fetch(url, {
      method: 'PATCH',
      headers: this.getInitHeaders(),
      body: JSON.stringify(update)
    })
    return await this.processJsonResponse(response)
  },

  async listPortalEnvAlerts(portalShortcode: string, envName: string): Promise<ParticipantDashboardAlert[]> {
    const url = `${basePortalEnvUrl(portalShortcode, envName)}/dashboard/config/alerts`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async updatePortalEnvAlert(
    portalShortcode: string, envName: string, triggerName: string, alertConfig: ParticipantDashboardAlert
  ) {
    const url = `${basePortalEnvUrl(portalShortcode, envName)}/dashboard/config/alerts/${triggerName}`
    const response = await fetch(url, {
      method: 'PATCH',
      headers: this.getInitHeaders(),
      body: JSON.stringify(alertConfig)
    })
    return await this.processJsonResponse(response)
  },

  async createPortalEnvAlert(
    portalShortcode: string, envName: string, triggerName: string, alertConfig: ParticipantDashboardAlert
  ) {
    const url = `${basePortalEnvUrl(portalShortcode, envName)}/dashboard/config/alerts/${triggerName}`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(alertConfig)
    })
    return await this.processJsonResponse(response)
  },

  async fetchEnvDiff(portalShortcode: string, sourceEnvName: string, destEnvName: string):
    Promise<PortalEnvironmentChange> {
    const url = `${basePortalEnvUrl(portalShortcode, destEnvName)}/diff?sourceEnv=${sourceEnvName}`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async applyEnvChanges(portalShortcode: string, destEnvName: string, changes: PortalEnvironmentChange):
    Promise<PortalEnvironment> {
    const url = `${basePortalEnvUrl(portalShortcode, destEnvName)}/diff/apply`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(changes)
    })
    return await this.processJsonResponse(response)
  },

  async updatePortalEnvConfig(portalShortcode: string, envName: string, config: PortalEnvironmentConfig) {
    const url = `${basePortalEnvUrl(portalShortcode, envName)}/config`
    const response = await fetch(url, {
      method: 'PATCH',
      headers: this.getInitHeaders(),
      body: JSON.stringify(config)
    })
    return await this.processJsonResponse(response)
  },

  async fetchInternalConfig(): Promise<InternalConfig> {
    const url = `${API_ROOT}/internal/v1/config`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async populatePortal(fileName: string, overwrite: boolean, shortcodeOverride: string | undefined) {
    const params = queryString.stringify({ filePathName: fileName, overwrite, shortcodeOverride  })
    const url = `${basePopulateUrl()}/portal?${params}`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders()
    })
    return await this.processJsonResponse(response)
  },

  async uploadPortal(file: File, overwrite: boolean, shortcodeOverride: string | undefined):
    Promise<SiteMediaMetadata> {
    const params = queryString.stringify({ overwrite, shortcodeOverride })
    const url = `${basePopulateUrl()}/portal/upload?${params}`
    const headers = this.getInitHeaders()
    delete headers['Content-Type'] // browsers will auto-add the correct type for the multipart file
    const formData = new FormData()
    formData.append('file', file)
    const response = await fetch(url, {
      method: 'POST',
      headers,
      body: formData
    })
    return await this.processJsonResponse(response)
  },

  async extractPortal(portalShortcode: string) {
    const url = `${basePopulateUrl()}/portal/${portalShortcode}/extract`
    const response = await fetch(url, this.getGetInit())
    return this.processResponse(response)
  },

  async populateSurvey(fileName: string, overwrite: boolean, portalShortcode: string) {
    const url = `${basePopulateUrl()}/survey/${portalShortcode}?filePathName=${fileName}&overwrite=${overwrite}`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders()
    })
    return await this.processJsonResponse(response)
  },

  async populateSiteContent(fileName: string, overwrite: boolean, portalShortcode: string) {
    const url = `${basePopulateUrl()}/siteContent/${portalShortcode}?filePathName=${fileName}&overwrite=${overwrite}`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders()
    })
    return await this.processJsonResponse(response)
  },

  async populateAdminConfig() {
    const url = `${basePopulateUrl()}/adminConfig`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders()
    })
    return await this.processJsonResponse(response)
  },

  getParticipantLink(portalEnvConfig: PortalEnvironmentConfig, uiHostname: string,
    portalShortcode: string, envName: string): string {
    if (portalEnvConfig?.participantHostname) {
      return `https://${portalEnvConfig.participantHostname}`
    }
    const participantHost = `${envName}.${portalShortcode}.${uiHostname}`
    return `https://${participantHost}`
  },

  setBearerToken(token: string | null) {
    bearerToken = token
  }
}

/** gets an image url for SiteMedia */
export function getMediaUrl(portalShortcode: string, cleanFileName: string, version: number) {
  return `${getMediaBaseUrl(portalShortcode)}/${version}/${cleanFileName}`
}

/** gets the base url for public site media (e.g., images) */
export function getMediaBaseUrl(portalShortcode: string) {
  return `${basePublicPortalEnvUrl(portalShortcode, 'live')}/siteMedia`
}

/** base api path for study-scoped api requests */
function basePortalEnvUrl(portalShortcode: string, envName: string) {
  return `${API_ROOT}/portals/v1/${portalShortcode}/env/${envName}`
}

function basePortalUrl(portalShortcode: string) {
  return `${API_ROOT}/portals/v1/${portalShortcode}`
}

/** base api path for study-scoped api requests */
function basePublicPortalEnvUrl(portalShortcode: string, envName: string) {
  return `${API_ROOT}/public/portals/v1/${portalShortcode}/env/${envName}`
}

/** base api path for study-scoped api requests */
function baseStudyEnvUrl(portalShortcode: string, studyShortcode: string, envName: string) {
  return `${API_ROOT}/portals/v1/${portalShortcode}/studies/${studyShortcode}/env/${envName}`
}

function baseStudyEnvUrlFromParams(studyEnvParams: StudyEnvParams) {
  return baseStudyEnvUrl(studyEnvParams.portalShortcode, studyEnvParams.studyShortcode, studyEnvParams.envName)
}

/** base api path for populate api calls */
function basePopulateUrl() {
  return `${API_ROOT}/internal/v1/populate`
}

/** base api path for address api calls */
function baseAddressUrl() {
  return `${API_ROOT}/address/v1`
}
