import React, { useState } from 'react'
import Api, { Enrollee, Survey, SurveyResponse } from 'api/api'
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
import { StudyEnvParams, useStudyEnvParamsFromPath } from '../../StudyEnvironmentRouter'
import { failureNotification, successNotification } from '../../../util/notifications'
import { Store } from 'react-notifications-component'

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
  const studyEnvParams = useStudyEnvParamsFromPath() as StudyEnvParams

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
      creatingAdminUserId: adminUserId,
      surveyId: form.id,
      complete: true
    } as SurveyResponse

    try {
      const response = await Api.updateSurveyResponse({
        studyEnvParams, stableId: form.stableId, enrolleeShortcode: enrollee.shortcode,
        version: form.version, response: responseDto, taskId
      })
      Store.addNotification(successNotification('Response saved'))
      refreshSurvey(surveyModel, null)
    } catch {
      Store.addNotification(failureNotification('Error saving response'))
      refreshSurvey(surveyModel, null)
    }
  }

  const { surveyModel, refreshSurvey } = useSurveyJSModel(form, resumableData,
    onComplete, pager, 'sandbox', enrollee.profile)

  surveyModel.locale = 'en'

  return (
    <>
      {/* f3f3f3 background is to match surveyJs "modern" theme */}
      <div style={{ background: '#f3f3f3' }} className="flex-grow-1">
        <SurveyComponent model={surveyModel}/>
        <SurveyFooter survey={form} surveyModel={surveyModel}/>
      </div>
    </>
  )
}
