import React, { useState } from 'react'
import 'react-querybuilder/dist/query-builder.scss'
import { doApiLoad } from 'api/api-utils'
import Api, { EnrolleeMergePlan, MergeAction, MergeActionAction, ParticipantTask, ParticipantUserMerge } from 'api/api'
import { Enrollee } from '@juniper/ui-core'
import LoadingSpinner from 'util/LoadingSpinner'
import { renderPageHeader } from 'util/pageUtils'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowRight } from '@fortawesome/free-solid-svg-icons'
import { successNotification } from '../../../util/notifications'
import { useNavigate } from 'react-router-dom'
import { participantListPath, StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { Store } from 'react-notifications-component'

const mergeArrow = <FontAwesomeIcon icon={faArrowRight} className="px-2"/>

const MERGE_ACTION_LABELS: Record<MergeActionAction, React.ReactNode> = {
  'MOVE_SOURCE': <span>move source</span>,
  'NO_ACTION': <span>Do nothing</span>,
  'MERGE': <span>Merge</span>,
  'DELETE_SOURCE': <span className="text-danger">delete source</span>,
  'MOVE_SOURCE_DELETE_TARGET': <span>move source &amp; <span className="text-danger">delete target</span></span>
}

/**
 * Returns a cohort builder modal
 */
export default function ParticipantMergeView({ source, target, studyEnvContext }:
 {source?: string, target?: string, studyEnvContext: StudyEnvContextT }) {
  const [sourceEmail, setSourceEmail] = useState(source || '')
  const [targetEmail, setTargetEmail] = useState(target || '')
  const [mergePlan, setMergePlan] = useState<ParticipantUserMerge>()
  const [isLoading, setIsLoading] = useState(false)
  const navigate = useNavigate()
  const planMerge = () => {
    if (!sourceEmail || !targetEmail || !targetEmail.includes('@') || !sourceEmail.includes('@')) { return }
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
      navigate(participantListPath(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName))
    }, { setIsLoading })
  }

  return <div className="container-fluid px-4 py-2">
    <div className="d-flex align-items-center justify-content-between ">
      {renderPageHeader('Merge Participants')}
    </div>

    <div className="d-flex">
      <div className="container">
        <h5>Source Account</h5>
        <input type="text" className="form-control" id="source"
          onChange={e => setSourceEmail(e.target.value)} value={sourceEmail}/>
        <p className="text-muted fst-italic">The account to delete/merge.</p>
      </div>
      <div className="container">
        <h5>Target Account</h5>

        <input type="text" className="form-control" id="target" size={20}
          onChange={e => setTargetEmail(e.target.value)} value={targetEmail}/>
        <p className="text-muted fst-italic">The account to keep.</p>
      </div>
    </div>
    <Button variant="primary" onClick={planMerge} tooltip={'Plan only, will not take actions'}
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
              enrolleeMerge={merge} key={index}/>) }
          </div>
          <Button variant="primary" onClick={executeMerge}>Execute merge</Button>
        </div> }
      </LoadingSpinner>
    </div>
  </div>
}

function EnrolleeMergePlanView({ enrolleeMerge }: {enrolleeMerge: MergeAction<Enrollee, EnrolleeMergePlan>}) {
  return <div className="mx-4">
    { enrolleeMerge.pair.source?.shortcode }
    {mergeArrow}
    { enrolleeMerge.pair.target?.shortcode }
    <table>
      <thead>
        <th>source</th>
        <th>action</th>
        <th>target</th>
      </thead>
      <tbody>
        { enrolleeMerge.mergePlan.tasks.map((taskMerge, index) =>
          <MergeTaskView taskMerge={taskMerge} key={index}/>) }
      </tbody>
    </table>
  </div>
}

function MergeTaskView({ taskMerge }: {taskMerge: MergeAction<ParticipantTask, object>}) {
  const source = taskMerge.pair.source
  const target = taskMerge.pair.target
  return <tr>
    <td>{renderTaskSummary(source)}</td>
    <td className="px-2">{MERGE_ACTION_LABELS[taskMerge.action]}</td>
    <td>{renderTaskSummary(target)}</td>
  </tr>
}

function renderTaskSummary(task?: ParticipantTask) {
  if (!task) {
    return <span className="text-muted fst-italic">none</span>
  }
  return <span>
    {task.taskType} - {task.targetName || ''} <span className="text-muted fst-italic">({task.status})</span>
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
