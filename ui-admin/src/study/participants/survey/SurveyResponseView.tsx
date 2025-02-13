import React, {
  useEffect,
  useState
} from 'react'
import {
  ParticipantTask,
  StudyEnvironmentSurvey,
  SurveyResponse
} from 'api/api'

import {
  NavLink,
  useParams
} from 'react-router-dom'
import SurveyFullDataView from './SurveyFullDataView'
import SurveyResponseEditor from './SurveyResponseEditor'
import {
  ResponseMapT,
  surveyResponsePath
} from '../enrolleeView/EnrolleeView'
import { EnrolleeParams } from '../enrolleeView/useRoutedEnrollee'
import {
  AutosaveStatus,
  Enrollee,
  instantToDateString,
  instantToDefaultString,
  ParticipantTaskStatus,
  useTaskIdParam
} from '@juniper/ui-core'
import DocumentTitle from 'util/DocumentTitle'
import _uniq from 'lodash/uniq'
import pluralize from 'pluralize'
import {
  paramsFromContext,
  StudyEnvContextT
} from 'study/StudyEnvironmentRouter'
import {
  userHasPermission,
  useUser
} from 'user/UserProvider'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faCheck,
  faCircleCheck,
  faCircleHalfStroke,
  faEye,
  faMinus,
  faPencil,
  faPrint,
  faSave,
  faWarning
} from '@fortawesome/free-solid-svg-icons'
import { Button } from 'components/forms/Button'
import { IconDefinition } from '@fortawesome/fontawesome-svg-core'
import classNames from 'classnames'
import { faCircle as faEmptyCircle } from '@fortawesome/free-regular-svg-icons'
import JustifyChangesModal from '../JustifyChangesModal'
import { ParticipantDocumentListView } from './ParticipantDocumentListView'
import SurveyAssignModal from './SurveyAssignModal'
import TaskChangeModal from './TaskChangeModal'
import PrintFormView from 'study/participants/survey/PrintFormView'


type SurveyView = 'viewing' | 'editing' | 'printing'

/** Show responses for a survey based on url param */
export default function SurveyResponseView({ enrollee, responseMap, updateResponseMap, studyEnvContext, onUpdate }: {
  enrollee: Enrollee, responseMap: ResponseMapT,
  updateResponseMap: (stableId: string, response: SurveyResponse) => void,
  studyEnvContext: StudyEnvContextT, onUpdate: () => void
}) {
  const params = useParams<EnrolleeParams>()
  const [showAssignModal, setShowAssignModal] = useState(false)
  let { taskId } = useTaskIdParam()

  const surveyStableId: string | undefined = params.surveyStableId

  if (!surveyStableId) {
    return <div>Select a survey</div>
  }
  const surveyAndResponses = responseMap[surveyStableId]
  const isAssigned = surveyAndResponses.tasks.length > 0
  /** default to the most recent (tasks are already sorted by creation date) */
  if (!taskId) {
    taskId = surveyAndResponses.tasks[0]?.id
  }
  const task = surveyAndResponses.tasks.find(t => t.id === taskId)
  const response = surveyAndResponses.responses.find(r => task?.surveyResponseId === r.id)
  const showTaskBar = surveyAndResponses.tasks.length > 1
  // key forces the component to be destroyed/remounted when different survey selected
  return <div>
    <DocumentTitle title={`${enrollee.shortcode} - ${surveyAndResponses.survey.survey.name}`}/>
    <h4>{surveyAndResponses.survey.survey.name}</h4>
    {!isAssigned && <div className="d-flex align-items-center">
      <span className="text-muted fst-italic me-4">Not assigned</span>
      <Button variant={'secondary'} outline={true}
        onClick={() => setShowAssignModal(!showAssignModal)} className="ms-2">
            Assign
      </Button>
    </div>}
    {isAssigned && <>
      {showTaskBar && <div className="d-flex">
        {surveyAndResponses.tasks.map(task => <NavLink key={task.id}
          style={({ isActive }: { isActive: boolean }) => ({
            borderBottom: (isActive && task.id === taskId) ? '2px solid #708DBC' : '',
            background: (isActive && task.id === taskId) ? '#E1E8F7' : ''
          })}
          className="p-2"
          to={surveyResponsePath(studyEnvContext.currentEnvPath, enrollee.shortcode, surveyStableId, task.id)}>
          {instantToDateString(task.completedAt ?? task.createdAt)}
        </NavLink>)}
      </div>}
      <RawEnrolleeSurveyView key={`${surveyStableId}${taskId}`} enrollee={enrollee} studyEnvContext={studyEnvContext}
        updateResponseMap={updateResponseMap}
        task={task!}
        configSurvey={surveyAndResponses.survey} response={response} onUpdate={onUpdate}/>
    </>}
    {showAssignModal && <SurveyAssignModal studyEnvParams={paramsFromContext(studyEnvContext)}
      enrollee={enrollee} survey={surveyAndResponses.survey.survey}
      onDismiss={() => setShowAssignModal(false)}
      onSubmit={onUpdate}/>}
  </div>
}


