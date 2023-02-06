import React, { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import Api, {
  ConsentForm,
  ConsentResponse,
  ConsentWithResponses,
  Enrollee,
  Portal,
  ResumableData,
  StudyEnvironmentConsent
} from 'api/api'

import { Survey as SurveyComponent } from 'survey-react-ui'
import { PageNumberControl, useRoutablePageNumber, useSurveyJSModel } from 'util/surveyJsUtils'
import { usePortalEnv } from 'providers/PortalProvider'
import { useUser } from 'providers/UserProvider'
import LoadingSpinner from 'util/LoadingSpinner'


/**
 * display a single consent form to a participant.  The pageNumber argument can be specified to start at the given
 * page
 */
function RawConsentView({ form, enrollee, response, resumableData, pager, studyShortcode }:
                          {
                            form: ConsentForm, enrollee: Enrollee, response: ConsentResponse,
                            resumableData: ResumableData | null, pager: PageNumberControl, studyShortcode: string
                          }) {
  /** Submit the response to the server */
  function onComplete() {
    if (!surveyModel) {
      return
    }
    alert(`Submitting form for ${enrollee.shortcode} -- ${response?.id} -- ${studyShortcode}.  Not yet implemented`)
  }

  const { surveyModel, pageNumber } = useSurveyJSModel(form, resumableData, onComplete, pager)
  if (surveyModel && resumableData) {
    // consent responses are not editable -- they must be withdrawn via separate workflow
    surveyModel.mode = 'display'
  }

  return <div>
    <h4 className="text-center mt-2">{form.name}</h4>
    {surveyModel && <div className="d-flex align-items-center flex-column">
      <span className="detail">page {pageNumber} of {surveyModel.pages.length}</span>
      <SurveyComponent model={surveyModel}/>
    </div>}
  </div>
}

/** handles paging the form */
function PagedConsentView({ form, responses, enrollee, studyShortcode }:
                            {
                              form: StudyEnvironmentConsent, responses: ConsentResponse[], enrollee: Enrollee,
                              studyShortcode: string
                            }) {
  const response = responses[0]

  let resumableData = null
  if (response?.resumeData) {
    resumableData = JSON.parse(response?.resumeData) as ResumableData
  }
  const pager = useRoutablePageNumber()

  return <RawConsentView enrollee={enrollee} form={form.consentForm} response={response}
    resumableData={resumableData} pager={pager} studyShortcode={studyShortcode}/>
}

/** handles loading the consent form and responses from the server */
export default function ConsentView() {
  const { portal } = usePortalEnv()
  const { enrollees } = useUser()
  const [formAndResponses, setFormAndResponses] = useState<ConsentWithResponses | null>(null)
  const params = useParams()
  const stableId = params.stableId
  const version = parseInt(params.version ?? '')
  const studyShortcode = params.studyShortcode
  if (!stableId || !version || !studyShortcode) {
    return <div>You must specify study, form, and version</div>
  }
  const enrollee = enrolleeForStudy(enrollees, studyShortcode, portal)

  useEffect(() => {
    Api.fetchConsentAndResponses({ studyShortcode, stableId, version, taskId: null })
      .then(response => {
        setFormAndResponses(response)
      }).catch(() => {
        alert('error loading consent form - please retry')
      })
  }, [])

  return <LoadingSpinner isLoading={!formAndResponses}>
    {formAndResponses && <PagedConsentView enrollee={enrollee} form={formAndResponses.studyEnvironmentConsent}
      responses={formAndResponses.consentResponses}
      studyShortcode={studyShortcode}/>}
  </LoadingSpinner>
}

/** Gets the enrollee object matching the given study */
function enrolleeForStudy(enrollees: Enrollee[], studyShortcode: string, portal: Portal): Enrollee {
  const studyEnvId = portal.portalStudies.find(pStudy => pStudy.study.shortcode === studyShortcode)?.study
    .studyEnvironments[0].id

  const enrollee = enrollees.find(enrollee => enrollee.studyEnvironmentId === studyEnvId)
  if (!enrollee) {
    throw `enrollment not found for ${studyShortcode}`
  }
  return enrollee
}

