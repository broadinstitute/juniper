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
import { makeSurveyJsData, SurveyJsResumeData, useI18n } from '@juniper/ui-core'
import { HubUpdate } from 'hub/hubUpdates'
import { usePortalEnv } from 'providers/PortalProvider'
import { useUser } from 'providers/UserProvider'
import { DocumentTitle } from 'util/DocumentTitle'
import { PageLoadingIndicator } from 'util/LoadingSpinner'
import SurveyReviewModeButton from '../survey/ReviewModeButton'
import SurveyAutoCompleteButton from '../survey/SurveyAutoCompleteButton'
import { useTaskIdParam } from '../survey/SurveyView'

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
  const { selectedLanguage } = useI18n()
  const { updateEnrollee } = useUser()

  surveyModel.locale = selectedLanguage || 'default'

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
      fullData: JSON.stringify(getSurveyJsAnswerList(surveyModel, selectedLanguage)),
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
      <DocumentTitle title={form.name}/>
      <div style={{ background: '#f3f3f3' }} className="flex-grow-1">
        <SurveyReviewModeButton surveyModel={surveyModel}/>
        <SurveyAutoCompleteButton surveyModel={surveyModel}/>
        {surveyModel ? <SurveyComponent model={surveyModel} /> : null}
      </div>
    </>

  )
}

/** handles paging the form */
function PagedConsentView({ form, responses, enrollee, studyShortcode }: {
  form: StudyEnvironmentConsent, responses: ConsentResponse[], enrollee: Enrollee, studyShortcode: string
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
  const taskId = useTaskIdParam()
  const navigate = useNavigate()

  if (!stableId || !version || !studyShortcode) {
    return <div>You must specify study, form, and version</div>
  }
  const enrollee = enrolleeForStudy(enrollees, studyShortcode, portal)

  const loadForm = async () => {
    try {
      const response = await Api.fetchConsentAndResponses({
        studyShortcode,
        enrolleeShortcode: enrollee.shortcode, stableId, version
      })
      setFormAndResponses(response)
    } catch (e) {
      // if we can't load it as a consent form, try loading it as a survey
      try {
        await Api.fetchSurveyAndResponse({
          studyShortcode, enrolleeShortcode: enrollee.shortcode, stableId, version, taskId
        })
        // it's a survey -- view it there
        navigate(`/hub/study/${studyShortcode}/enrollee/${enrollee.shortcode}/survey/${stableId}`
          + `/${version}?taskId=${taskId}`)
      } catch (e) {
        // if everything fails, go back to the hub
        navigate('/hub')
      }
    }
  }

  useEffect(() => {
    loadForm()
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
