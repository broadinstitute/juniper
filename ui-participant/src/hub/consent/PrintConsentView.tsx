import React, { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Model } from 'survey-core'
import { Survey as SurveyComponent } from 'survey-react-ui'

import {
  surveyJSModelFromForm, makeSurveyJsData,
  waitForImages, configureModelForPrint
} from '@juniper/ui-core'

import Api, { Answer, Enrollee } from 'api/api'
import { usePortalEnv } from 'providers/PortalProvider'
import { useUser } from 'providers/UserProvider'
import { DocumentTitle } from 'util/DocumentTitle'
import { PageLoadingIndicator } from 'util/LoadingSpinner'
import { enrolleeForStudy } from './ConsentView'

type UsePrintableConsentArgs = {
  studyShortcode: string
  enrollee: Enrollee
  stableId: string
  version: number
}

const usePrintableConsent = (args: UsePrintableConsentArgs) => {
  const { studyShortcode, enrollee, stableId, version } = args

  const { portalEnv } = usePortalEnv()

  const [loading, setLoading] = useState(true)
  const [surveyModel, setSurveyModel] = useState<Model | null>(null)
  const navigate = useNavigate()
  useEffect(() => {
    const loadConsent = async () => {
      const consentAndResponses = await Api.fetchConsentAndResponses({
        studyShortcode,
        enrolleeShortcode: enrollee.shortcode,
        stableId,
        version
      })

      const form = consentAndResponses.studyEnvironmentConsent.consentForm
      const response = consentAndResponses.consentResponses[0]
      let answers: Answer[] = []
      if (response?.fullData) {
        answers = JSON.parse(response.fullData)
      }
      const resumableData = makeSurveyJsData(response?.resumeData, answers, enrollee.participantUserId)

      const surveyModel = surveyJSModelFromForm(form)
      surveyModel.title = form.name
      surveyModel.data = resumableData?.data
      configureModelForPrint(surveyModel)
      surveyModel.setVariable('portalEnvironmentName', portalEnv.environmentName)

      return surveyModel
    }

    setLoading(true)
    loadConsent()
      .then(setSurveyModel)
      .catch(() => {
        navigate('/hub')
      })
      .finally(() => {
        setLoading(false)
      })
  }, [studyShortcode, enrollee, stableId, version])

  return { loading, surveyModel }
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
const PrintConsentView = () => {
  const { portal } = usePortalEnv()
  const { enrollees } = useUser()
  const params = useParams()
  const stableId = params.stableId
  const version = parseInt(params.version ?? '')
  const studyShortcode = params.studyShortcode
  if (!stableId || !version || !studyShortcode) {
    return <div>You must specify study, form, and version</div>
  }
  const enrollee = enrolleeForStudy(enrollees, studyShortcode, portal)

  const { loading, surveyModel } = usePrintableConsent({ studyShortcode, enrollee, stableId, version })

  useEffect(() => {
    if (surveyModel) {
      waitForImages().then(() => { window.print() })
    }
  }, [surveyModel])

  if (loading) {
    return <PageLoadingIndicator/>
  } else if (!surveyModel) {
    return null
  } else {
    return (
      <>
        <DocumentTitle title={surveyModel.title} />
        <SurveyComponent model={surveyModel} />
      </>
    )
  }
}

export default PrintConsentView
