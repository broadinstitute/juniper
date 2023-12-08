import React, { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import Api, {
  Answer,
  ConsentForm,
  ConsentResponse,
  ConsentWithResponses,
  Enrollee,
  Portal,
  StudyEnvironmentConsent
} from 'api/api'

import { Survey as SurveyComponent } from 'survey-react-ui'
import {
  getResumeData,
  getSurveyJsAnswerList,
  PageNumberControl,
  useRoutablePageNumber,
  useSurveyJSModel
} from 'util/surveyJsUtils'
import { makeSurveyJsData, SurveyJsResumeData } from '@juniper/ui-core'
import { HubUpdate } from 'hub/hubUpdates'
import { usePortalEnv } from 'providers/PortalProvider'
import { useUser } from 'providers/UserProvider'
import { DocumentTitle } from 'util/DocumentTitle'
import { PageLoadingIndicator } from 'util/LoadingSpinner'

/**
 * display a single consent form to a participant.  The pageNumber argument can be specified to start at the given
 * page
 */
function RawConsentView({ form, enrollee, resumableData, pager, studyShortcode, isEditingPrevious }:
                          {
                            form: ConsentForm, enrollee: Enrollee, isEditingPrevious: boolean
                            resumableData: SurveyJsResumeData | null, pager: PageNumberControl, studyShortcode: string
                          }) {
  const { surveyModel, refreshSurvey } = useSurveyJSModel(form, resumableData, onComplete, pager)
  const navigate = useNavigate()
  const { updateEnrollee } = useUser()
  if (surveyModel && isEditingPrevious) {
    // consent responses are not editable -- they must be withdrawn via separate workflow
    surveyModel.mode = 'display'
  }

  /** Submit the response to the server */
  function onComplete() {
    if (!surveyModel || !refreshSurvey) {
      return
    }
    const responseDto = {
      resumeData: getResumeData(surveyModel, enrollee.participantUserId, true),
      enrolleeId: enrollee.id,
      fullData: JSON.stringify(getSurveyJsAnswerList(surveyModel)),
      creatingParticipantId: enrollee.participantUserId,
      consentFormId: form.id,
      // if the form doesn't export an explicit "consented" property, then the default is that they've consented
      // if they are able to submit it
      consented: surveyModel.getCalculatedValueByName('consented')?.value ?? true,
      completed: true
    } as ConsentResponse

    Api.submitConsentResponse({
      studyShortcode, stableId: form.stableId, enrolleeShortcode: enrollee.shortcode,
      version: form.version, response: responseDto
    }).then(response => {
      response.enrollee.participantTasks = response.tasks
      const hubUpdate: HubUpdate = {
        message: {
          title: `${form.name} completed`,
          type: 'SUCCESS'
        }
      }
      updateEnrollee(response.enrollee).then(() => {
        navigate('/hub', { state: hubUpdate })
      })
    }).catch(() => {
      refreshSurvey(surveyModel, null)
    })
  }


  return (
    <>
      <DocumentTitle title={form.name} />
      {surveyModel ? <SurveyComponent model={surveyModel} /> : null}
    </>
  )
}

/** handles paging the form */
function PagedConsentView({ form, responses, enrollee, studyShortcode }:
                            {
                              form: StudyEnvironmentConsent, responses: ConsentResponse[], enrollee: Enrollee,
                              studyShortcode: string
                            }) {
  const response = responses[0]
  let answers: Answer[] = []
  if (response?.fullData) {
    answers = JSON.parse(response.fullData)
  }
  const resumableData = makeSurveyJsData(response?.resumeData, answers, enrollee.participantUserId)

  const pager = useRoutablePageNumber()

  return <RawConsentView enrollee={enrollee} form={form.consentForm}
    isEditingPrevious={!!response} resumableData={resumableData} pager={pager}
    studyShortcode={studyShortcode}/>
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
  const navigate = useNavigate()

  if (!stableId || !version || !studyShortcode) {
    return <div>You must specify study, form, and version</div>
  }
  const enrollee = enrolleeForStudy(enrollees, studyShortcode, portal)

  useEffect(() => {
    Api.fetchConsentAndResponses({
      studyShortcode,
      enrolleeShortcode: enrollee.shortcode, stableId, version
    }).then(response => {
      setFormAndResponses(response)
    }).catch(() => {
      navigate('/hub')
    })
  }, [])

  if (!formAndResponses) {
    return <PageLoadingIndicator/>
  }

  return (
    <PagedConsentView
      enrollee={enrollee}
      form={formAndResponses.studyEnvironmentConsent}
      responses={formAndResponses.consentResponses}
      studyShortcode={studyShortcode}
    />
  )
}

/** Gets the enrollee object matching the given study */
export function enrolleeForStudy(enrollees: Enrollee[], studyShortcode: string, portal: Portal): Enrollee {
  const studyEnvId = portal.portalStudies.find(pStudy => pStudy.study.shortcode === studyShortcode)?.study
    .studyEnvironments[0].id

  const enrollee = enrollees.find(enrollee => enrollee.studyEnvironmentId === studyEnvId)
  if (!enrollee) {
    throw `enrollment not found for ${studyShortcode}`
  }
  return enrollee
}