/** show responses for a survey */
export function RawEnrolleeSurveyView({
  enrollee, configSurvey, task, response, studyEnvContext, onUpdate,
  updateResponseMap
}: {
  enrollee: Enrollee, configSurvey: StudyEnvironmentSurvey, task: ParticipantTask,
  updateResponseMap: (stableId: string, response: SurveyResponse) => void,
  response?: SurveyResponse, studyEnvContext: StudyEnvContextT, onUpdate: () => void
}) {
  const { user } = useUser()
  // Admin-only forms should default to edit mode
  const [view, setView] = useState<SurveyView>(configSurvey.survey.surveyType === 'ADMIN' ? 'editing' : 'viewing')
  const [autosaveStatus, setAutosaveStatus] = useState<AutosaveStatus | undefined>()
  const [showJustificationModal, setShowJustificationModal] = useState(false)
  const [justification, setJustification] = useState<string>('')
  const [showTaskModal, setShowTaskModal] = useState(false)

  return <div>
    <div>
      <div className="d-flex align-items-center justify-content-between">
        <div className="d-flex align-items-center">
          <div className="border rounded-3 p-0">
            {taskStatusIndicators[task.status]}
            <Button variant="secondary"
              tooltip={'Change task status or remove task from participant'}
              className="badge p-2 rounded-start-0" onClick={() => setShowTaskModal(!showTaskModal)}>
              <FontAwesomeIcon icon={faPencil}/>
            </Button>
          </div>
          {surveyTaskStatus(task, response)}
        </div>

        <div className="d-flex align-items-center">
          <AutosaveStatusIndicator status={autosaveStatus}/>
          <div className="dropdown">
            <Button
              data-bs-toggle='dropdown'
              className="dropdown-toggle border m-1"
              type="button"
              id="surveyModeMenu"
              variant="light"
              aria-haspopup="true"
              aria-expanded="false"
              aria-label={view === 'editing' ? 'Editing' : view === 'viewing' ? 'Viewing' : 'Printing'}
            >
              {view == 'editing' &&
                  <><FontAwesomeIcon icon={faPencil} className="fa-lg"/> Editing</>}
              {view == 'viewing' &&
                  <><FontAwesomeIcon icon={faEye} className="fa-lg"/> Viewing</>
              }
              {view == 'printing' &&
                  <><FontAwesomeIcon icon={faPrint} className="fa-lg"/> Printing</>
              }
            </Button>
            <div className="dropdown-menu" aria-labelledby="surveyModeMenu">
              <DropdownButton
                onClick={() => setView('viewing')}
                icon={faEye}
                label="Viewing"
                description="Read form responses"
              />
              <div className="dropdown-divider my-1"></div>
              {userHasPermission(user, studyEnvContext.portal.id, 'participant_data_edit') &&
                <>
                  <DropdownButton
                    onClick={() => {
                      if (configSurvey.survey.surveyType === 'ADMIN') {
                        setView('editing')
                      } else {
                        setShowJustificationModal(true)
                      }
                    }}
                    icon={faPencil}
                    disabled={!configSurvey.survey.allowAdminEdit}
                    label="Editing"
                    description="Edit form responses directly"
                  />
                  <div className="dropdown-divider my-1"></div>
                </>
              }
              <DropdownButton
                onClick={() => {
                  setView('printing')
                }}
                disabled={!response?.answers.length}
                icon={faPrint}
                label="Printing"
                description="Print or download the form"
              />
            </div>
          </div>
        </div>
      </div>
      <hr/>
      {(view === 'viewing' && (!response?.answers.length && !response?.participantFiles?.length)) &&
          <div>No response for enrollee {enrollee.shortcode}</div>}
      {view === 'viewing' && response?.answers.length !== undefined && response?.answers.length > 0 &&
          <SurveyFullDataView
            responseId={response.id}
            enrollee={enrollee}
            answers={response?.answers || []}
            survey={configSurvey.survey}
            studyEnvContext={studyEnvContext}/>}
      {view === 'viewing' && (
        response?.participantFiles?.length !== undefined && response.participantFiles.length > 0
      ) &&
          <ParticipantDocumentListView
            studyEnvContext={studyEnvContext}
            enrollee={enrollee}
            showAssociatedTasks={false}
            documents={response.participantFiles || []}/>
      }
      {view === 'editing' && user && <SurveyResponseEditor studyEnvContext={studyEnvContext}
        updateResponseMap={updateResponseMap}
        justification={justification}
        setAutosaveStatus={setAutosaveStatus}
        survey={configSurvey.survey} response={response} adminUserId={user.id}
        enrollee={enrollee} onUpdate={onUpdate}/>}
      {showJustificationModal && <JustifyChangesModal
        saveWithJustification={justification => {
          setJustification(justification)
          setShowJustificationModal(false)
          setView('editing')
        }}
        onDismiss={() => setShowJustificationModal(false)}
        changes={[]}
        confirmText={'Continue to edit'}
      />}
      {view === 'printing' && <PrintFormView answers={response?.answers || []} survey={configSurvey.survey}/>}
    </div>
    {showTaskModal && <TaskChangeModal studyEnvParams={paramsFromContext(studyEnvContext)}
      onDismiss={() => setShowTaskModal(false)}
      task={task} onSubmit={onUpdate}/>}
  </div>
}


