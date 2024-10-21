import React, { useEffect, useState } from 'react'
import 'react-querybuilder/dist/query-builder.scss'
import { doApiLoad } from 'api/api-utils'
import Api, { EnrolleeMergePlan, MergeAction, ParticipantTask, ParticipantUserMerge } from 'api/api'
import { Enrollee, instantToDateString, StudyEnvParams } from '@juniper/ui-core'
import LoadingSpinner from 'util/LoadingSpinner'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faArrowRight,
  faExchange
} from '@fortawesome/free-solid-svg-icons'
import { successNotification } from 'util/notifications'
import { Link, useNavigate } from 'react-router-dom'
import { paramsFromContext, participantAccountsPath, StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { Store } from 'react-notifications-component'
import { studyEnvParticipantPath } from '../ParticipantsRouter'
import { statusDisplayMap } from '../enrolleeView/EnrolleeView'

const mergeArrow = <FontAwesomeIcon icon={faArrowRight} className="px-2"/>

/**
 * Returns a cohort builder modal
 */
export default function ParticipantMergeView({ source, target, studyEnvContext, onUpdate }:
 {source?: string, target?: string, studyEnvContext: StudyEnvContextT, onUpdate: () => void }) {
  const [sourceEmail, setSourceEmail] = useState(source || '')
  const [targetEmail, setTargetEmail] = useState(target || '')
  const [mergePlan, setMergePlan] = useState<ParticipantUserMerge>()
  const [isLoading, setIsLoading] = useState(false)
  const navigate = useNavigate()
  const planMerge = (sourceEmail: string, targetEmail: string) => {
    if (!targetEmail.includes('@') || !sourceEmail.includes('@')) { return }
    doApiLoad(async () => {
      const mergePlan = await Api.fetchMergePlan(studyEnvContext.portal.shortcode,
        studyEnvContext.currentEnv.environmentName, sourceEmail, targetEmail)
      setMergePlan(mergePlan)
    }, { setIsLoading })
  }

  const executeMerge = () => {
    if (!mergePlan) { return }
    doApiLoad(async () => {
      await Api.executeMergePlan(studyEnvContext.portal.shortcode,
        studyEnvContext.currentEnv.environmentName, mergePlan)
      Store.addNotification(successNotification('Merge successful'))
      onUpdate()
      navigate(participantAccountsPath(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName))
    }, { setIsLoading })
  }

  const exchange = () => {
    setSourceEmail(targetEmail)
    setTargetEmail(sourceEmail)
    planMerge(targetEmail, sourceEmail)
  }

  useEffect(() => {
    if (source && target) {
      planMerge(source, target)
    }
  }, [])

  return <div className="container-fluid px-4 py-2">

    <div className="d-flex">
      <div className="container">
        <label htmlFor="sourceEmail">Duplicate email</label>
        <input type="text" className="form-control" id="sourceEmail"
          onChange={e => setSourceEmail(e.target.value)} value={sourceEmail}/>
        <p className="text-muted fst-italic">The account to delete/merge.</p>
      </div>
      <Button variant="secondary" onClick={exchange}>
        <FontAwesomeIcon icon={faExchange}/>
      </Button>
      <div className="container">
        <label htmlFor="targetEmail">Original email</label>
        <input type="text" className="form-control" id="targetEmail" size={20}
          onChange={e => setTargetEmail(e.target.value)} value={targetEmail}/>
        <p className="text-muted fst-italic">The account to keep.</p>
      </div>
    </div>
    <Button variant="primary" onClick={() => planMerge(sourceEmail, targetEmail)}
      tooltip={'Plan only, will not take actions'}
    >Preview Merge</Button>
    <hr/>
    <div>
      <LoadingSpinner isLoading={isLoading}>
        {!mergePlan && <div> </div>}
        { mergePlan && <div>
          <h2 className="h4 my-3">Merge Preview</h2>
          <div className="d-flex">
            {renderMergeUserInfo(mergePlan, true)}
            {mergeArrow}
            {renderMergeUserInfo(mergePlan, false)}
          </div>
          <div>
            { mergePlan.enrollees.map((merge, index) => <EnrolleeMergePlanView
              enrolleeMerge={merge} key={index} studyEnvParams={paramsFromContext(studyEnvContext)}/>) }
          </div>
          <Button variant="primary" onClick={executeMerge} className="mt-3">Execute Merge</Button>
        </div> }
      </LoadingSpinner>
    </div>
  </div>
}

function EnrolleeMergePlanView({ enrolleeMerge,  studyEnvParams }:
  {enrolleeMerge: MergeAction<Enrollee, EnrolleeMergePlan>, studyEnvParams: StudyEnvParams }) {
  const sortedTasks = enrolleeMerge.mergePlan.tasks.sort((a, b) =>
    taskComparator(a.pair.source ?? a.pair.target, b.pair.source ?? b.pair.target))
  return <div className="mx-4">
    { enrolleeMerge.pair.source &&
      <Link to={studyEnvParticipantPath(studyEnvParams, enrolleeMerge.pair.source.shortcode)}>
        { enrolleeMerge.pair.source.shortcode }
      </Link> }

    {mergeArrow}
    { enrolleeMerge.pair.target &&
      <Link to={studyEnvParticipantPath(studyEnvParams, enrolleeMerge.pair.target.shortcode)}>
        { enrolleeMerge.pair.target.shortcode }
      </Link> }
    <table>
      <thead>
        <tr>
          <th>task</th>
          <th className="px-2">duplicate</th>
          <th className="px-2">original</th>
          <th className="px-2">Merge result</th>
        </tr>
      </thead>
      <tbody>
        { sortedTasks.map((taskMerge, index) =>
          <MergeTaskView taskMerge={taskMerge} key={index}/>) }
      </tbody>
    </table>
  </div>
}

function MergeTaskView({ taskMerge }: {taskMerge: MergeAction<ParticipantTask, object>}) {
  const source = taskMerge.pair.source
  const target = taskMerge.pair.target
  return <tr>
    <td>{renderTaskTitle(source, target)}</td>
    <td className="px-2">{renderTaskSummary(source)}</td>
    <td className="px-2">{renderTaskSummary(target)}</td>
    <td>{renderResultSummary(taskMerge)}</td>
  </tr>
}

function renderTaskSummary(task?: ParticipantTask) {
  if (!task) {
    return <span className="text-muted fst-italic">n/a</span>
  }
  return <Button variant="secondary" tooltip={`assigned ${instantToDateString(task.createdAt)}`}>
    {statusDisplayMap[task.status]}
  </Button>
}

function renderResultSummary(taskMerge: MergeAction<ParticipantTask, object>) {
  if (taskMerge.pair.source && taskMerge.pair.target) {
    if (taskMerge.action === 'MOVE_SOURCE') {
      return <span>keep both</span>
    } else if (taskMerge.action === 'MERGE') {
      return <span>merge</span>
    } else if (taskMerge.action === 'MOVE_SOURCE_DELETE_TARGET') {
      return <span>keep duplicate only</span>
    } else if (taskMerge.action === 'DELETE_SOURCE') {
      return <span>keep original only</span>
    }
  } else if (taskMerge.pair.source && !taskMerge.pair.target) {
    if (taskMerge.action === 'MOVE_SOURCE') {
      return <span>move to original</span>
    } else if (taskMerge.action === 'DELETE_SOURCE' || taskMerge.action === 'NO_ACTION') {
      return <span>delete</span>
    }
  } else if (!taskMerge.pair.source && taskMerge.pair.target) {
    if (taskMerge.action === 'NO_ACTION') {
      return <span>keep</span>
    }
  }
  return null
}

function renderTaskTitle(task1: ParticipantTask | undefined, task2: ParticipantTask | undefined) {
  if (!task1 && !task2) {
    return <span className="text-muted fst-italic">none</span>
  }
  const task = (task1 ?? task2)!
  return <span>
    {task.taskType} - {task.targetName || ''}
  </span>
}

function renderMergeUserInfo(mergePlan: ParticipantUserMerge, isSource: boolean) {
  const ppUser = isSource ? mergePlan.ppUsers.pair.source! : mergePlan.ppUsers.pair.target!
  const user = isSource ? mergePlan.users.pair.source! : mergePlan.users.pair.target!
  return <div>
    <span>{user.username}</span>
    { ppUser.profile && <span className="text-muted fst-italic ps-2">
        ({ppUser?.profile?.givenName} {ppUser?.profile?.familyName})
    </span> }
    { !ppUser.profile && <span className="text-muted fst-italic ps-2">(no profile)</span> }
  </div>
}

const TASK_TYPE_ORDER = ['CONSENT', 'SURVEY', 'KIT_REQUEST', 'OUTREACH', 'ADMIN_FORM', 'ADMIN_NOTE']

/** Sorts tasks based on their types, then based on status, and then based on their internal ordering */
function taskComparator(taskA?: ParticipantTask, taskB?: ParticipantTask) {
  if (!taskA || !taskB) {
    return 0
  }
  const typeOrder = TASK_TYPE_ORDER.indexOf(taskA.taskType) - TASK_TYPE_ORDER.indexOf(taskB.taskType)
  if (typeOrder != 0) {
    return typeOrder
  }
  return taskA.taskOrder - taskB.taskOrder
}
