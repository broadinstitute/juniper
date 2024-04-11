import React, { useState } from 'react'
import { Enrollee, Survey, SurveyResponse } from 'api/api'
import DocumentTitle from 'util/DocumentTitle'
import {
  getDataWithCalculatedValues, getResumeData, getUpdatedAnswers,
  makeSurveyJsData,
  PageNumberControl, SurveyFooter,
  SurveyJsResumeData,
  useRoutablePageNumber, useSurveyJSModel
} from '@juniper/ui-core'
import _cloneDeep from 'lodash/cloneDeep'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Survey as SurveyComponent } from 'survey-react-ui'

/** allows editing of a survey response */
export default function SurveyEditView({ response, survey, enrollee, adminUserId }:
{response?: SurveyResponse, survey: Survey, enrollee: Enrollee, adminUserId: string}) {
  const taskId = useTaskIdParam()
  if (!taskId) {
    return <span>Task Id must be specified</span>
  }
  const [workingResponse, setWorkingResponse] = useState<SurveyResponse>(response ? _cloneDeep(response) :
    makeEmptyResponse(enrollee, survey, adminUserId))
  return <div>
    <DocumentTitle title={`${enrollee.shortcode} - ${survey.name}`}/>
    <h6>{survey.name}</h6>
    <div>
      <PagedSurveyView form={survey} enrollee={enrollee} adminUserId={adminUserId} response={workingResponse}
        taskId={taskId} setWorkingResponse={setWorkingResponse}/>
    </div>
  </div>
}


const makeEmptyResponse = (enrollee: Enrollee, survey: Survey, adminUserId: string): SurveyResponse => {
  return {
    enrolleeId: enrollee.id,
    creatingAdminUserId: adminUserId,
    surveyId: survey.id,
    resumeData: '{}',
    answers: [],
    complete: false
  }
}

/** handles paging the form */
export function PagedSurveyView({
  form, response, setWorkingResponse, taskId, enrollee, adminUserId
}: {
    form: Survey, response: SurveyResponse, setWorkingResponse: (response: SurveyResponse) => void,
    taskId: string, adminUserId: string, enrollee: Enrollee
  }) {
  const resumableData = makeSurveyJsData(response.resumeData,
    response.answers, adminUserId)

  const pager = useRoutablePageNumber()

  return <RawSurveyView form={form} taskId={taskId} response={response} setWorkingResponse={setWorkingResponse}
    resumableData={resumableData} pager={pager} enrollee={enrollee} adminUserId={adminUserId}/>
}

const TASK_ID_PARAM = 'taskId'
/** gets the task ID from the URL */
export const useTaskIdParam = (): string | null => {
  const [searchParams] = useSearchParams()
  return searchParams.get(TASK_ID_PARAM)
}

/**
 * display a single survey form to a participant.
 */
export function RawSurveyView({
  form, resumableData, pager, setWorkingResponse,
  taskId, response, adminUserId, enrollee
}:
  {
    form: Survey, taskId: string, response?: SurveyResponse, adminUserId: string,
    setWorkingResponse: (response: SurveyResponse) => void,
    resumableData: SurveyJsResumeData | null, pager: PageNumberControl,
    enrollee: Enrollee
  }) {
  const navigate = useNavigate()

  /** Submit the response to the server */
  const onComplete = async () => {
    if (!surveyModel || !refreshSurvey) {
      return
    }
    const currentModelValues = getDataWithCalculatedValues(surveyModel)
    const responseDto = {
      resumeData: getResumeData(surveyModel, adminUserId, true),
      enrolleeId: enrollee.id,
      answers: getUpdatedAnswers(resumableData || {}, currentModelValues, 'en'),
      creatingParticipantId: enrollee.participantUserId,
      surveyId: form.id,
      complete: true
    } as SurveyResponse

    try {
      alert('saving!!')
    } catch {
      refreshSurvey(surveyModel, null)
    }
  }

  const { surveyModel, refreshSurvey } = useSurveyJSModel(form, resumableData,
    onComplete, pager, 'sandbox', enrollee.profile)

  surveyModel.locale = 'en'

  return (
    <>
      <DocumentTitle title={form.name} />
      {/* f3f3f3 background is to match surveyJs "modern" theme */}
      <div style={{ background: '#f3f3f3' }} className="flex-grow-1">
        <SurveyComponent model={surveyModel}/>
        <SurveyFooter survey={form} surveyModel={surveyModel}/>
      </div>
    </>
  )
}
