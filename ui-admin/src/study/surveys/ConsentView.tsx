import React, { useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { Store } from 'react-notifications-component'

import {  StudyParams } from 'study/StudyRouter'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Api, {
  ConsentForm,
  Portal,
  Study,
  StudyEnvironment,
  StudyEnvironmentConsent
} from 'api/api'

import { failureNotification, successNotification } from 'util/notifications'
import SurveyEditorView from './SurveyEditorView'
import { useUser } from 'user/UserProvider'

/** Handles logic for updating study environment surveys */
function RawConsentView({ portal, study, currentEnv, consent, readOnly = false }:
                         {portal: Portal, study: Study, currentEnv: StudyEnvironment,
                           consent: ConsentForm, readOnly?: boolean}) {
  const { user } = useUser()
  const navigate = useNavigate()

  const [currentForm, setCurrentForm] = useState(consent)
  /** saves the survey as a new version */
  async function createNewVersion({ content: updatedTextContent }: { content: string }): Promise<void> {
    if (!user.superuser) {
      Store.addNotification(failureNotification('you do not have permissions to save surveys'))
      return
    }

    consent.content = updatedTextContent
    try {
      const updatedConsent = await Api.createNewConsentVersion(portal.shortcode, currentForm)
      const configuredSurvey = currentEnv.configuredConsents
        .find(cf => cf.consentForm.stableId === updatedConsent.stableId) as StudyEnvironmentConsent
      const updatedConfig = { ...configuredSurvey, consentFormId: updatedConsent.id, survey: updatedConsent }
      const updatedConfiguredConsent = await Api.updateConfiguredConsent(portal.shortcode,
        study.shortcode, currentEnv.environmentName, updatedConfig)
      Store.addNotification(successNotification(
        `Updated ${currentEnv.environmentName} to version ${updatedConsent.version}`
      ))
      updateFromServer(updatedConsent, updatedConfiguredConsent)
      updatedTextContent = updatedConsent.content
    } catch (e) {
      Store.addNotification(failureNotification(`save failed`))
    }
  }

  /** Syncs the survey with one from the server */
  function updateFromServer(updatedConsent: ConsentForm, updatedConfiguredConsent: StudyEnvironmentConsent) {
    setCurrentForm(updatedConsent)
    updatedConfiguredConsent.consentForm = updatedConsent
    const configuredIndex = currentEnv.configuredConsents
      .findIndex(s => s.consentForm.stableId === updatedConsent.stableId)
    currentEnv.configuredConsents[configuredIndex] = updatedConfiguredConsent
  }

  return (
    <SurveyEditorView
      currentForm={currentForm}
      readOnly={readOnly}
      onCancel={() => navigate('../../..')}
      onSave={createNewVersion}
    />
  )
}

export type ConsentParamsT = StudyParams & {
  consentStableId: string
}

/** routable component for survey editing */
function ConsentView({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const params = useParams<ConsentParamsT>()
  const consentStableId: string | undefined = params.consentStableId

  const { portal, currentEnv, study } = studyEnvContext
  const [searchParams] = useSearchParams()
  const isReadOnly = searchParams.get('readOnly') === 'true'

  if (!consentStableId) {
    return <span>you need to specify the stableId of the consentForm</span>
  }

  const consent = currentEnv.configuredConsents.find(s => s.consentForm.stableId === consentStableId)?.consentForm
  if (!consent) {
    return <span>The consent {consentStableId} does not exist in this environment</span>
  }
  return <RawConsentView portal={portal} study={study} currentEnv={currentEnv} consent={consent} readOnly={isReadOnly}/>
}

export default ConsentView
