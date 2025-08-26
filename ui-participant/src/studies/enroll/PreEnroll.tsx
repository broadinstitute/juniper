import React, { useEffect, useState } from 'react'
import Api, {
  EligibilityResult,
  PreEnrollmentResponse,
  Survey
} from 'api/api'
import { useNavigate } from 'react-router-dom'
import { StudyEnrollContext } from './StudyEnrollRouter'
import {
  getResumeData,
  getSurveyJsAnswerList,
  makeSurveyJsData,
  SurveyAutoCompleteButton,
  SurveyReviewModeButton,
  useI18n,
  useSurveyJSModel
} from '@juniper/ui-core'
import { useUser } from '../../providers/UserProvider'
import { useActiveUser } from '../../providers/ActiveUserProvider'
import { useEnrollmentParams } from './useEnrollmentParams'
import { LoadingSpinner } from '../../util/LoadingSpinner'
import { ApiErrorResponse, defaultApiErrorHandle } from '../../util/error-utils'

/**
 * pre-enrollment surveys are expected to have a calculated value that indicates
 * whether the participant meets the criteria.
 * */
const ENROLLMENT_QUALIFIED_VARIABLE = 'qualified'

export default function PreEnrollView({ enrollContext, survey }:
  { enrollContext: StudyEnrollContext, survey: Survey }) {
  const eligibleRule = enrollContext.studyEnv.studyEnvironmentConfig.studyEligibilityRule

  // if there's an eligibility rule, we need to check eligibility before showing the survey
  const [isLoading, setIsLoading] = useState(!!eligibleRule)
  const [eligibilityResult, setEligibilityResult] = useState<EligibilityResult>({ eligible: !!eligibleRule })

  const checkEligibility = async () => {
    if (eligibleRule) {
      setIsLoading(true)
      try {
        const response = await Api.checkEligible(enrollContext.studyShortcode)
        setEligibilityResult(response)
        setIsLoading(false)
      } catch (e: unknown) {
        const statusCode = (e as ApiErrorResponse).statusCode
        // ignore 401/403s -- not being logged in is handled by the survey content
        if (statusCode === 403 || statusCode === 401) {
          setIsLoading(false)
        } else {
          defaultApiErrorHandle(e as ApiErrorResponse)
        }
      }
    }
  }

  useEffect(() => {
    checkEligibility()
  }, [eligibleRule])
  return <>
    { !isLoading && <EligiblePreEnrollView enrollContext={enrollContext}
      eligibilityResult={eligibilityResult} survey={survey}/> }
    { isLoading && <LoadingSpinner/>}
  </>
}


/** Renders a pre-enrollment form, and handles submitting the user-inputted response */
export function EligiblePreEnrollView({ enrollContext, survey, eligibilityResult }:
                                        { enrollContext: StudyEnrollContext,
                                          survey: Survey,
                                          eligibilityResult: EligibilityResult
                                        }) {
  const {
    studyEnv,
    updatePreEnrollResponseId,
    isProxyEnrollment,
    isSubjectEnrollment,
    studyShortcode,
    portalShortcode
  } = enrollContext
  const { selectedLanguage } = useI18n()
  const { profile, enrollees: activeEnrollees } = useActiveUser()
  const { user, enrollees: allEnrollees } = useUser()

  const enrollee = activeEnrollees
    .find(enrollee => enrollee.studyEnvironmentId === studyEnv.id)
  const proxyProfile = allEnrollees
    .find(enrollee => enrollee.participantUserId === user?.id && enrollee.profile)?.profile
  const navigate = useNavigate()
  // for now, we assume all pre-screeners are a single page
  const pager = { pageNumber: 0, updatePageNumber: () => 0 }

  const { preFilledAnswers, referralSource, clearStoredEnrollmentParams } = useEnrollmentParams()
  const resumeData = makeSurveyJsData(undefined, preFilledAnswers, user?.id)

  const { surveyModel, refreshSurvey, SurveyComponent } = useSurveyJSModel(
    survey,
    resumeData,
    handleComplete,
    pager,
    {
      profile: profile || undefined,
      proxyProfile,
      studyEnvParams: { envName: studyEnv.environmentName, studyShortcode, portalShortcode },
      enrolleeShortcode: enrollee?.shortcode || '',
      referencedAnswers: [],
      extraVariables: {
        isProxyEnrollment, isSubjectEnrollment, user,
        eligibilityResult
      }
    },
    { extraCssClasses: { container: 'my-0' } }
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
      referralSource: referralSource || undefined,
      qualified
    }

    // submit the form even if it isn't eligible, so we can track stats on exclusions
    Api.submitPreEnrollResponse({
      surveyStableId: survey.stableId,
      surveyVersion: survey.version,
      preEnrollResponse: responseDto
    }).then(result => {
      //clear the stored enrollment params in case someone else uses the same browser
      //without closing the tab
      clearStoredEnrollmentParams()
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
      <div className="mb-2">{SurveyComponent}</div>
    </div>
  )
}
