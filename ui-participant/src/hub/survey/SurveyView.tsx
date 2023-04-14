import React, {useEffect, useRef, useState} from 'react'
import {useNavigate, useParams, useSearchParams} from 'react-router-dom'
import _union from 'lodash/union'
import _keys from 'lodash/keys'
import _reduce from 'lodash/reduce'
import _isEqual from 'lodash/isEqual'

import Api, {
  ConsentForm,
  Enrollee,
  Portal,
  ResumableData,
  StudyEnvironmentSurvey,
  SurveyResponse,
  SurveyWithResponse
} from 'api/api'

import {Survey as SurveyComponent} from 'survey-react-ui'
import {
  generateFormResponseDto,
  PageNumberControl,
  SourceType,
  SurveyResponseDto,
  useRoutablePageNumber,
  useSurveyJSModel
} from 'util/surveyJsUtils'
import {HubUpdate} from 'hub/hubUpdates'
import {usePortalEnv} from 'providers/PortalProvider'
import {useUser} from 'providers/UserProvider'
import {PageLoadingIndicator} from 'util/LoadingSpinner'
import {withErrorBoundary} from '../../util/ErrorBoundary'

const TASK_ID_PARAM = 'taskId'

/**
 * display a single survey form to a participant.
 */
function RawSurveyView({form, enrollee, resumableData, pager, studyShortcode, taskId}:
                         {
                           form: ConsentForm, enrollee: Enrollee, taskId: string
                           resumableData: ResumableData | null, pager: PageNumberControl, studyShortcode: string
                         }) {
  let prevSave = useRef(resumableData?.data ?? {})
  /** Submit the response to the server */
  const onComplete = () => {
    if (!surveyModel || !refreshSurvey) {
      return
    }
    const responseDto = generateFormResponseDto({
      surveyJSModel: surveyModel, enrolleeId: enrollee.id, sourceType: SourceType.ENROLLEE
    }) as SurveyResponseDto
    responseDto.complete = true

    Api.submitSurveyResponse({
      studyShortcode, stableId: form.stableId, enrolleeShortcode: enrollee.shortcode,
      version: form.version, response: responseDto, taskId
    }).then(response => {
      response.enrollee.participantTasks = response.tasks
      updateEnrollee(response.enrollee)
      const hubUpdate: HubUpdate = {
        message: {
          title: `${form.name} completed`,
          type: 'success'
        }
      }
      navigate('/hub', {state: hubUpdate})
    }).catch(() => {
      refreshSurvey(surveyModel, null)
      alert('an error occurred')
    })
  }

  const {surveyModel, refreshSurvey, setSurveyModel} = useSurveyJSModel(form, resumableData,
    onComplete, pager, enrollee.profile)
  const navigate = useNavigate()

  const {updateEnrollee} = useUser()
  if (surveyModel && resumableData) {
    // survey responses aren't yet editable after completion
    surveyModel.mode = 'display'
  }

  function saveDiff(prevData: ResumableData | null) {
    setSurveyModel(freshSurveyModel => {
      if (freshSurveyModel) {
        console.log(prevSave.current)
        console.log(freshSurveyModel.data)
        console.log(computeDiff(prevSave.current as Record<string, object>, freshSurveyModel.data))
        prevSave.current = freshSurveyModel.data
      }
      return freshSurveyModel
    })
  }

  useEffect(() => {
    const interval = window.setInterval(saveDiff, 5000)
    return () => {
      window.clearInterval(interval)
    }
  }, [])

  return <div>
    <h4 className="text-center mt-2">{form.name}</h4>
    {surveyModel && <SurveyComponent model={surveyModel}/>}
  </div>
}

/** handles paging the form */
function PagedSurveyView({form, activeResponse, enrollee, studyShortcode, taskId}:
                           {
                             form: StudyEnvironmentSurvey, activeResponse: SurveyResponse, enrollee: Enrollee,
                             studyShortcode: string, taskId: string
                           }) {
  let resumableData = null
  if (activeResponse?.lastSnapshot?.resumeData) {
    resumableData = JSON.parse(activeResponse?.lastSnapshot.resumeData) as ResumableData
  }

  const pager = useRoutablePageNumber()

  return <RawSurveyView enrollee={enrollee} form={form.survey} taskId={taskId}
                        resumableData={resumableData} pager={pager} studyShortcode={studyShortcode}/>
}

/** handles loading the survey form and responses from the server */
function SurveyView() {
  const {portal} = usePortalEnv()
  const {enrollees} = useUser()
  const [formAndResponses, setFormAndResponse] = useState<SurveyWithResponse | null>(null)
  const params = useParams()
  const stableId = params.stableId
  const version = parseInt(params.version ?? '')
  const studyShortcode = params.studyShortcode

  const [searchParams] = useSearchParams()
  const taskId = searchParams.get(TASK_ID_PARAM) ?? ''

  if (!stableId || !version || !studyShortcode) {
    return <div>You must specify study, form, and version</div>
  }
  const enrollee = enrolleeForStudy(enrollees, studyShortcode, portal)

  useEffect(() => {
    Api.fetchSurveyAndResponse({
      studyShortcode,
      enrolleeShortcode: enrollee.shortcode, stableId, version, taskId
    })
      .then(response => {
        setFormAndResponse(response)
      }).catch(() => {
      alert('error loading survey form - please retry')
    })
  }, [])

  if (!formAndResponses) {
    return <PageLoadingIndicator/>
  }

  return (
    <PagedSurveyView
      enrollee={enrollee}
      form={formAndResponses.studyEnvironmentSurvey}
      activeResponse={formAndResponses.surveyResponse}
      studyShortcode={studyShortcode}
      taskId={taskId}
    />
  )
}

export default withErrorBoundary(SurveyView)

/** Gets the enrollee object matching the given study */
function enrolleeForStudy(enrollees: Enrollee[], studyShortcode: string, portal: Portal): Enrollee {
  const studyEnvId = portal.portalStudies.find(pStudy => pStudy.study.shortcode === studyShortcode)?.study
    .studyEnvironments[0].id

  const enrollee = enrollees.find(enrollee => enrollee.studyEnvironmentId === studyEnvId)
  if (!enrollee) {
    throw `enrollment not found for ${studyShortcode}`
  }
  return enrollee
}

function computeDiff(original: Record<string, object> | null, updated: Record<string, object>): SurveyDiff {
  if (!original) {
    return {
      diff: updated,
      deletedKeys: []
    }
  }
  const allkeys = _union(_keys(original), _keys(updated));
  const deletedKeys: string[] = []
  const differences = _reduce(allkeys, function (result, key) {
    if (!_isEqual(original[key], updated[key])) {
      if (key in updated) {
        // @ts-expect-error not sure how to typescript reduce
        result[key] = updated[key]
      } else {
        deletedKeys.push(key)
      }
    }
    return result;
  }, {});
  return {
    diff: differences,
    deletedKeys: deletedKeys
  }
}

export type SurveyDiff = {
  diff: Record<string, object>,
  deletedKeys: string[]
}