const taskStatusIndicators: Record<ParticipantTaskStatus, React.ReactNode> = {
  NEW: <span className="badge bg-secondary p-2 rounded-end-0 border border-light">
    <FontAwesomeIcon icon={faEmptyCircle}/> Not Started</span>,
  VIEWED: <span className="badge bg-secondary p-2 rounded-end-0">
    <FontAwesomeIcon icon={faCircleHalfStroke}/> Viewed
  </span>,
  IN_PROGRESS: <span className="badge bg-secondary p-2 rounded-end-0">
    <FontAwesomeIcon icon={faCircleHalfStroke}/> In Progress
  </span>,
  REJECTED: <span className="badge bg-danger p-2 rounded-end-0">
    <FontAwesomeIcon icon={faCircleHalfStroke}/> Declined
  </span>,
  COMPLETE: <span className="badge bg-success p-2 rounded-end-0">
    <FontAwesomeIcon icon={faCircleCheck}/> Complete</span>,
  REMOVED: <span className="badge bg-secondary p-2 rounded-end-0">
    <FontAwesomeIcon icon={faMinus}/> Removed</span>
}

function surveyTaskStatus(task: ParticipantTask, surveyResponse?: SurveyResponse) {
  let versionString = ''
  if (surveyResponse && surveyResponse.answers.length) {
    const answerVersions = _uniq(surveyResponse.answers.map(ans => ans.surveyVersion))
    versionString = `${pluralize('version', answerVersions.length)} ${answerVersions.join(', ')}`
  }

  return <div className="d-flex align-items-center">
    {surveyResponse && <span className="ms-2">{surveyResponse.complete ?
      'Completed' : 'Last Updated'} {instantToDefaultString(surveyResponse.createdAt)} ({versionString})
    </span>}
  </div>
}

type DropdownButtonProps = {
  onClick: () => void,
  icon?: IconDefinition,
  className?: string,
  label: string,
  disabled?: boolean,
  description?: string
}

export const DropdownButton = (props: DropdownButtonProps) => {
  const { onClick, icon, label, disabled, description, className } = props
  return (
    <button
      className={classNames('dropdown-item d-flex align-items-center', { disabled }, className)}
      type="button"
      onClick={onClick}>
      {icon && <FontAwesomeIcon icon={icon} className="me-2"/>}
      <div className={'d-flex flex-column'}>
        <span>{label}</span>
        {description && <span className="text-muted" style={{ fontSize: '0.75em' }}>{description}</span>}
      </div>
    </button>
  )
}

const AutosaveStatusIndicator = ({ status }: { status?: AutosaveStatus }) => {
  const [displayStatus, setDisplayStatus] = useState<AutosaveStatus | undefined>(status)

  useEffect(() => {
    if (status) {
      setDisplayStatus(status)
    }
    if (status === 'SAVING') {
      setTimeout(() => setDisplayStatus(undefined), 1000)
    } else if (status === 'SAVED') {
      setTimeout(() => setDisplayStatus(undefined), 3000)
    }
  }, [status])

  return <>
    {(displayStatus === 'SAVING') && <span className="text-muted me-2">
      <FontAwesomeIcon icon={faSave}/> Autosaving...</span>}
    {(displayStatus === 'SAVED') && <span className="text-muted me-2">
      <FontAwesomeIcon icon={faCheck} className={'text-success'}/> Response saved</span>}
    {(displayStatus === 'ERROR') && <span className="text-muted me-2">
      <FontAwesomeIcon icon={faWarning} className={'text-danger'}/> Error saving response</span>}
  </>
}
