import React from 'react'
import { Survey, SurveyResponse } from 'api/api'
import DocumentTitle from 'util/DocumentTitle'

import _cloneDeep from 'lodash/cloneDeep'
import { Enrollee, PagedSurveyView, useTaskIdParam } from '@juniper/ui-core'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { Store } from 'react-notifications-component'
import { failureNotification, successNotification } from 'util/notifications'
import { usePortalLanguage } from 'portal/usePortalLanguage'

/** allows editing of a survey response */
export default function SurveyEditView({ studyEnvContext, response, survey, enrollee, adminUserId, onUpdate }: {
  studyEnvContext: StudyEnvContextT, response?: SurveyResponse,
  survey: Survey, enrollee: Enrollee, adminUserId: string, onUpdate: () => void
}) {
  const { defaultLanguage } = usePortalLanguage()
  const taskId = useTaskIdParam()
  if (!taskId) {
    return <span>Task Id must be specified</span>
  }
  const workingResponse = response ? _cloneDeep(response) : makeEmptyResponse(enrollee, survey, adminUserId)
  const studyEnvParams = {
    studyShortcode: studyEnvContext.study.shortcode,
    envName: studyEnvContext.currentEnv.environmentName,
    portalShortcode: studyEnvContext.portal.shortcode
  }
  return <div>
    <DocumentTitle title={`${enrollee.shortcode} - ${survey.name}`}/>
    <div>
      <PagedSurveyView
        studyEnvParams={studyEnvParams}
        form={survey}
        enrollee={enrollee}
        response={workingResponse}
        selectedLanguage={defaultLanguage.languageCode}
        adminUserId={adminUserId}
        onSuccess={() => {
          onUpdate()
          Store.addNotification(successNotification('Response saved'))
        }}
        onFailure={() => Store.addNotification(failureNotification('Response could not be saved'))}
        updateProfile={() => {}} //eslint-disable-line @typescript-eslint/no-empty-function
        updateEnrollee={() => {}} //eslint-disable-line @typescript-eslint/no-empty-function
        taskId={taskId}
        showHeaders={true}/>
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
