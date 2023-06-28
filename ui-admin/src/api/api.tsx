import {
  ConsentForm,
  Survey,
  ConsentResponse,
  NotificationConfig,
  ParticipantTask,
  Portal,
  PortalEnvironment,
  PortalEnvironmentConfig,
  StudyEnvironmentConsent,
  StudyEnvironmentSurvey,
  SurveyResponse,
  PreregistrationResponse
} from '@juniper/ui-core'
import { facetValuesToString, FacetValue } from './enrolleeSearch'

export type {
  Answer,
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
  NotificationConfig,
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

export type AdminUser = {
  username: string,
  token: string,
  superuser: boolean,
  portalPermissions: Record<string, string[]>,
  isAnonymous: boolean,
  portalAdminUsers?: PortalAdminUser[]
};

export type NewAdminUser = {
  username: string,
  superuser: boolean,
  portalShortcode: string | null
}

export type PortalAdminUser = {
  portalId: string
}

export type StudyEnvironmentUpdate = {
  id: string,
  preEnrollSurveyId: string
}

export type EnrolleeSearchResult = {
  enrollee: Enrollee,
  profile: Profile,
  mostRecentKitStatus: string | null
}

export type Enrollee = {
  id: string,
  shortcode: string,
  surveyResponses: SurveyResponse[],
  consentResponses: ConsentResponse[],
  preRegResponse?: PreregistrationResponse,
  preEnrollmentResponse?: PreregistrationResponse,
  participantTasks: ParticipantTask[],
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
  birthDate: number[]
}

export type MailingAddress = {
  street1: string,
  street2: string,
  city: string,
  state: string,
  country: string,
  postalCode: string
}

export type Notification = {
  id: string,
  notificationConfigId: string,
  deliveryStatus: string,
  deliveryType: string,
  sentTo: string,
  createdAt: number,
  lastUpdatedAt: number,
  retries: number,
  notificationConfig?: NotificationConfig
}

export type DataChangeRecord = {
  id: string,
  createdAt: number,
  modelName: string,
  fieldName: string,
  oldValue: string,
  newValue: string,
  responsibleUserId: string,
  responsibleAdminUserId: string
}

export type KitType = {
  id: string,
  name: string,
  displayName: string,
  description: string
}

export type KitRequest = {
  id: string,
  createdAt: number,
  kitType: KitType,
  sentToAddress: string,
  status: string
}

export type Config = {
  b2cTenantName: string,
  b2cClientId: string,
  b2cPolicyName: string,
  participantUiHostname: string,
  participantApiHostname: string,
  adminUiHostname: string,
  adminApiHostname: string
}

export type MailingListContact = {
  name: string,
  email: string,
  createdAt: number
}


export type PortalEnvironmentChange = {
  siteContentChange: VersionedEntityChange,
  configChanges: ConfigChange[],
  preRegSurveyChanges: VersionedEntityChange,
  notificationConfigChanges: ListChange<NotificationConfig, VersionedConfigChange>
  studyEnvChanges: StudyEnvironmentChange[]
}

export type StudyEnvironmentChange = {
  studyShortcode: string,
  configChanges: ConfigChange[],
  preEnrollSurveyChanges: VersionedEntityChange,
  consentChanges: ListChange<StudyEnvironmentConsent, VersionedConfigChange>,
  surveyChanges: ListChange<StudyEnvironmentSurvey, VersionedConfigChange>,
  notificationConfigChanges: ListChange<NotificationConfig, VersionedConfigChange>
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

export type ConfigChange = {
  propertyName: string,
  oldValue: object | boolean,
  newValue: object | boolean
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
    return Promise.reject(response)
  },

  async getConfig(): Promise<Config> {
    const response = await fetch(`/config`)
    return await this.processJsonResponse(response)
  },

  async unauthedLogin(username: string): Promise<AdminUser> {
    const url =`${API_ROOT}/current-user/v1/unauthed/login?${  new URLSearchParams({
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
    const url =`${API_ROOT}/current-user/v1/unauthed/refresh`
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
    const url =`${API_ROOT}/current-user/v1/login`
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
    const url =`${API_ROOT}/current-user/v1/refresh`
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

  async createNewSurveyVersion(portalShortcode: string, survey: Survey): Promise<Survey> {
    const url = `${API_ROOT}/portals/v1/${portalShortcode}/surveys/${survey.stableId}/newVersion`

    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(survey)
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

  async updateStudyEnvironment(portalShortcode: string, studyShortcode: string, envName: string,
    studyEnvUpdate: StudyEnvironmentUpdate): Promise<StudyEnvironmentUpdate> {
    const url = `${API_ROOT}/portals/v1/${portalShortcode}/studies/${studyShortcode}/env/${envName}`
    const response = await fetch(url, {
      method: 'PATCH',
      headers: this.getInitHeaders(),
      body: JSON.stringify(studyEnvUpdate)
    })
    return await this.processJsonResponse(response)
  },

  async getSurveyVersions(studyShortname: string, stableId: string) {
    const response = await fetch(`${API_ROOT}/studies/${studyShortname}/surveys/${stableId}`, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async updateConfiguredSurvey(portalShortcode: string, studyShortcode: string, environmentName: string,
    configuredSurvey: StudyEnvironmentSurvey): Promise<StudyEnvironmentSurvey> {
    const url =`${API_ROOT}/portals/v1/${portalShortcode}/studies/${studyShortcode}` +
      `/env/${environmentName}/configuredSurveys/${configuredSurvey.id}`

    const response = await fetch(url, {
      method: 'PATCH',
      headers: this.getInitHeaders(),
      body: JSON.stringify(configuredSurvey)
    })
    return await this.processJsonResponse(response)
  },

  async updateConfiguredConsent(portalShortcode: string, studyShortcode: string, environmentName: string,
    configuredConsent: StudyEnvironmentConsent): Promise<StudyEnvironmentConsent> {
    const url =`${API_ROOT}/portals/v1/${portalShortcode}/studies/${studyShortcode}` +
      `/env/${environmentName}/configuredConsents/${configuredConsent.id}`

    const response = await fetch(url, {
      method: 'PATCH',
      headers: this.getInitHeaders(),
      body: JSON.stringify(configuredConsent)
    })
    return await this.processJsonResponse(response)
  },

  async searchEnrollees(portalShortcode: string, studyShortcode: string, envName: string, facetValues: FacetValue[]):
    Promise<EnrolleeSearchResult[]> {
    const facetString = encodeURIComponent(facetValuesToString(facetValues))
    const url =`${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/enrollees?facets=${facetString}`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async getEnrollee(portalShortcode: string, studyShortcode: string, envName: string, enrolleeShortcode: string):
    Promise<Enrollee> {
    const url =`${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/enrollees/${enrolleeShortcode}`
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

  async createKitRequest(
    portalShortcode: string,
    studyShortcode: string,
    envName: string,
    enrolleeShortcode: string,
    kitType: string
  ): Promise<string> {
    const params = new URLSearchParams({ kitType })
    const url =
      `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/enrollees/${enrolleeShortcode}/requestKit?${params}`
    const response = await fetch(url, { method: 'POST', headers: this.getInitHeaders() })
    return await this.processJsonResponse(response)
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

  async fetchKitTypes(portalShortcode: string, studyShortcode: string): Promise<KitType[]> {
    const url = `${baseStudyUrl(portalShortcode, studyShortcode)}/kitTypes`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async testNotification(portalShortcode: string, envName: string,
    notificationConfigId: string, enrolleeRuleData: object): Promise<NotificationConfig> {
    const url = `${basePortalEnvUrl(portalShortcode, envName)}/notificationConfigs/${notificationConfigId}`
      + `/test`

    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(enrolleeRuleData)
    })
    return await this.processJsonResponse(response)
  },

  async fetchMetric(portalShortcode: string, studyShortcode: string, envName: string, metricName: string):
    Promise<BasicMetricDatum[]> {
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/metrics/${metricName}`
    const response = await fetch(url,  this.getGetInit())
    return await this.processJsonResponse(response)
  },

  exportEnrollees(portalShortcode: string, studyShortcode: string,
    envName: string, exportOptions: ExportOptions):
    Promise<Response> {
    const exportOptionsParams = exportOptions as Record<string, unknown>
    let url =`${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/export/data?`
    const searchParams = new URLSearchParams()
    for (const prop in exportOptionsParams) {
      searchParams.set(prop, (exportOptionsParams[prop] as string | boolean).toString())
    }
    url += searchParams.toString()
    return fetch(url,  this.getGetInit())
  },

  exportDictionary(portalShortcode: string, studyShortcode: string,
    envName: string, exportOptions: ExportOptions):
    Promise<Response> {
    const exportOptionsParams = exportOptions as Record<string, unknown>
    let url =`${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/export/dictionary?`
    const searchParams = new URLSearchParams()
    for (const prop in exportOptionsParams) {
      searchParams.set(prop, (exportOptionsParams[prop] as string | boolean).toString())
    }
    url += searchParams.toString()
    return fetch(url,  this.getGetInit())
  },

  async findNotificationConfigsForStudyEnv(portalShortcode: string, studyShortcode: string, envName: string):
    Promise<NotificationConfig[]> {
    const url =`${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/notificationConfigs`
    const response = await fetch(url,  this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async sendAdHocNotification({
    portalShortcode, studyShortcode, envName, enrolleeShortcodes,
    customMessages, notificationConfigId
  }:
                          {portalShortcode: string, studyShortcode: string, envName: string,
                            enrolleeShortcodes: string[], customMessages: Record<string, string>,
                            notificationConfigId: string}): Promise<Response> {
    const url =`${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/notifications/adhoc`
    return await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify({
        notificationConfigId,
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
    const url =`${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/datarepo/datasets`
    return await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(createDataset)
    })
  },

  async deleteDatasetForStudyEnvironment(portalShortcode: string, studyShortcode: string,
    envName: string, datasetName: string):
      Promise<Response> {
    const url =`${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/datarepo/datasets/${datasetName}`
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

  async fetchAdminUsers(): Promise<AdminUser[]> {
    const url = `${API_ROOT}/adminUsers/v1`
    const response = await fetch(url, this.getGetInit())
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

  async fetchEnvDiff(portalShortcode: string, sourceEnvName: string, destEnvName: string):
    Promise<PortalEnvironmentChange> {
    const url = `${basePortalEnvUrl(portalShortcode, destEnvName)}/update/diff?sourceEnv=${sourceEnvName}`
    const response = await fetch(url, this.getGetInit())
    return await this.processJsonResponse(response)
  },

  async applyEnvChanges(portalShortcode: string, destEnvName: string, changes: PortalEnvironmentChange):
    Promise<PortalEnvironment> {
    const url = `${basePortalEnvUrl(portalShortcode, destEnvName)}/update/apply`
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

/** base api path for study-scoped api requests */
function basePortalEnvUrl(portalShortcode: string, envName: string) {
  return `${API_ROOT}/portals/v1/${portalShortcode}/env/${envName}`
}

/** base api path for study-scoped api requests */
function baseStudyUrl(portalShortcode: string, studyShortcode: string) {
  return `${API_ROOT}/portals/v1/${portalShortcode}/studies/${studyShortcode}`
}

/** base api path for study-scoped api requests */
function baseStudyEnvUrl(portalShortcode: string, studyShortcode: string, envName: string) {
  return `${API_ROOT}/portals/v1/${portalShortcode}/studies/${studyShortcode}/env/${envName}`
}
