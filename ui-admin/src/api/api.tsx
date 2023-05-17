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

export type Study = {
  name: string,
  shortcode: string,
  studyEnvironments: StudyEnvironment[]
}

export type StudyEnvironmentUpdate = {
  id: string,
  preEnrollSurveyId: string
}

export type StudyEnvironment = {
  id: string,
  environmentName: string,
  studyEnvironmentConfig: StudyEnvironmentConfig,
  preEnrollSurvey: Survey,
  preEnrollSurveyId: string,
  configuredSurveys: StudyEnvironmentSurvey[],
  configuredConsents: StudyEnvironmentConsent[],
  notificationConfigs: NotificationConfig[]
}

export type VersionedForm = {
  id: string,
  name: string,
  stableId: string,
  version: number,
  createdAt: string,
  content: string
}

export type Survey = VersionedForm

export type ConsentForm = VersionedForm

export type StudyEnvironmentSurvey = {
  id: string,
  surveyId: string,
  survey: Survey,
  recur: boolean,
  recurrenceIntervalDays: number,
  surveyOrder: number,
  allowAdminEdit: boolean,
  allowParticipantStart: boolean,
  allowParticipantReedit: boolean,
  prepopulate: boolean
}

export type StudyEnvironmentConsent = {
  id: string,
  consentFormId: string,
  consentForm: ConsentForm,
  consentOrder: number,
  allowAdminEdit: boolean,
  allowParticipantStart: boolean,
  allowParticipantReedit: boolean,
  prepopulate: boolean
}

export type StudyEnvironmentConfig = {
  passwordProtected: boolean,
  password: string,
  acceptingEnrollment: boolean,
  initialized: boolean
}


export type PortalStudy = {
  study: Study
}

export type Portal = {
  id?: string,
  name: string,
  shortcode: string,
  portalStudies: PortalStudy[],
  portalEnvironments: PortalEnvironment[]
}

export type PortalEnvironment = {
  environmentName: string,
  portalEnvironmentConfig: PortalEnvironmentConfig,
  siteContent?: SiteContent
}

export type SiteContent = {
  defaultLanguage: string,
  localizedSiteContents: LocalSiteContent[],
  stableId: string,
  version: number
}

export type LocalSiteContent = {
  language: string,
  navbarItems: NavbarItem[],
  landingPage: HtmlPage,
  navLogoCleanFileName: string,
  navLogoVersion: number,
  footerSection?: HtmlSection
  primaryBrandColor?: string
}

export type HtmlPage = {
  title: string,
  path: string,
  sections: HtmlSection[]
}

export type NavbarItem = {
  label: string,
  externalLink?: string,
  anchorLinkPath?: string,
  itemType: string
  htmlPage?: HtmlPage
}

export type NavbarItemInternal = NavbarItem & {
  htmlPage: HtmlPage
}

export type NavbarItemInternalAnchor = NavbarItem & {
  anchorLinkPath: string
}

/** type predicate for handling internal links */
export function isInternalLink(navItem: NavbarItem): navItem is NavbarItemInternal {
  return navItem.itemType === 'INTERNAL'
}

/** type predicate for handling internal anchor links */
export function isInternalAnchorLink(navItem: NavbarItem): navItem is NavbarItemInternalAnchor {
  return navItem.itemType === 'INTERNAL_ANCHOR'
}

export type SectionType =
  | 'BANNER_IMAGE'
  | 'FAQ'
  | 'HERO_CENTERED'
  | 'HERO_WITH_IMAGE'
  | 'LINK_SECTIONS_FOOTER'
  | 'PARTICIPATION_DETAIL'
  | 'PHOTO_BLURB_GRID'
  | 'RAW_HTML'
  | 'SOCIAL_MEDIA'
  | 'STEP_OVERVIEW'

export type HtmlSection = {
  id: string,
  sectionType: SectionType,
  anchorRef?: string,
  rawContent?: string | null,
  sectionConfig?: string | null
}

export type PortalEnvironmentConfig = {
  acceptingRegistration: boolean,
  password: string,
  passwordProtected: boolean,
  initialized: boolean
  participantHostname?: string,
  emailSourceAddress?: string
}

