import React, { useEffect, useState } from 'react'
import Api, { Enrollee, ParticipantTask, Study, SurveyResponse } from 'api/api'
import { getTaskPath } from './TaskLink'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import SurveyModal from './SurveyModal'
import { useTaskIdParam } from './survey/SurveyView'
import { Survey } from '@juniper/ui-core/build/types/forms'

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
export default function OutreachTasks({ enrollees, studies }: {enrollees: Enrollee[], studies: Study[]}) {
  const navigate = useNavigate()
  const outreachParams = useOutreachParams()
  const [outreachDetails, setOutreachDetails] = useState<Survey[]>([])

  const outreachTasks = enrollees.flatMap(enrollee => enrollee.participantTasks)
    .filter(task => task.taskType === 'OUTREACH')
    .sort((a, b) => a.createdAt - b.createdAt)
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

  const loadOutreachSurveys = async () => {
    const surveys = await Api.listOutreachSurveys('ourheart', 'JLPWIF')
    return surveys
  }

  useEffect(() => {
    //loads the outreach surveys and sets the state
    loadOutreachSurveys()
      .then(response => {
        setOutreachDetails(response)
      })
      .catch(error => {
        console.log(error)
      })
  }, [])

  useEffect(() => {
    const matchedTask = outreachTasks.find(task => task.id === outreachParams.taskId)
    if (outreachParams.stableId && matchedTask && matchedTask.status === 'NEW') {
      const taskStudy = studyForTask(matchedTask, studies)
      const taskEnrollee = enrolleeForTask(matchedTask, enrollees)
      markTaskAsViewed(matchedTask, taskEnrollee, taskStudy)
    }
  }, [outreachParams.stableId])

  return <div className="">
    <div className="row g-3 pb-3">
      {outreachTasks.map(task => {
        const taskStudy = studyForTask(task, studies)
        const taskEnrollee = enrolleeForTask(task, enrollees)
        const taskUrl = getTaskPath(task, taskEnrollee.shortcode, taskStudy.shortcode)
        // Gutters seem not to work??  So I had to add margins manually
        return <div className="col-md-6 col-sm-12" key={task.id}>
          <div className="p-4 d-block rounded-3"
            style={{ background: '#fff', minHeight: '6em' }} key={task.id}>
            <h3 className="h5">{task.targetName}</h3>
            <p className="text-muted">
              {outreachDetails.find(survey => survey.stableId === task.targetStableId)?.blurb}
            </p>
            <div className="py-3 text-center" style={{ background: 'var(--brand-color-shift-90)' }}>
              <Link to={taskUrl} className="btn rounded-pill ps-4 pe-4 fw-bold btn-primary">
              Learn More
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
