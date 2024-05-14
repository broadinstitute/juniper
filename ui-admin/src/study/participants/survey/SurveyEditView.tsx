import React, { useState } from 'react'
import { Survey, SurveyResponse } from 'api/api'
import DocumentTitle from 'util/DocumentTitle'

import _cloneDeep from 'lodash/cloneDeep'
import { useSearchParams } from 'react-router-dom'
import { Enrollee, PagedSurveyView } from '@juniper/ui-core'
import { StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { Store } from 'react-notifications-component'
import { failureNotification, successNotification } from 'util/notifications'
import { usePortalLanguage } from 'portal/usePortalLanguage'

/** allows editing of a survey response */
export default function SurveyEditView({ studyEnvContext, response, survey, enrollee, adminUserId }: {
  studyEnvContext: StudyEnvContextT, response?: SurveyResponse, survey: Survey, enrollee: Enrollee, adminUserId: string
}) {
  const { defaultLanguage } = usePortalLanguage()
  const taskId = useTaskIdParam()
  if (!taskId) {
    return <span>Task Id must be specified</span>
  }
  const [workingResponse] = useState<SurveyResponse>(response ? _cloneDeep(response) :
    makeEmptyResponse(enrollee, survey, adminUserId))
  const studyEnvParams = {
    studyShortcode: studyEnvContext.study.shortcode,
    envName: studyEnvContext.currentEnv.environmentName,
    portalShortcode: studyEnvContext.portal.shortcode
  }
  return <div>
    <DocumentTitle title={`${enrollee.shortcode} - ${survey.name}`}/>
    <div>
      <PagedSurveyView studyEnvParams={studyEnvParams} form={survey} enrollee={enrollee}
        adminUserId={adminUserId} response={workingResponse} selectedLanguage={defaultLanguage.languageCode}
        onSuccess={() => Store.addNotification(successNotification('Response saved'))}
        onFailure={() => Store.addNotification(failureNotification('Response could not be saved'))}
        updateProfile={() => console.log('meh')}
        taskId={taskId} updateEnrollee={() => console.log('meh')} showHeaders={true}/>
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

const TASK_ID_PARAM = 'taskId'
/** gets the task ID from the URL */
export const useTaskIdParam = (): string | null => {
  const [searchParams] = useSearchParams()
  return searchParams.get(TASK_ID_PARAM)
}
