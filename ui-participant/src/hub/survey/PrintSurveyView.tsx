import React, { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Model } from 'survey-core'
import { Survey as SurveyComponent } from 'survey-react-ui'

import {
  surveyJSModelFromForm, makeSurveyJsData,
  waitForImages, configureModelForPrint
} from '@juniper/ui-core'

import Api, { Enrollee } from 'api/api'
import { usePortalEnv } from 'providers/PortalProvider'
import { useUser } from 'providers/UserProvider'
import { DocumentTitle } from 'util/DocumentTitle'
import { PageLoadingIndicator } from 'util/LoadingSpinner'
import { enrolleeForStudy, useTaskIdParam } from './SurveyView'


type UsePrintableConsentArgs = {
  studyShortcode: string
  enrollee: Enrollee
  stableId: string
  version: number
}

/** hook for loading a survey in printable format */
const usePrintableSurvey = (args: UsePrintableConsentArgs) => {
  const { studyShortcode, enrollee, stableId, version } = args

  const { portalEnv } = usePortalEnv()

  const [loading, setLoading] = useState(true)
  const [surveyModel, setSurveyModel] = useState<Model | null>(null)
  const navigate = useNavigate()
  const taskId = useTaskIdParam()

  useEffect(() => {
    const loadForm = async () => {
      const surveyWithResponse = await Api.fetchSurveyAndResponse({
        studyShortcode,
        enrolleeShortcode: enrollee.shortcode,
        stableId,
        version,
        taskId
      })

      const form = surveyWithResponse.studyEnvironmentSurvey.survey
      const response = surveyWithResponse.surveyResponse
      const resumableData = makeSurveyJsData(response?.resumeData, response?.answers, enrollee.participantUserId)

      const surveyModel = surveyJSModelFromForm(form)
      surveyModel.title = form.name
      surveyModel.data = resumableData?.data
      configureModelForPrint(surveyModel)
      surveyModel.setVariable('portalEnvironmentName', portalEnv.environmentName)

      return surveyModel
    }

    setLoading(true)
    loadForm()
      .then(setSurveyModel)
      .catch(() => {
        navigate(`/hub`)
      })
      .finally(() => {
        setLoading(false)
      })
  }, [studyShortcode, enrollee, stableId, version])

  return { loading, surveyModel }
}

/** renders a completed survey and automatically pops up a print dialog */
const PrintSurveyView = () => {
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

  const { loading, surveyModel } = usePrintableSurvey({ studyShortcode, enrollee, stableId, version })

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

export default PrintSurveyView
