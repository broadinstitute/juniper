import React, {
  useEffect,
  useState
} from 'react'
import {
  StudyEnvironmentSurvey,
  SurveyResponse
} from 'api/api'

import {
  useLocation,
  useNavigate,
  useParams
} from 'react-router-dom'
import SurveyFullDataView from './SurveyFullDataView'
import SurveyResponseEditor from './SurveyResponseEditor'
import { ResponseMapT } from '../enrolleeView/EnrolleeView'
import { EnrolleeParams } from '../enrolleeView/useRoutedEnrollee'
import {
  AutosaveStatus,
  Enrollee,
  instantToDefaultString
} from '@juniper/ui-core'
import DocumentTitle from 'util/DocumentTitle'
import _uniq from 'lodash/uniq'
import pluralize from 'pluralize'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
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

/** Show responses for a survey based on url param */
export default function SurveyResponseView({ enrollee, responseMap, updateResponseMap, studyEnvContext, onUpdate }: {
  enrollee: Enrollee, responseMap: ResponseMapT,
  updateResponseMap: (stableId: string, response: SurveyResponse) => void,
  studyEnvContext: StudyEnvContextT, onUpdate: () => void
}) {
  const params = useParams<EnrolleeParams>()

  const surveyStableId: string | undefined = params.surveyStableId

  if (!surveyStableId) {
    return <div>Select a survey</div>
  }
  const surveyAndResponses = responseMap[surveyStableId]
  if (!surveyAndResponses) {
    return <div>This survey has not been assigned to this participant</div>
  }
  // key forces the component to be destroyed/remounted when different survey selected
  return <RawEnrolleeSurveyView key={surveyStableId} enrollee={enrollee} studyEnvContext={studyEnvContext}
    updateResponseMap={updateResponseMap}
    configSurvey={surveyAndResponses.survey} response={surveyAndResponses.response} onUpdate={onUpdate}/>
}

/** show responses for a survey */
export function RawEnrolleeSurveyView({
  enrollee, configSurvey, response, studyEnvContext, onUpdate,
  updateResponseMap
}: {
  enrollee: Enrollee, configSurvey: StudyEnvironmentSurvey,
  updateResponseMap: (stableId: string, response: SurveyResponse) => void,
  response?: SurveyResponse, studyEnvContext: StudyEnvContextT, onUpdate: () => void
}) {
  const { user } = useUser()
  const navigate = useNavigate()
  const location = useLocation()
  // Admin-only forms should default to edit mode
  const [isEditing, setIsEditing] = useState(configSurvey.survey.surveyType === 'ADMIN')
  const [autosaveStatus, setAutosaveStatus] = useState<AutosaveStatus | undefined>()
  const [showJustificationModal, setShowJustificationModal] = useState(false)
  const [justification, setJustification] = useState<string>('')

  return <div>
    <DocumentTitle title={`${enrollee.shortcode} - ${configSurvey.survey.name}`}/>
    <h4>{configSurvey.survey.name}</h4>
    <div>
      <div className="d-flex align-items-center justify-content-between">
        <div>{surveyTaskStatus(response)}</div>
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
              aria-label={isEditing ? 'Editing' : 'Viewing'}
            >
              {isEditing ?
                <><FontAwesomeIcon icon={faPencil} className="fa-lg"/> Editing</> :
                <><FontAwesomeIcon icon={faEye} className="fa-lg"/> Viewing</>
              }
            </Button>
            <div className="dropdown-menu" aria-labelledby="surveyModeMenu">
              <DropdownButton
                onClick={() => setIsEditing(false)}
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
                        setIsEditing(true)
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
                  setIsEditing(false)
                  navigate(`print${location.search}`)
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
      {(!isEditing && !response?.answers.length) && <div>No response for enrollee {enrollee.shortcode}</div>}
      {(!isEditing && response?.answers.length) && <SurveyFullDataView
        responseId={response.id}
        enrollee={enrollee}
        answers={response?.answers || []}
        survey={configSurvey.survey}
        studyEnvContext={studyEnvContext}/>}
      {isEditing && user && <SurveyResponseEditor studyEnvContext={studyEnvContext}
        updateResponseMap={updateResponseMap}
        justification={justification}
        setAutosaveStatus={setAutosaveStatus}
        survey={configSurvey.survey} response={response} adminUserId={user.id}
        enrollee={enrollee} onUpdate={onUpdate}/>}
      {showJustificationModal && <JustifyChangesModal
        saveWithJustification={justification => {
          setJustification(justification)
          setShowJustificationModal(false)
          setIsEditing(true)
        }}
        onDismiss={() => setShowJustificationModal(false)}
        changes={[]}
        confirmText={'Continue to edit'}
      />}
    </div>
  </div>
}

function surveyTaskStatus(surveyResponse?: SurveyResponse) {
  let versionString = ''
  if (surveyResponse && surveyResponse.answers.length) {
    const answerVersions = _uniq(surveyResponse.answers.map(ans => ans.surveyVersion))
    versionString = `${pluralize('version', answerVersions.length)} ${answerVersions.join(', ')}`
  }

  if (!surveyResponse) {
    return <span className="badge bg-secondary p-2"><FontAwesomeIcon icon={faEmptyCircle}/> Not Started</span>
  }
  return <div className="d-flex align-items-center">
    {surveyResponse.complete ?
      <span className="badge bg-success p-2">
        <FontAwesomeIcon icon={faCircleCheck}/> Complete
      </span> :
      <span className="badge bg-secondary p-2">
        <FontAwesomeIcon icon={faCircleHalfStroke}/> In Progress
      </span>}
    <div className="vr m-3"></div>
    <span>{surveyResponse.complete?
      'Completed' : 'Last Updated'} {instantToDefaultString(surveyResponse.createdAt)} ({versionString})</span>
  </div>
}

type DropdownButtonProps = {
  onClick: () => void,
  icon?: IconDefinition,
  label: string,
  disabled?: boolean,
  description?: string
}

const DropdownButton = (props: DropdownButtonProps) => {
  const { onClick, icon, label, disabled, description } = props
  return (
    <button
      className={classNames('dropdown-item d-flex align-items-center', { disabled })}
      type="button"
      onClick={onClick}>
      {icon && <FontAwesomeIcon icon={icon} className="me-2"/>}
      <div className="d-flex flex-column">
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
