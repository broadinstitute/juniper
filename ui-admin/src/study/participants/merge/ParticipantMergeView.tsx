import React, { useEffect, useState } from 'react'
import 'react-querybuilder/dist/query-builder.scss'
import { doApiLoad } from 'api/api-utils'
import Api, { EnrolleeMergePlan, MergeAction, MergeActionAction, ParticipantTask, ParticipantUserMerge } from 'api/api'
import { Enrollee, StudyEnvParams } from '@juniper/ui-core'
import LoadingSpinner from 'util/LoadingSpinner'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowRight, faCheck, faExchange } from '@fortawesome/free-solid-svg-icons'
import { successNotification } from 'util/notifications'
import { Link, useNavigate } from 'react-router-dom'
import { paramsFromContext, participantAccountsPath, StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { Store } from 'react-notifications-component'
import { studyEnvParticipantPath } from '../ParticipantsRouter'

const mergeArrow = <FontAwesomeIcon icon={faArrowRight} className="px-2"/>

const MERGE_ACTION_LABELS: Record<MergeActionAction, Record<'source' |'target', React.ReactNode>> = {
  'MOVE_SOURCE': { source: <FontAwesomeIcon icon={faArrowRight}/>, target: <FontAwesomeIcon icon={faCheck}/> },
  'NO_ACTION': { source: '', target: '' },
  'MERGE': { source: 'merge', target: 'merge' },
  'DELETE_SOURCE': {
    source: '',
    target: <FontAwesomeIcon icon={faCheck}/>
  },
  'MOVE_SOURCE_DELETE_TARGET': {
    source: <FontAwesomeIcon icon={faArrowRight}/>,
    target: ''
  }
}

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
        <h5>Source Account</h5>
        <input type="text" className="form-control" id="source"
          onChange={e => setSourceEmail(e.target.value)} value={sourceEmail}/>
        <p className="text-muted fst-italic">The account to delete/merge.</p>
      </div>
      <Button variant="secondary" onClick={exchange}>
        <FontAwesomeIcon icon={faExchange}/>
      </Button>
      <div className="container">
        <h5>Target Account</h5>

        <input type="text" className="form-control" id="target" size={20}
          onChange={e => setTargetEmail(e.target.value)} value={targetEmail}/>
        <p className="text-muted fst-italic">The account to keep.</p>
      </div>
    </div>
    <Button variant="primary" onClick={() => planMerge(sourceEmail, targetEmail)}
      tooltip={'Plan only, will not take actions'}
    >Plan Merge</Button>
    <hr/>
    <div>
      <LoadingSpinner isLoading={isLoading}>
        {!mergePlan && <div> </div>}
        { mergePlan && <div>
          <h2 className="h4 my-3">Merge Plan</h2>
          <div className="d-flex">
            {renderMergeUserInfo(mergePlan, true)}
            {mergeArrow}
            {renderMergeUserInfo(mergePlan, false)}
          </div>
          <div>
            { mergePlan.enrollees.map((merge, index) => <EnrolleeMergePlanView
              enrolleeMerge={merge} key={index} studyEnvParams={paramsFromContext(studyEnvContext)}/>) }
          </div>
          <Button variant="primary" onClick={executeMerge} className="mt-3">Execute merge</Button>
        </div> }
      </LoadingSpinner>
    </div>
  </div>
}

function EnrolleeMergePlanView({ enrolleeMerge, studyEnvParams }:
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
        <th>source</th>
        <th> </th>
        <th> target</th>
        <th></th>
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
  const isSourceDeleted = taskMerge.action === 'DELETE_SOURCE'
  const isTargetDeleted = taskMerge.action === 'MOVE_SOURCE_DELETE_TARGET'
  return <tr>
    <td>{renderTaskSummary(source, isSourceDeleted)}</td>
    <td className="ps-2 pe-5">{ source && MERGE_ACTION_LABELS[taskMerge.action].source}</td>
    <td>{renderTaskSummary(target, isTargetDeleted)}</td>
    <td className="px-2">{ target && MERGE_ACTION_LABELS[taskMerge.action].target}</td>
  </tr>
}

function renderTaskSummary(task: ParticipantTask | undefined, isDeleted: boolean) {
  if (!task) {
    return <span className="text-muted fst-italic">none</span>
  }
  const summary = <span>
    {task.taskType} - {task.targetName || ''} <span className="text-muted fst-italic">({task.status})</span>
  </span>
  if (isDeleted) {
    return <del>{summary}</del>
  }
  return summary
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
