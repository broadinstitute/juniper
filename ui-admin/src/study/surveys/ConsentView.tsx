import React, { useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { Store } from 'react-notifications-component'

import {  StudyParams } from 'study/StudyRouter'
import { StudyEnvContextT, studyEnvFormsPath } from 'study/StudyEnvironmentRouter'
import Api, {
  ConsentForm,
  StudyEnvironmentConsent
} from 'api/api'

import { successNotification } from 'util/notifications'
import SurveyEditorView from './SurveyEditorView'
import { doApiLoad, useLoadingEffect } from 'api/api-utils'
import LoadingSpinner from 'util/LoadingSpinner'

/** Handles logic for updating study environment surveys */
function RawConsentView({ studyEnvContext, consent, readOnly = false }:
                         {studyEnvContext: StudyEnvContextT,
                           consent: ConsentForm, readOnly?: boolean}) {
  const { portal, study, currentEnv } = studyEnvContext
  const navigate = useNavigate()

  const [currentForm, setCurrentForm] = useState(consent)
  /** saves the survey as a new version */
  async function createNewVersion({ content: updatedTextContent }: { content: string }): Promise<void> {
    consent.content = updatedTextContent
    doApiLoad(async () => {
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
    })
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
      studyEnvContext={studyEnvContext}
      currentForm={currentForm}
      readOnly={readOnly}
      onCancel={() => navigate(studyEnvFormsPath(portal.shortcode, study.shortcode, currentEnv.environmentName))}
      onSave={createNewVersion}
    />
  )
}

export type ConsentParamsT = StudyParams & {
  consentStableId: string,
  version: string
}

/** loads a consentForm */
export const useLoadedConsentForm = (portalShortcode: string, stableId: string, version: number) => {
  const [form, setForm] = useState<ConsentForm>()

  /** load the survey from the server to get answer mappings and ensure we've got the latest content */
  const { isLoading } = useLoadingEffect(async () => {
    const form = await Api.getConsentForm(portalShortcode, stableId, version)
    setForm(form)
  }, [portalShortcode, stableId, version])

  return { isLoading, form }
}

/** read consent-form-related url params */
export const useConsentParams = () => {
  const params = useParams<ConsentParamsT>()
  const [searchParams] = useSearchParams()
  const isReadOnly = searchParams.get('readOnly') === 'true'
  const version = params.version ? parseInt(params.version) : undefined

  return { isReadOnly, version, stableId: params.consentStableId }
}

/** routable component for survey editing */
function ConsentView({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { isReadOnly, version, stableId } = useConsentParams()
  const { currentEnv } = studyEnvContext
  if (!stableId) {
    return <span>you need to specify the stableId of the consentForm</span>
  }
  const applyReadOnly = isReadOnly || currentEnv.environmentName !== 'sandbox'
  const configuredForm = currentEnv.configuredConsents
    .find(s => s.consentForm.stableId === stableId)?.consentForm
  const appliedVersion = version || configuredForm?.version

  if (!appliedVersion) {
    return <span>The consent form {stableId} is not already configured for this environment
      -- you must specify a version</span>
  }

  const { isLoading, form } = useLoadedConsentForm(studyEnvContext.portal.shortcode,
    stableId, appliedVersion)
  return <>
    { isLoading && <LoadingSpinner/> }
    { !isLoading && form && <RawConsentView studyEnvContext={studyEnvContext}
      consent={form} readOnly={applyReadOnly}/> }
  </>
}

export default ConsentView
