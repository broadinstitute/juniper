import React, { useState } from 'react'
import { useParams, useSearchParams } from 'react-router-dom'
import { Store } from 'react-notifications-component'

import {  StudyParams } from 'study/StudyProvider'
import { useStudyEnvironmentOutlet } from 'study/StudyEnvironmentProvider'
import Api, {
  ConsentForm,
  Portal,
  Study,
  StudyEnvironment,
  StudyEnvironmentConsent,
  StudyEnvironmentSurvey,
  Survey
} from 'api/api'

import { failureNotification, successNotification } from 'util/notifications'
import SurveyEditorView from './SurveyEditorView'

/** Handles logic for updating study environment surveys */
function RawConsentView({ portal, study, currentEnv, consent, readOnly = false }:
                         {portal: Portal, study: Study, currentEnv: StudyEnvironment,
                           consent: ConsentForm, readOnly?: boolean}) {
  const [currentForm, setCurrentForm] = useState(consent)
  /** saves the survey as a new version */
  async function createNewVersion(updatedTextContent: string): Promise<string> {
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
    return updatedTextContent
  }

  /** Syncs the survey with one from the server */
  function updateFromServer(updatedConsent: ConsentForm, updatedConfiguredConsent: StudyEnvironmentConsent) {
    setCurrentForm(updatedConsent)
    updatedConfiguredConsent.consentForm = updatedConsent
    const configuredIndex = currentEnv.configuredConsents
      .findIndex(s => s.consentForm.stableId === updatedConsent.stableId)
    currentEnv.configuredConsents[configuredIndex] = updatedConfiguredConsent
  }

  /** resets the survey to a previous version */
  async function changeVersion(version: number) {
    // setShowVersionSelector(false)
    alert(`not implemented ${  version}`)
    try {
      // const updatedSurvey = await Api.updateConfiguredSurvey(portal.shortcode, currentEnv.environmentName,
      //   currentSurvey.stableId, version)
      // updateSurveyFromServer(updatedSurvey)
      // Store.addNotification(successNotification(`Set to version ${updatedSurvey.version}`))
    } catch (e) {
      Store.addNotification(failureNotification(`update failed`))
    }
  }

  return <SurveyEditorView portalShortcode={portal.shortcode} currentForm={currentForm} readOnly={readOnly}
    createNewVersion={createNewVersion} changeVersion={changeVersion}/>
}

export type ConsentParamsT = StudyParams & {
  consentStableId: string
}

/** routable component for survey editing */
function SurveyView() {
  const params = useParams<ConsentParamsT>()
  const consentStableId: string | undefined = params.consentStableId

  const { portal, study, currentEnv } = useStudyEnvironmentOutlet()
  const [searchParams] = useSearchParams()
  const isReadonly = searchParams.get('mode') === 'view'

  if (!consentStableId) {
    return <span>you need to specify the stableId of the consentForm</span>
  }

  const consent = currentEnv.configuredConsents.find(s => s.consentForm.stableId === consentStableId)?.consentForm
  if (!consent) {
    return <span>The consent {consentStableId} does not exist in this environment</span>
  }
  return <RawConsentView portal={portal} study={study} currentEnv={currentEnv} consent={consent} readOnly={isReadonly}/>
}

export default SurveyView
