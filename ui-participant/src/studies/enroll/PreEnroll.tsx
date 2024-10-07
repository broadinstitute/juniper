import React, { useEffect } from 'react'
import Api, {
  PreEnrollmentResponse,
  Survey
} from 'api/api'
import { useNavigate } from 'react-router-dom'
import { StudyEnrollContext } from './StudyEnrollRouter'
import {
  getResumeData,
  getSurveyJsAnswerList,
  SurveyAutoCompleteButton,
  SurveyReviewModeButton,
  useI18n,
  useSurveyJSModel
} from '@juniper/ui-core'
import { useUser } from '../../providers/UserProvider'
import { useActiveUser } from '../../providers/ActiveUserProvider'

/**
 * pre-enrollment surveys are expected to have a calculated value that indicates
 * whether the participant meets the criteria.
 * */
const ENROLLMENT_QUALIFIED_VARIABLE = 'qualified'

/** Renders a pre-enrollment form, and handles submitting the user-inputted response */
export default function PreEnrollView({ enrollContext, survey }:
                                        { enrollContext: StudyEnrollContext, survey: Survey }) {
  const { studyEnv, updatePreEnrollResponseId, isProxyEnrollment, isSubjectEnrollment } = enrollContext
  const { selectedLanguage } = useI18n()
  const { profile } = useActiveUser()
  const { user, enrollees } = useUser()
  const proxyProfile = enrollees.find(enrollee => enrollee.participantUserId === user?.id && enrollee.profile)?.profile
  const navigate = useNavigate()
  // for now, we assume all pre-screeners are a single page
  const pager = { pageNumber: 0, updatePageNumber: () => 0 }
  const { surveyModel, refreshSurvey, SurveyComponent } = useSurveyJSModel(
    survey,
    null,
    handleComplete,
    pager,
    studyEnv.environmentName,
    profile || undefined,
    proxyProfile,
    [],
    { extraCssClasses: { container: 'my-0' }, extraVariables: { isProxyEnrollment, isSubjectEnrollment } }
  )

  surveyModel.locale = selectedLanguage || 'default'

  /** submit the form */
  function handleComplete() {
    if (!surveyModel) {
      return
    }
    const qualified = surveyModel.getCalculatedValueByName(ENROLLMENT_QUALIFIED_VARIABLE).value
    const responseDto: Partial<PreEnrollmentResponse> = {
      resumeData: getResumeData(surveyModel, null),
      answers: getSurveyJsAnswerList(surveyModel, selectedLanguage),
      surveyId: survey.id,
      studyEnvironmentId: studyEnv.id,
      qualified
    }

    // submit the form even if it isn't eligible, so we can track stats on exclusions
    Api.submitPreEnrollResponse({
      surveyStableId: survey.stableId,
      surveyVersion: survey.version,
      preEnrollResponse: responseDto
    }).then(result => {
      if (!qualified) {
        navigate('../ineligible')
      } else {
        updatePreEnrollResponseId(result.id as string)
      }
    }).catch(() => {
      updatePreEnrollResponseId(null)
      // SurveyJS doesn't support "uncompleting" surveys, so we have to reinitialize it
      // (for now we assume prereg is only a single page)
      refreshSurvey(surveyModel.data, 1)
    })
  }


  useEffect(() => {
    // if we're on this page, make sure we clear out any saved response ids -- this handles cases where the user
    // uses the back button
    updatePreEnrollResponseId(null)
  }, [])

  return (
    <div style={{ background: '#f3f3f3' }} className="flex-grow-1">
      <SurveyReviewModeButton surveyModel={surveyModel} envName={studyEnv.environmentName}/>
      <SurveyAutoCompleteButton surveyModel={surveyModel} envName={studyEnv.environmentName}/>
      {SurveyComponent}
    </div>
  )
}
