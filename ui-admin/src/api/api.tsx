export type AdminUser = {
  username: string,
  token: string
};

export type Study = {
  name: string,
  shortcode: string,
  studyEnvironments: StudyEnvironment[]
}

export type StudyEnvironmentUpdate = {
  id: string,
  preRegSurveyId: string
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
  name: string,
  shortcode: string,
  portalStudies: PortalStudy[]
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
  familyName: string
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
  createdAt: number, // this is a java instant, so number of seconds since epoch start
  lastUpdatedAt: string,
  surveyId: string,
  surveyStableId: string,
  surveyVersion: string,
  snapshots: ResponseSnapshot[],
  lastSnapshot: ResponseSnapshot
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
  completedAt: number,
  status: string,
  taskType: string,
  targetName: string,
  taskOrder: number,
  blocksHub: boolean
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

export type Config = {
  b2cTenantName: string,
  b2cClientId: string
}

let bearerToken: string | null = null
export const API_ROOT = process.env.REACT_APP_API_ROOT
const participantRootPath = process.env.REACT_APP_PARTICIPANT_APP_ROOT
const participantProtocol = process.env.REACT_APP_PARTICIPANT_APP_PROTOCOL


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
    const url =`${API_ROOT}/current-user/v1/unauthed-login?${  new URLSearchParams({
      username
    })}`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders()
    })
    return await this.processJsonResponse(response)
  },

  async tokenLogin(token: string): Promise<AdminUser> {
    const url =`${API_ROOT}/current-user/v1/token-login`
    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify({ token })
    })
    return await this.processJsonResponse(response)
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

  async testNotification(portalShortcode: string, envName: string,
    notificationConfigId: string, enrolleeRuleData: object): Promise<NotificationConfig> {
    const url = `${API_ROOT}/portals/v1/${portalShortcode}/env/${envName}/notificationConfigs/${notificationConfigId}`
      + `/test`

    const response = await fetch(url, {
      method: 'POST',
      headers: this.getInitHeaders(),
      body: JSON.stringify(enrolleeRuleData)
    })
    return await this.processJsonResponse(response)
  },

  getParticipantLink(portalShortcode: string, envName: string): string {
    const participantHost = `${envName}.${portalShortcode}.${participantRootPath}`
    return `${participantProtocol}://${participantHost}`
  },

  setBearerToken(token: string | null) {
    bearerToken = token
  }
}

/** base api path for study-scoped api requests */
function baseStudyEnvUrl(portalShortcode: string, studyShortcode: string, envName: string) {
  return `${API_ROOT}/portals/v1/${portalShortcode}/studies/${studyShortcode}/env/${envName}`
}
