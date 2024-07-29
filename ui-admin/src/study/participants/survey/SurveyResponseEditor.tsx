import React, { useEffect } from 'react'
import { Survey, SurveyResponse } from 'api/api'
import DocumentTitle from 'util/DocumentTitle'

import _cloneDeep from 'lodash/cloneDeep'
import { AutosaveStatus, Enrollee, PagedSurveyView, useTaskIdParam, makeSurveyJsData } from '@juniper/ui-core'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { Store } from 'react-notifications-component'
import { failureNotification, successNotification } from 'util/notifications'
import { usePortalLanguage } from 'portal/languages/usePortalLanguage'

/** allows editing of a survey response */
export default function SurveyResponseEditor({
  studyEnvContext, response, survey, enrollee, adminUserId,
  onUpdate, setAutosaveStatus, updateResponseMap, justification
}: {
  studyEnvContext: StudyEnvContextT, response?: SurveyResponse, setAutosaveStatus: (status: AutosaveStatus) => void,
  survey: Survey, enrollee: Enrollee, adminUserId: string, onUpdate: () => void,
  updateResponseMap: (stableId: string, response: SurveyResponse) => void, justification?: string
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

  useEffect(() => {
    // do an initial test parse to identify errors
    makeSurveyJsData(undefined, workingResponse.answers, undefined, (msg: React.ReactNode) => {
      Store.addNotification(failureNotification(msg))
    })
  }, [])

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
        updateResponseMap={updateResponseMap}
        setAutosaveStatus={setAutosaveStatus}
        onFailure={() => Store.addNotification(failureNotification('Response could not be saved'))}
        updateProfile={() => { /*no-op for admins*/ }}
        updateEnrollee={() => { /*no-op for admins*/ }}
        taskId={taskId}
        justification={justification}
        showHeaders={false}/>
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
