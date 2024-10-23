import {
  AddressValidationResult,
  AlertTrigger,
  Enrollee,
  EnrolleeRelation,
  EnvironmentName,
  Family,
  HubResponse,
  KitRequest,
  KitType,
  MailingAddress,
  ParticipantDashboardAlert,
  ParticipantNote,
  ParticipantTask,
  ParticipantTaskType,
  ParticipantUser,
  Portal,
  PortalEnvironment,
  PortalEnvironmentConfig,
  PortalEnvironmentLanguage,
  PortalParticipantUser,
  Profile,
  SiteContent,
  Study,
  StudyEnvironmentConfig,
  StudyEnvironmentSurvey,
  StudyEnvParams,
  Survey,
  SurveyResponse,
  Trigger
} from '@juniper/ui-core'
import queryString from 'query-string'
import {
  AdminUser,
  AdminUserParams,
  Role
} from './adminUser'
import { SystemStatus } from 'status/status'

export type {
  Answer,
  AddressValidationResult,
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
  StudyEnvironmentSurvey,
  Survey,
  SurveyResponse,
  VersionedForm
} from '@juniper/ui-core'

export type StudyEnvironmentUpdate = {
  id: string,
  preEnrollSurveyId: string
}

export type EnrolleeSearchExpressionResult = {
  enrollee: Enrollee,
  profile: Profile,
  latestKit?: KitRequest,
  families: Family[]
  participantUser?: ParticipantUser
  portalParticipantUser?: PortalParticipantUser
}

export type ParticipantUsersAndEnrollees = {
  participantUsers: ParticipantUser[]
  enrollees: Enrollee[]
}

export type ExpressionSearchFacets  = { [index: string]: SearchValueTypeDefinition }

export type ProfileUpdateDto = {
  justification: string,
  profile: Profile
}

