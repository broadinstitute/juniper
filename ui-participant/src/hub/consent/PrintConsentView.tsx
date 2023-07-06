import React, { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { Model } from 'survey-core'
import { Survey as SurveyComponent } from 'survey-react-ui'

import { surveyJSModelFromForm } from '@juniper/ui-core'

import Api, { Answer, Enrollee } from 'api/api'
import { usePortalEnv } from 'providers/PortalProvider'
import { useUser } from 'providers/UserProvider'
import { DocumentTitle } from 'util/DocumentTitle'
import { PageLoadingIndicator } from 'util/LoadingSpinner'
import { makeSurveyJsData } from '@juniper/ui-core'

import { enrolleeForStudy } from './ConsentView'

/**
 * If window.print is called immediately, some images might not have been loaded
 * and won't be included in the printed output. This returns a promise that
 * resolves after all images on the page have loaded.
 */
const waitForImages = () => new Promise(resolve => {
  const images = Array.from(document.querySelectorAll('img'))
  if (images.length === 0) {
    resolve(undefined)
    return
  }

  let nComplete = 0

  const cb = () => {
    nComplete += 1
    if (nComplete === images.length) {
      resolve(undefined)
    }
  }

  for (const image of images) {
    const clone = new Image()
    clone.onload = cb
    clone.onerror = cb
    clone.src = image.src
  }
})

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

      surveyModel.mode = 'display'
      surveyModel.questionsOnPageMode = 'singlePage'
      surveyModel.showProgressBar = 'off'
      surveyModel.setVariable('portalEnvironmentName', portalEnv.environmentName)

      return surveyModel
    }

    setLoading(true)
    loadConsent()
      .then(setSurveyModel)
      .catch(() => {
        alert('Error loading consent form - please retry')
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