export type EnrolleeSearchResult = {
  enrollee: Enrollee,
  profile: Profile
}

export type Enrollee = {
  shortcode: string,
  surveyResponses: SurveyResponse[],
  consentResponses: ConsentResponse[],
  preRegResponse?: PreregistrationResponse,
  preEnrollmentResponse?: PreregistrationResponse,
  participantTasks: ParticipantTask[],
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
  birthDate: number
}

export type MailingAddress = {
  street1: string,
  street2: string,
  city: string,
  state: string,
  country: string,
  postalCode: string
}

export type ResumableData = {
  currentPageNo: number,
  data: object
}

export type ResponseSnapshot = {
  createdAt: string,
  resumeData: string,
  fullData: string
}

export type SurveyResponse = {
  complete: boolean,
  createdAt: number, // this is a java instant, so number of seconds since epoch start
  lastUpdatedAt: string,
  surveyId: string,
  surveyStableId: string,
  surveyVersion: string,
  answers: Answer[]
}

export type Answer = {
  stringValue: string,
  numberValue: number,
  objectValue: object,
  booleanValue: boolean,
  questionStableId: string
}

export type PreregistrationResponse = {
  createdAt: string,
  fullData: string,
  surveyStableId: string,
  surveyVersion: string
}

export type ConsentResponse = {
  id: string,
  createdAt: number,
  consented: boolean,
  consentFormId: string,
  fullData: string
}

export type ParticipantTask = {
  id: string,
  completedAt?: number,
  status: string,
  taskType: string,
  targetName: string,
  taskOrder: number,
  blocksHub: boolean,
}

export type NotificationConfig = {
  id: string,
  studyEnvironmentId: string,
  portalEnvironmentId: string,
  active: boolean,
  notificationType: string,
  deliveryType: string,
  rule: string,
  eventType: string,
  taskType: string,
  taskTargetStableId: string,
  afterMinutesIncomplete: number,
  reminderIntervalMinutes: number,
  maxNumReminders: number,
  emailTemplateId: string,
  emailTemplate: EmailTemplate
}

export type EmailTemplate = {
  subject: string,
  body: string
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
  changed: boolean,
  oldStableId: string,
  newStableId: string,
  oldVersion: number,
  newVersion: number
}

export type ConfigChange = {
  propertyName: string,
  oldValue: object,
  newValue: object
}

export type ListChange<T, CT> = {
  addedItems: T[],
  removedItems: T[],
  changedItems: CT[]
}

export type VersionedConfigChange = {
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
  lastUpdatedAt: number,
  studyEnvironmentId: string,
  datasetId: string,
  datasetName: string,
  lastExported: number
}

export type DatasetJobHistory = {
  id: string,
  createdAt: number,
  lastUpdatedAt: number,
  studyEnvironmentId: string,
  tdrJobId: string,
  datasetName: string,
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

  async getEnrollees(portalShortcode: string, studyShortcode: string, envName: string):
    Promise<EnrolleeSearchResult[]> {
    const url =`${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/enrollees`
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
    const url = `${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)
    }/enrollees/${enrolleeShortcode}/changeRecords`
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

  listDatasetsForStudyEnvironment(portalShortcode: string, studyShortcode: string,
    envName: string):
      Promise<Response> {
    const url =`${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/datarepo/datasets`
    return fetch(url,  this.getGetInit())
  },

  getJobHistoryForDataset(portalShortcode: string, studyShortcode: string,
    envName: string, datasetName: string):
      Promise<Response> {
    const url =`${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/datarepo/datasets/${datasetName}/jobs`
    return fetch(url,  this.getGetInit())
  },

  async createDatasetForStudyEnvironment(portalShortcode: string, studyShortcode: string,
    envName: string, datasetName: { name: string }):
      Promise<Response> {
    const url =`${baseStudyEnvUrl(portalShortcode, studyShortcode, envName)}/datarepo/datasets`
    return await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(datasetName)
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
function baseStudyEnvUrl(portalShortcode: string, studyShortcode: string, envName: string) {
  return `${API_ROOT}/portals/v1/${portalShortcode}/studies/${studyShortcode}/env/${envName}`
}