export type NotificationEventDetails = {
  subject: string,
  toEmail: string,
  fromEmail: string,
  status: string,
  opensCount: number,
  clicksCount: number,
  lastEventTime: number
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
  enrollee?: Enrollee,
  trigger?: Trigger
  eventDetails?: NotificationEventDetails
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
  enrolleeId?: string,
  responsibleAdminUserId?: string,
  justification?: string
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

export type ParticipantTaskListDto = {
  tasks: ParticipantTask[]
  enrollees: Enrollee[]
  participantNotes: ParticipantNote[]
}

export type SiteMediaMetadata = {
  id: string,
  createdAt: number,
  cleanFileName: string,
  version: number
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
  id?: string,
  name: string,
  email: string,
  createdAt?: number
}

export type DataImport = {
  id: string,
  importType: string,
  responsibleUserId: string,
  studyEnvironmentId: string,
  status?: string,
  createdAt: number,
  lastUpdatedAt?: number,
  importItems?: DataImportItem[]
}

export type DataImportItem = {
  id?: string,
  importId: string,
  createdParticipantUserId?: string,
  createdEnrolleeId?: string,
  status?: string,
  message?: string,
  detail?: string,
  createdAt: number,
  lastUpdatedAt?: number
}

export type PortalEnvironmentChangeRecord = {
  createdAt: number
  portalId?: string
  environmentName: string
  adminUserId: string
  portalEnvironmentChange: string
  parsedChange?: PortalEnvironmentChange
}

export type PortalEnvironmentChange = {
  siteContentChange: VersionedEntityChange
  configChanges: ConfigChange[]
  preRegSurveyChanges: VersionedEntityChange
  triggerChanges: ListChange<Trigger, VersionedConfigChange>
  participantDashboardAlertChanges: ParticipantDashboardAlertChange[]
  studyEnvChanges: StudyEnvironmentChange[]
  languageChanges: ListChange<PortalEnvironmentLanguage, VersionedConfigChange>
}

export type StudyEnvironmentChange = {
  studyShortcode: string
  configChanges: ConfigChange[]
  preEnrollSurveyChanges: VersionedEntityChange
  surveyChanges: ListChange<StudyEnvironmentSurvey, VersionedConfigChange>
  triggerChanges: ListChange<Trigger, VersionedConfigChange>
  kitTypeChanges: ListChange<KitType, VersionedConfigChange>
}

export type VersionedEntityChange = {
  changed: true
  oldStableId: string
  newStableId: string
  oldVersion: number
  newVersion: number
} | {
  changed: false
}

type ConfigChangeValue = object | string | boolean
export type ConfigChange = {
  propertyName: string
  oldValue: ConfigChangeValue
  newValue: ConfigChangeValue
}

export type ListChange<T, CT> = {
  addedItems: T[]
  removedItems: T[]
  changedItems: CT[]
}

export type VersionedConfigChange = {
  sourceId: string
  destId: string
  configChanges: ConfigChange[]
  documentChange: VersionedEntityChange
}

export type ParticipantDashboardAlertChange = {
  trigger: AlertTrigger
  changes: ConfigChange[]
}

export type ExportOptions = {
  fileFormat: string,
  splitOptionsIntoColumns?: boolean,
  stableIdsForOptions?: boolean,
  onlyIncludeMostRecent?: boolean,
  includeSubHeaders?: boolean,
  excludeModules?: string[],
  filterString?: string,
  rowLimit?: number,
  includeFields: string[]
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

export type KitRequestListResponse = {
  kitRequests: KitRequest[]
  exceptions: { message: string }[]
}

export type InternalConfig = {
  pepperDsmConfig: Record<string, string>
  addrValidationConfig: Record<string, string>
  airtable: Record<string, string>
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

export type SearchValueType = 'STRING' | 'NUMBER' | 'DATE' | 'BOOLEAN' | 'INSTANT'
export type SearchValueTypeDefinition = {
  type: SearchValueType
  choices?: { stableId: string, text: string }[]
  allowMultiple: boolean
  allowOtherDescription: boolean
}

export type WithdrawnEnrollee = {
  createdAt: number
  shortcode: string
  userData: string
  reason: 'PARTICIPANT_REQUEST' | 'TESTING' | 'DUPLICATE'
  note: string
}

export type ExportIntegration = {
    id: string,
  name: string,
    createdAt: number,
    lastUpdatedAt: number,
    destinationType: string,
    enabled: boolean,
    exportOptions: ExportOptions,
    destinationUrl: string
}

export type ExportIntegrationJob = {
  id: string,
  status: string,
  exportIntegrationId: string,
  startedAt: number,
  completedAt?: number,
  result: string,
  creatingAdminUserId?: string,
  systemProcess?: string
}

export type ParticipantUserMerge = {
  users: MergeAction<ParticipantUser, object>
  ppUsers: MergeAction<PortalParticipantUser, object>
  enrollees: MergeAction<Enrollee, EnrolleeMergePlan>[]
}

export type MergeAction<T, MP> = {
  pair: MergePair<T>
  action: MergeActionAction
  mergePlan?: MP
}

export type EnrolleeMergePlan = {
  tasks: MergeAction<ParticipantTask, object>[]
  kits: MergeAction<KitRequest, object>[]
}

export type MergeActionAction =
  'MOVE_SOURCE' | // no change to target, reassign source to target (not a delete/recreate, just a reassign)
  'NO_ACTION' | // nothing
  'MERGE' | // do some logic to reconcile source and target
  'DELETE_SOURCE' | // delete the source, likely because it is empty or a pure dupe
  'MOVE_SOURCE_DELETE_TARGET' // move source to target and delete target

export type MergePair<T> = {
  source?: T,
  target?: T
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
      token: loginResult.token,
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
      token: loginResult.token,
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

  async fetchStudiesWithEnvs(portalShortcode: string, envName: string): Promise<Study[]> {
    const response = await fetch(`${API_ROOT}/portals/v1/${portalShortcode}/studies?envName=${envName}`,
      this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async getLanguageTexts(selectedLanguage: string, portalShortcode?: string): Promise<Record<string, string>> {
    const params = queryString.stringify({ portalShortcode, language: selectedLanguage })
    const url = `${API_ROOT}/i18n/v1?${params}`
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

  async getStudyEnvSurveys(portalShortcode: string, stableId: string, version: number): Promise<Survey> {
    const url = `${API_ROOT}/portals/v1/${portalShortcode}/surveys/${stableId}/${version}/studyEnvSurveys`
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

  async replaceConfiguredSurvey(portalShortcode: string, studyShortcode: string, environmentName: string,
    configuredSurvey: { surveyId: string, studyEnvironmentId: string }): Promise<StudyEnvironmentSurvey> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, environmentName)}`
      + `/configuredSurveys/replace`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(configuredSurvey)
    })
    return await this.processJsonResponse(response)
  },

  async updateSurveyResponse({ studyEnvParams, stableId, version, enrolleeShortcode, response, taskId }: {
    studyEnvParams: StudyEnvParams, stableId: string, version: number,
    response: SurveyResponse, enrolleeShortcode: string, taskId: string
  }): Promise<HubResponse> {
    let url = `${baseStudyEnvUrlFromParams(studyEnvParams)}/enrollee/${enrolleeShortcode}`
      + `/surveys/${stableId}/${version}`
    if (taskId) {
      url = `${url}?taskId=${taskId}`
    }
    const result = await fetch(url, {
      method: 'PATCH',
      headers: this.getInitHeaders(),
      body: JSON.stringify(response)
    })
    return await this.processJsonResponse(result)
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

  async updateConfiguredSurveys(portalShortcode: string, studyShortcode: string, envName: string,
    configuredSurveys: StudyEnvironmentSurvey[]): Promise<StudyEnvironmentSurvey[]> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/configuredSurveys`

    const response = await fetch(url, {
      method: 'PATCH',
      headers: this.getInitHeaders(),
      body: JSON.stringify(configuredSurveys)
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

  async getSiteContent(portalShortcode: string, stableId: string, version: number) {
    const baseUrl = `${basePortalUrl(portalShortcode)}/siteContents/${stableId}/${version}`
    const response = await fetch(baseUrl, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async getCurrentSiteContent(portalShortcode: string, environmentName: string) {
    const baseUrl = `${basePortalEnvUrl(portalShortcode, environmentName)}/siteContent`
    const response =await fetch(baseUrl, this.getGetInit())
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

  async fetchParticipantUsers(portalShortcode: string, envName: string): Promise<ParticipantUsersAndEnrollees> {
    const response = await fetch(`${basePortalUrl(portalShortcode)}/env/${envName}/participantUsers`,
      this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async fetchMergePlan(portalShortcode: string, envName: string, sourceEmail: string, targetEmail: string):
    Promise<ParticipantUserMerge> {
    const response = await fetch(`${basePortalUrl(portalShortcode)}/env/${envName}/participantUsers/merge/plan`, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify({ sourceEmail, targetEmail })
    })
    return await this.processJsonResponse(response)
  },

  async executeMergePlan(portalShortcode: string, envName: string, mergePlan: ParticipantUserMerge):
    Promise<ParticipantUserMerge> {
    const response = await fetch(`${basePortalUrl(portalShortcode)}/env/${envName}/participantUsers/merge/execute`, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(mergePlan)
    })
    return await this.processJsonResponse(response)
  },

  async getExpressionSearchFacets(portalShortcode: string, studyShortcode: string, envName: string):
    Promise<ExpressionSearchFacets> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/enrollee/search/v2/facets`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async executeSearchExpression(
    portalShortcode: string,
    studyShortcode: string,
    envName: string,
    expression: string,
    opts: { limit?: number } = {}):
    Promise<EnrolleeSearchExpressionResult[]> {
    let url = `${
      baseStudyEnvUrl(portalShortcode, studyShortcode, envName)
    }/enrollee/search/v2?expression=${encodeURIComponent(expression)}`
    if (opts.limit) {
      url += `&limit=${opts.limit}`
    }
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async getEnrollee(portalShortcode: string, studyShortcode: string, envName: string, enrolleeShortcodeOrId: string):
    Promise<Enrollee> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/enrollees/${enrolleeShortcodeOrId}`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async fetchEnrolleeNotifications(portalShortcode: string, studyShortcode: string, envName: string,
    enrolleeShortcode: string): Promise<Notification[]> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)
    }/notifications/byEnrollee/${enrolleeShortcode}`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async fetchTriggerNotifications(portalShortcode: string, studyShortcode: string, envName: string,
    triggerId: string): Promise<Notification[]> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)
    }/notifications/byTrigger/${triggerId}`
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
    enrolleeShortcode: string, withdrawParams: {reason: string, note: string}): Promise<object> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)
    }/enrollees/${enrolleeShortcode}/withdraw`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(withdrawParams)
    })
    return await this.processJsonResponse(response)
  },

  async fetchWithdrawnEnrollees(studyEnvParams: StudyEnvParams): Promise<WithdrawnEnrollee[]> {
    const url = `${baseStudyEnvUrlFromParams(studyEnvParams)}/withdrawnEnrollees`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async fetchEnrolleeChangeRecords(portalShortcode: string, studyShortcode: string, envName: string,
    enrolleeShortcode: string, modelName?: string): Promise<DataChangeRecord[]> {
    const params = queryString.stringify({ modelName })
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)
    }/enrollees/${enrolleeShortcode}/changeRecords?${params}`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async fetchEnrolleeAdminTasks(portalShortcode: string, studyShortcode: string, envName: string,
    enrolleeShortcode: string): Promise<ParticipantTask[]> {
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

  async findRelationsByTargetShortcode(
    portalShortcode: string,
    studyShortcode: string,
    envName: EnvironmentName,
    enrolleeShortcode: string): Promise<EnrolleeRelation[]> {
    const url = (
        `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/enrolleeRelations/byTarget/${enrolleeShortcode}`
    )
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async createRelation(
    portalShortcode: string,
    studyShortcode: string,
    envName: EnvironmentName,
    relation: EnrolleeRelation,
    justification: string): Promise<EnrolleeRelation> {
    const params = queryString.stringify({ justification })
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/enrolleeRelations?${params}`
    const response = await fetch(url, {
      method: 'POST',
      body: JSON.stringify(relation),
      headers: this.getInitHeaders()
    })
    return await this.processJsonResponse(response)
  },

  async deleteRelation(
    portalShortcode: string,
    studyShortcode: string,
    envName: EnvironmentName,
    relationId: string,
    justification: string): Promise<Response> {
    const params = queryString.stringify({ justification })
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/enrolleeRelations/${relationId}?${params}`
    const response = await fetch(url, {
      method: 'DELETE',
      headers: this.getInitHeaders()
    })
    return await this.processResponse(response)
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
    kitOptions: { kitType: string, distributionMethod: string, skipAddressValidation: boolean, kitLabel?: string }
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

  async collectKit(
    portalShortcode: string,
    studyShortcode: string,
    envName: string,
    enrolleeShortcode: string,
    kitOptions: { kitLabel: string, returnTrackingNumber: string }
  ): Promise<string> {
    const url =
        `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/enrollees/${enrolleeShortcode}/collectKit`
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
    kitOptions: { kitType: string, distributionMethod: string, skipAddressValidation: boolean }
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

  async updateKitTypes(studyEnvParams: StudyEnvParams, kitTypes: string[]): Promise<string> {
    const url = `${baseStudyEnvUrlFromParams(studyEnvParams)}/kitTypes`
    const response = await fetch(url, {
      method: 'PATCH',
      headers: this.getInitHeaders(),
      body: JSON.stringify(kitTypes)
    })
    return await this.processJsonResponse(response)
  },

  async fetchAllowedKitTypes(studyEnvParams: StudyEnvParams): Promise<KitType[]> {
    const url = `${baseStudyEnvUrlFromParams(studyEnvParams)}/allowedKitTypes`
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
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/export/data`
    return fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(exportOptions)
    })
  },

  exportDictionary(portalShortcode: string, studyShortcode: string,
    envName: string, exportOptions: ExportOptions):
    Promise<Response> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/export/dictionary`
    return fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(exportOptions)
    })
  },

  async fetchExportIntegrations(studyEnvParams: StudyEnvParams): Promise<ExportIntegration[]> {
    const url = `${baseStudyEnvUrlFromParams(studyEnvParams)}/exportIntegrations`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async fetchExportIntegration(studyEnvParams: StudyEnvParams, id: string): Promise<ExportIntegration> {
    const url = `${baseStudyEnvUrlFromParams(studyEnvParams)}/exportIntegrations/${id}`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async createExportIntegration(studyEnvParams: StudyEnvParams, integration: ExportIntegration):
    Promise<ExportIntegration> {
    const url = `${baseStudyEnvUrlFromParams(studyEnvParams)}/exportIntegrations`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(integration)
    })
    return await this.processJsonResponse(response)
  },

  async runExportIntegration(studyEnvParams: StudyEnvParams, id: string): Promise<ExportIntegrationJob> {
    const url = `${baseStudyEnvUrlFromParams(studyEnvParams)}/exportIntegrations/${id}/run`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders()
    })
    return await this.processJsonResponse(response)
  },

  async saveExportIntegration(studyEnvParams: StudyEnvParams, integration: ExportIntegration):
    Promise<ExportIntegration> {
    const url = `${baseStudyEnvUrlFromParams(studyEnvParams)}/exportIntegrations/${integration.id}`
    const response = await fetch(url, {
      method: 'PATCH',
      headers: this.getInitHeaders(),
      body: JSON.stringify(integration)
    })
    return await this.processJsonResponse(response)
  },

  async fetchExportIntegrationJobs(studyEnvParams: StudyEnvParams): Promise<ExportIntegrationJob[]> {
    const url = `${baseStudyEnvUrlFromParams(studyEnvParams)}/exportIntegrationJobs`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async findTrigger(portalShortcode: string, studyShortcode: string, envName: string, id: string):
    Promise<Trigger> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/triggers/${id}`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async findTriggersForStudyEnv(portalShortcode: string, studyShortcode: string, envName: string):
    Promise<Trigger[]> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/triggers`
    const response = await fetch(url, this.getGetInit())
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
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/notifications/adhoc`
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

  async addMailingListContacts(portalShortcode: string, envName: string, contact: MailingListContact[]):
    Promise<MailingListContact[]> {
    const url = `${basePortalEnvUrl(portalShortcode, envName)}/mailingList`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(contact)
    })
    return await this.processJsonResponse(response)
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

  async fetchDataImports(portalShortcode: string, studyShortcode: string, envName: string): Promise<DataImport[]> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/dataImport`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async fetchDataImport(portalShortcode: string, studyShortcode: string,
    envName: string, importId: string): Promise<DataImport> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/dataImport/${importId}`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async uploadDataImport(file: File, portalShortCode: string, studyShortcode: string, envName: EnvironmentName):
    Promise<DataImport> {
    const url = `${baseStudyEnvUrl(portalShortCode, studyShortcode, envName)}/dataImport`
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

  async deleteDataImport(portalShortcode: string, studyShortcode: string, envName: string,
    dataImportId: string): Promise<Response> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/dataImport/${dataImportId}`

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
    envName: string, include: string[]): Promise<ParticipantTaskListDto> {
    let url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/adminTasks`
    if (include.length) {
      url = `${url}?include=${include.join(',')}`
    }
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async updateAdminTask(portalShortcode: string, studyShortcode: string,
    envName: string, task: ParticipantTask): Promise<ParticipantTask> {
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

  async createSuperuser(adminUser: AdminUserParams): Promise<AdminUser> {
    const url = `${API_ROOT}/adminUsers/v1`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(adminUser)
    })
    return await this.processJsonResponse(response)
  },

  async createPortalUser(adminUser: AdminUserParams): Promise<AdminUser> {
    const url = `${API_ROOT}/portals/v1/${adminUser.portalShortcode}/adminUsers`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(adminUser)
    })
    return await this.processJsonResponse(response)
  },

  async updatePortalUser(portalShortcode: string, adminUserId: string, roleNames: string[]): Promise<AdminUser> {
    const url = `${API_ROOT}/portals/v1/${portalShortcode}/adminUsers/${adminUserId}`
    const response = await fetch(url, {
      method: 'PATCH',
      headers: this.getInitHeaders(),
      body: JSON.stringify(roleNames)
    })
    return await this.processJsonResponse(response)
  },

  /** removes an admin user, and all associated portal users (this should be superuser only) */
  async removeAdminUser(adminUser: AdminUser): Promise<Response> {
    const url = `${API_ROOT}/adminUsers/v1/${adminUser.id}`
    const response = await fetch(url, {
      method: 'DELETE',
      headers: this.getInitHeaders()
    })
    return await this.processResponse(response)
  },

  async removePortalUser(adminUser: AdminUser, portalShortcode: string): Promise<Response> {
    const url = `${API_ROOT}/portals/v1/${portalShortcode}/adminUsers/${adminUser.id}`
    const response = await fetch(url, {
      method: 'DELETE',
      headers: this.getInitHeaders()
    })
    return await this.processResponse(response)
  },

  async fetchRoles(): Promise<Role[]> {
    const url = `${API_ROOT}/roles/v1`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
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

  async setPortalEnvLanguages(portalShortcode: string, envName: string, languages: PortalEnvironmentLanguage[]) {
    const url = `${basePortalEnvUrl(portalShortcode, envName)}/portalLanguages`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(languages)
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

  async getFamily(
    portalShortcode: string, studyShortcode: string, environmentName: EnvironmentName, familyShortcodeOrId: string
  ): Promise<Family> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, environmentName)}/families/${familyShortcodeOrId}`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async getAllFamilies(
    portalShortcode: string, studyShortcode: string, environmentName: EnvironmentName
  ): Promise<Family[]> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, environmentName)}/families`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async addMemberToFamily(
    portalShortcode: string, studyShortcode: string, environmentName: EnvironmentName,
    familyShortcode: string, enrolleeShortcode: string, justification: string
  ) {
    const params = queryString.stringify({ justification })
    const url = `${
      baseStudyEnvUrl(portalShortcode, studyShortcode, environmentName)
    }/families/${familyShortcode}/members/${enrolleeShortcode}?${params}`

    const result = await fetch(url, {
      method: 'PUT',
      headers: this.getInitHeaders()
    })
    return await this.processResponse(result)
  },

  async removeMemberFromFamily(
    portalShortcode: string, studyShortcode: string, environmentName: EnvironmentName,
    familyShortcode: string, enrolleeShortcode: string, justification: string
  ) {
    const params = queryString.stringify({ justification })
    const url = `${
      baseStudyEnvUrl(portalShortcode, studyShortcode, environmentName)
    }/families/${familyShortcode}/members/${enrolleeShortcode}?${params}`

    const result = await fetch(url, {
      method: 'DELETE',
      headers: this.getInitHeaders()
    })
    return await this.processResponse(result)
  },

  async updateProband(
    portalShortcode: string, studyShortcode: string, environmentName: EnvironmentName,
    familyShortcode: string, enrolleeShortcode: string, justification: string
  ): Promise<Family> {
    const params = queryString.stringify({ justification })
    const url = `${
      baseStudyEnvUrl(portalShortcode, studyShortcode, environmentName)
    }/families/${familyShortcode}/proband/${enrolleeShortcode}?${params}`

    const result = await fetch(url, {
      method: 'PATCH',
      headers: this.getInitHeaders()
    })
    return await this.processJsonResponse(result)
  },

  async createFamily(
    portalShortcode: string, studyShortcode: string, environmentName: EnvironmentName,
    family: Family, justification: string
  ): Promise<Family> {
    const params = queryString.stringify({ justification })
    const url = `${
      baseStudyEnvUrl(portalShortcode, studyShortcode, environmentName)
    }/families?${params}`

    const result = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(family)
    })
    return await this.processJsonResponse(result)
  },

  async deleteFamily(
    portalShortcode: string, studyShortcode: string, environmentName: EnvironmentName,
    familyShortcode: string, justification: string
  ): Promise<Response> {
    const params = queryString.stringify({ justification })
    const url = `${
      baseStudyEnvUrl(portalShortcode, studyShortcode, environmentName)
    }/families/${familyShortcode}?${params}`

    const result = await fetch(url, {
      method: 'DELETE',
      headers: this.getInitHeaders()
    })
    return await this.processResponse(result)
  },

  async fetchFamilyChangeRecords(
    portalShortcode: string, studyShortcode: string, environmentName: EnvironmentName,
    familyShortcode: string, modelName?: string
  ): Promise<DataChangeRecord[]> {
    const params = queryString.stringify({ modelName })
    const url = `${
      baseStudyEnvUrl(portalShortcode, studyShortcode, environmentName)
    }/families/${familyShortcode}/changeRecords?${params}`

    const result = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(result)
  },

  async fetchEnvDiff(portalShortcode: string, sourceEnvName: string, destEnvName: string):
    Promise<PortalEnvironmentChange> {
    const url = `${basePortalUrl(portalShortcode)}/publish/diff/${sourceEnvName}/${destEnvName}`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async applyEnvChanges(portalShortcode: string, destEnvName: string, changes: PortalEnvironmentChange):
    Promise<PortalEnvironment> {
    const url = `${basePortalUrl(portalShortcode)}/publish/apply/${destEnvName}`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(changes)
    })
    return await this.processJsonResponse(response)
  },

  async fetchPortalEnvChangeRecords(portalShortcode: string):
    Promise<PortalEnvironmentChangeRecord[]> {
    const url = `${basePortalUrl(portalShortcode)}/publish/changeRecords`
    const response = await fetch(url, this.getGetInit())
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
    const params = queryString.stringify({ filePathName: fileName, overwrite, shortcodeOverride })
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

  async populateEnrollee(studyEnvParams: StudyEnvParams, popType: string, username?: string) {
    const paramString = queryString.stringify({ username, popType })
    const url = `${baseStudyEnvUrlFromParams(studyEnvParams)}/enrollee/populate?${paramString}`

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

  async populateCommand(command: string, params: object) {
    const url = `${basePopulateUrl()}/command/${command}`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(params)
    })
    return await this.processJsonResponse(response)
  },

  async loadLogEvents(eventTypes: string[], days: string, limit: number = 1000) {
    const params = queryString.stringify({ eventTypes: eventTypes.join(','), days, limit })
    const url = `${API_ROOT}/logEvents?${params}`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async getSystemStatus(): Promise<SystemStatus> {
    const url = `/status`
    const response = await fetch(url, {
      method: 'GET',
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
export function getMediaUrl(portalShortcode: string, cleanFileName: string, version: number | 'latest') {
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
