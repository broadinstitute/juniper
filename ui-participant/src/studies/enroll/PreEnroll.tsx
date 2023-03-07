import React, { useEffect } from 'react'
import Api from 'api/api'
import { generateFormResponseDto, PreEnrollResponseDto, SourceType, useSurveyJSModel } from 'util/surveyJsUtils' // eslint-disable-line max-len
import { useNavigate } from 'react-router-dom'
import { StudyEnrollContext } from './StudyEnrollRouter'

/**
 * pre-enrollment surveys are expected to have a calculated value that indicates
 * whether the participant meets the criteria.
 * */
const ENROLLMENT_QUALIFIED_VARIABLE = 'qualified'

/** Renders a pre-enrollment form, and handles submitting the user-inputted response */
export default function PreEnrollView({ enrollContext }: { enrollContext: StudyEnrollContext }) {
  const { studyEnv, updatePreEnrollResponseId } = enrollContext
  const survey = studyEnv.preEnrollSurvey
  const navigate = useNavigate()
  // for now, we assume all pre-screeners are a single page
  const pager = { pageNumber: 0, updatePageNumber: () => 0 }
  const { surveyModel, refreshSurvey, SurveyComponent } =
    useSurveyJSModel(survey, null, handleComplete, pager)

  /** submit the form */
  function handleComplete() {
    if (!surveyModel) {
      return
    }
    const responseDto = generateFormResponseDto({
      surveyJSModel: surveyModel, enrolleeId: null, sourceType: SourceType.ANON
    })
    const qualified = surveyModel.getCalculatedValueByName(ENROLLMENT_QUALIFIED_VARIABLE).value
    const preEnrollResponse = {
      ...responseDto,
      qualified,
      studyEnvironmentId: studyEnv.id
    } as PreEnrollResponseDto
    // submit the form even if it isn't eligible, so we can track stats on exclusions
    Api.submitPreEnrollResponse({
      surveyStableId: survey.stableId,
      surveyVersion: survey.version,
      preEnrollResponse
    }).then(result => {
      if (!qualified) {
        navigate('../ineligible')
      } else {
        updatePreEnrollResponseId(result.id)
      }
    }).catch(() => {
      alert('an error occurred, please retry')
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

  return <div>
    {SurveyComponent}
  </div>
}
