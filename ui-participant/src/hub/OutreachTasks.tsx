import React, { useEffect } from 'react'
import Api, { Enrollee, ParticipantTask, Study, SurveyResponse, TaskWithSurvey } from 'api/api'
import { getTaskPath } from './TaskLink'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import SurveyModal from './SurveyModal'
import { useTaskIdParam } from './survey/SurveyView'
import { useI18n } from '@juniper/ui-core'
import { getLocalizedSurveyTitle } from '../util/surveyJsUtils'

type OutreachParams = {
    enrolleeShortcode?: string,
    studyShortcode?: string,
    stableId?: string
    version?: string
}

/** gets the outreach params from the URL */
const useOutreachParams = () => {
  const params = useParams<OutreachParams>()
  const taskId = useTaskIdParam()
  return {
    ...params,
    taskId,
    isOutreachPath: useLocation().pathname.includes('/outreach/') && !!taskId
  }
}

/** renders all outreach tasks for the set of enrollees. This operates on a list of enrollees, since
 * all of the tasks from the portal the user has signed into are shown, and that may include
 * multiple studies and therefore enrollees */
export default function OutreachTasks({ enrollees, studies, outreachTasks }: {
  enrollees: Enrollee[], studies: Study[], outreachTasks: TaskWithSurvey[]
}) {
  const { i18n, selectedLanguage } = useI18n()
  const navigate = useNavigate()
  const outreachParams = useOutreachParams()

  const sortedOutreachTasks = outreachTasks.sort((a, b) => {
    return a.task.createdAt - b.task.createdAt
  })
  const markTaskAsViewed = async (task: ParticipantTask, enrollee: Enrollee, study: Study) => {
    const responseDto = {
      resumeData: '{}',
      enrolleeId: enrollee.id,
      answers: [],
      creatingParticipantId: enrollee.participantUserId,
      surveyId: task.id,
      complete: false
    } as SurveyResponse
    await Api.updateSurveyResponse({
      studyShortcode: study.shortcode,
      enrolleeShortcode: enrollee.shortcode,
      stableId: task.targetStableId,
      version: task.targetAssignedVersion,
      taskId: task.id,
      alertErrors: false,
      response: responseDto
    })
  }

  useEffect(() => {
    const matchedTask = outreachTasks.find(({ task }) => task.id === outreachParams.taskId)?.task
    if (outreachParams.stableId && matchedTask && matchedTask.status === 'NEW') {
      const taskStudy = studyForTask(matchedTask, studies)
      const taskEnrollee = enrolleeForTask(matchedTask, enrollees)
      markTaskAsViewed(matchedTask, taskEnrollee, taskStudy)
    }
  }, [outreachParams.stableId])

  return <div className="">
    <div className="row g-3 pb-3">
      {sortedOutreachTasks.map(({ task, form }) => {
        const taskStudy = studyForTask(task, studies)
        const taskEnrollee = enrolleeForTask(task, enrollees)
        const taskUrl = getTaskPath(task, taskEnrollee.shortcode, taskStudy.shortcode)
        // Gutters seem not to work??  So I had to add margins manually
        return <div className="col-md-6 col-sm-12" key={task.id}>
          <div className="p-4 d-block rounded-3 shadow-sm"
            style={{ background: '#fff', minHeight: '6em' }} key={task.id}>
            <h3 className="h5">{getLocalizedSurveyTitle(form, selectedLanguage)}</h3>
            <p className="text-muted">
              {form.blurb}
            </p>
            <div className="py-3 text-center" style={{ background: 'var(--brand-color-shift-90)' }}>
              <Link to={taskUrl} className="btn rounded-pill ps-4 pe-4 fw-bold btn-primary">
                {i18n('outreachLearnMore')}
              </Link>
            </div>
          </div>
        </div>
      })}
      {outreachParams.isOutreachPath && <SurveyModal onDismiss={() => navigate('/hub')}/> }
    </div>
  </div>
}

/** finds the study that corresponds to the given task */
const studyForTask = (task: ParticipantTask, studies: Study[]) => {
  return studies.find(study => study.studyEnvironments[0].id === task.studyEnvironmentId)!
}

/** finds the enrollee that corresponds to the given task */
const enrolleeForTask = (task: ParticipantTask, enrollees: Enrollee[]) => {
  return enrollees.find(enrollee => enrollee.studyEnvironmentId === task.studyEnvironmentId)!
}
