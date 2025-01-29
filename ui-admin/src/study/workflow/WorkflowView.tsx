import React, {
  useEffect,
  useState
} from 'react'
import {
  Link,
  useNavigate
} from 'react-router-dom'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus'
import {
  paramsFromContext,
  StudyEnvContextT,
  studyEnvFormsParamsPath,
  studyEnvPreEnrollPath,
  studyEnvSurveyPath,
  studyEnvTriggerPath,
  studyEnvWorkflowPath,
  triggerPath
} from '../StudyEnvironmentRouter'
import { renderPageHeader } from 'util/pageUtils'
import { LoadedPortalContextT } from '../../portal/PortalProvider'
import {
  StudyEnvironmentSurvey,
  StudyEnvParams,
  Survey,
  Trigger
} from '@juniper/ui-core'
import Api from 'api/api'
import { useLoadingEffect } from 'api/api-utils'
import LoadingSpinner from 'util/LoadingSpinner'
import CreateTriggerModal from '../notifications/CreateTriggerModal'
import {
  faAsterisk,
  faEdit,
  faFilter,
  faTasks
} from '@fortawesome/free-solid-svg-icons'
import {
  faCalendarAlt,
  faCheckSquare,
  faClipboard,
  faEnvelope
} from '@fortawesome/free-regular-svg-icons'
import InfoPopup from 'components/forms/InfoPopup'
import _sortBy from 'lodash/sortBy'
import {
  minutesToDayString,
  triggerName
} from './workflowUtils'
import { EllipsisDropdownButton } from 'components/forms/Button'

/** shows configuration of notifications for a study */
export default function WorkflowView({ studyEnvContext, portalContext }:
  {studyEnvContext: StudyEnvContextT, portalContext: LoadedPortalContextT}) {
  const currentEnv = studyEnvContext.currentEnv
  const navigate = useNavigate()
  const [triggers, setTriggers] = useState<Trigger[]>([])
  const [studyEnvSurveys, setStudyEnvSurveys] = useState<StudyEnvironmentSurvey[]>([])
  const [preEnrollSurvey, setPreEnrollSurvey] = useState<Survey>()
  const [triggerOpts, setTriggerOpts] = useState<Partial<Trigger>>({})
  const [previousEnv, setPreviousEnv] = useState<string>(currentEnv.environmentName)
  const [showCreateModal, setShowCreateModal] = useState(false)

  const boxClasses = 'p-3 my-3 bg-light'
  const itemClasses = 'my-2 py-2'

  const { isLoading, reload } = useLoadingEffect(async () => {
    const [triggerList, surveyList] = await Promise.all([
      Api.findTriggersForStudyEnv(portalContext.portal.shortcode,
        studyEnvContext.study.shortcode, currentEnv.environmentName),
      Api.findConfiguredSurveys(portalContext.portal.shortcode, studyEnvContext.study.shortcode,
        currentEnv.environmentName, true, undefined)
    ])
    if (currentEnv.preEnrollSurveyId) {
      const preEnrollSurvey = await Api.getSurveyById(portalContext.portal.shortcode, currentEnv.preEnrollSurveyId)
      setPreEnrollSurvey(preEnrollSurvey)
    }
    setTriggers(triggerList)
    setStudyEnvSurveys(_sortBy(surveyList, 'surveyOrder'))
  }, [currentEnv.environmentName, studyEnvContext.study.shortcode])

  useEffect(() => {
    if (previousEnv !== currentEnv.environmentName) {
      // the user has changed the environment -- we need to clear the id off the path if there
      navigate(studyEnvWorkflowPath(paramsFromContext(studyEnvContext)))
      setPreviousEnv(currentEnv.environmentName)
    }
  }, [currentEnv.environmentName])

  const onCreate = (createdConfig: Trigger) => {
    reload()
    navigate(triggerPath(createdConfig, studyEnvContext.currentEnvPath))
    setShowCreateModal(false)
  }

  const formEditLink = <Link to={studyEnvFormsParamsPath(paramsFromContext(studyEnvContext))}>
    <FontAwesomeIcon icon={faEdit} className="ms-2 fa-xs fw-normal"/>
  </Link>

  return <div className="container-fluid px-4 py-2">
    { renderPageHeader('Participant Flow') }
    <ul className="list-unstyled">
      {isLoading && <LoadingSpinner/>}

      <li className={boxClasses} >
        <h3 className="h5">Pre-enroll {formEditLink}</h3>
        <div className={itemClasses}>
          { preEnrollSurvey &&
            <div>
              <FontAwesomeIcon icon={faClipboard} className="me-2"/>
              <Link to={studyEnvPreEnrollPath(paramsFromContext(studyEnvContext), preEnrollSurvey.stableId)}>
                {preEnrollSurvey.name}
              </Link>
            </div>
          }
          { !preEnrollSurvey && <><span className="fst-italic">None</span>
            <button className="btn btn-secondary" onClick={() => alert('go to forms page')}>
              <FontAwesomeIcon icon={faPlus}/> Add
            </button>
          </> }
        </div>
      </li>
      <li className={boxClasses} >
        <h3 className="h5 d-flex align-items-center">Enrollment
          <AddTriggerMenu triggerOpts={{ eventType: 'STUDY_ENROLLMENT' }}
            setTriggerOpts={setTriggerOpts}
            setShowCreateModal={setShowCreateModal}/></h3>

        <ul className="list-unstyled">
          { triggers.filter(trigger => trigger.eventType === 'STUDY_ENROLLMENT' && trigger.triggerType !== 'AD_HOC')
            .map(trigger =>
              <TriggerListItem trigger={trigger} key={trigger.id} studyEnvParams={paramsFromContext(studyEnvContext)}/>
            )
          }
        </ul>
      </li>
      <li className={boxClasses}>
        <h3 className="h5">Consents {formEditLink}</h3>
        <ul className="list-unstyled">
          { studyEnvSurveys.filter(studyEnvSurveys => studyEnvSurveys.survey.surveyType === 'CONSENT')
            .map(studyEnvSurvey =>
              <SurveyListItem studyEnvSurvey={studyEnvSurvey} key={studyEnvSurvey.id} triggers={triggers}
                setShowCreateModal={setShowCreateModal} triggerOpts={triggerOpts}
                setTriggerOpts={setTriggerOpts}
                studyEnvParams={paramsFromContext(studyEnvContext)}/>
            )
          }
        </ul>
        <div className="bg-white p-3">
          <div>
            <h4 className="h6 d-flex align-items-center">Actions &amp; Reminders
              <AddTriggerMenu triggerOpts={{ taskType: 'CONSENT',  eventType: 'STUDY_CONSENT' }}
                setTriggerOpts={setTriggerOpts}
                setShowCreateModal={setShowCreateModal}/>
            </h4>
          </div>
          <ul className="list-unstyled">
            { triggers.filter(trigger => trigger.eventType === 'STUDY_CONSENT' || trigger.taskType === 'CONSENT')
              .map(trigger =>
                <TriggerListItem trigger={trigger} key={trigger.id}
                  studyEnvParams={paramsFromContext(studyEnvContext)}/>
              )
            }
          </ul>
        </div>
      </li>
      <li className={boxClasses}>
        <h3 className="h5">Research Surveys {formEditLink}</h3>
        <ul className="list-unstyled">
          { studyEnvSurveys.filter(studyEnvSurveys => studyEnvSurveys.survey.surveyType === 'RESEARCH')
            .map(studyEnvSurvey =>
              <SurveyListItem studyEnvSurvey={studyEnvSurvey} key={studyEnvSurvey.id} triggers={triggers}
                setShowCreateModal={setShowCreateModal} triggerOpts={triggerOpts}
                setTriggerOpts={setTriggerOpts}
                studyEnvParams={paramsFromContext(studyEnvContext)}/>
            )
          }
        </ul>
        <div className="bg-white p-3">
          <div>
            <h4 className="h6 d-flex align-items-center">Actions &amp; Reminders
              <AddTriggerMenu triggerOpts={{ taskType: 'SURVEY', eventType: 'SURVEY_RESPONSE' }}
                setTriggerOpts={setTriggerOpts}
                setShowCreateModal={setShowCreateModal}/>
            </h4>
          </div>
          <ul className="list-unstyled">
            { triggers.filter(trigger => (trigger.taskType === 'SURVEY' && trigger.filterTargetStableIds.length === 0)
              && trigger.triggerType !== 'AD_HOC')
              .map(trigger =>
                <TriggerListItem trigger={trigger} key={trigger.id}
                  studyEnvParams={paramsFromContext(studyEnvContext)}/>
              )
            }
          </ul>
        </div>
      </li>
      <li className={boxClasses}>
        <h3 className="h5">Outreach Surveys {formEditLink}</h3>
        <ul className="list-unstyled">
          { studyEnvSurveys.filter(studyEnvSurveys => studyEnvSurveys.survey.surveyType === 'OUTREACH')
            .map(studyEnvSurvey =>
              <SurveyListItem studyEnvSurvey={studyEnvSurvey} key={studyEnvSurvey.id} triggers={triggers}
                setShowCreateModal={setShowCreateModal} triggerOpts={triggerOpts}
                setTriggerOpts={setTriggerOpts}
                studyEnvParams={paramsFromContext(studyEnvContext)}/>
            )
          }
        </ul>
        <div className="bg-white p-3">
          <div >
            <h4 className="h6 d-flex align-items-center">Actions &amp; Reminders
              <AddTriggerMenu triggerOpts={{ taskType: 'OUTREACH',  eventType: 'SURVEY_RESPONSE' }}
                setTriggerOpts={setTriggerOpts}
                setShowCreateModal={setShowCreateModal}/>
            </h4>
          </div>
          <ul className="list-unstyled">
            { triggers.filter(trigger => trigger.taskType === 'OUTREACH' && trigger.filterTargetStableIds.length === 0)
              .map(trigger =>
                <TriggerListItem trigger={trigger} key={trigger.id}
                  studyEnvParams={paramsFromContext(studyEnvContext)}/>
              )
            }
          </ul>
        </div>
      </li>
      <li className={boxClasses}>
        <h3 className="h5">Kits</h3>
        <ul className="list-unstyled">
          { triggers.filter(trigger => (trigger.eventType === 'KIT_SENT' || trigger.eventType === 'KIT_RECEIVED' ||
            trigger.taskType === 'KIT_REQUEST'))
            .map(trigger =>
              <TriggerListItem trigger={trigger} key={trigger.id} studyEnvParams={paramsFromContext(studyEnvContext)}/>
            )
          }
        </ul>
      </li>
      <li className={boxClasses}>
        <h3 className="h5">Ad-Hoc</h3>
        <ul className="list-unstyled">
          { triggers.filter(trigger => trigger.triggerType === 'AD_HOC')
            .map(trigger =>
              <TriggerListItem trigger={trigger} key={trigger.id} studyEnvParams={paramsFromContext(studyEnvContext)}/>
            )
          }
        </ul>
      </li>

    </ul>
    { showCreateModal && <CreateTriggerModal studyEnvContext={studyEnvContext} initialOpts={triggerOpts}
      onDismiss={() => setShowCreateModal(false)} onCreate={onCreate}
    /> }
  </div>
}

const AddTriggerMenu = ({ triggerOpts, setTriggerOpts, setShowCreateModal }:
  {triggerOpts: Partial<Trigger>,
    setTriggerOpts: (opts: Partial<Trigger>) => void,
    setShowCreateModal: (show: boolean) => void}) => {
  return <div className="nav-item dropdown ms-2">
    <EllipsisDropdownButton aria-label="configure" className="ms-auto"/>
    <div className="dropdown-menu">
      <ul className="list-unstyled">
        {triggerOpts.taskType && <li>
          <button className="dropdown-item"
            onClick={() => {
              setTriggerOpts({ ...triggerOpts, triggerType: 'TASK_REMINDER', eventType: undefined })
              setShowCreateModal(true)
            }}>
            Add reminder
          </button>
        </li> }
        {triggerOpts.taskType && <li>
          <button className="dropdown-item"
            onClick={() => {
              setTriggerOpts({
                ...triggerOpts,
                triggerType: 'TASK_REMINDER',
                eventType: undefined,
                maxNumReminders: 1,
                afterMinutesIncomplete: 0,
                reminderIntervalMinutes: 0,
                rule: `{enrollee.createdAt} < '${new Date().toISOString()}'`
              })
              setShowCreateModal(true)
            }}>
                Add launch notification
          </button>
        </li>}
        {triggerOpts.eventType && <li>
          <button className="dropdown-item"
            onClick={() => {
              setTriggerOpts({ ...triggerOpts, triggerType: 'EVENT', taskType: undefined })
              setShowCreateModal(true)
            }}>
            Add action
          </button>
        </li> }
      </ul>
    </div>
  </div>
}

const SurveyListItem = ({ studyEnvSurvey, studyEnvParams, triggers, triggerOpts, setTriggerOpts, setShowCreateModal }:
  { studyEnvSurvey: StudyEnvironmentSurvey, studyEnvParams: StudyEnvParams, triggers: Trigger[],
    triggerOpts: Partial<Trigger>,
    setTriggerOpts: (opts: Partial<Trigger>) => void,
    setShowCreateModal: (show: boolean) => void}) => {
  const survey = studyEnvSurvey.survey
  const matchedTriggers = triggers.filter(trigger => trigger.filterTargetStableIds.includes(survey.stableId))

  return <li className="py-2">
    <div className={'d-flex align-items-center'}>
      { survey.surveyType === 'CONSENT' && <FontAwesomeIcon icon={faCheckSquare} className="me-2"/> }
      { survey.surveyType === 'RESEARCH' && <FontAwesomeIcon icon={faClipboard} className="me-2"/> }
      { survey.eligibilityRule && <span className="me-2 text-muted fst-italic">
        <InfoPopup content={survey.eligibilityRule} marginClass="me-2"
          target={<FontAwesomeIcon icon={faFilter} title="conditional logic"/>}/>
      </span> }
      { survey.required && <span className="me-2 text-muted">
        <FontAwesomeIcon icon={faAsterisk} title={'required'}/>
      </span> }
      <Link to={studyEnvSurveyPath(studyEnvParams, studyEnvSurvey.survey.stableId, survey.version)}>
        { survey.name}
      </Link>
      <AddTriggerMenu triggerOpts={{
        ...triggerOpts,
        taskType: survey.surveyType === 'RESEARCH' ? 'SURVEY' : survey.surveyType,
        eventType: 'SURVEY_RESPONSE',
        filterTargetStableIds: [survey.stableId]
      }}
      setTriggerOpts={setTriggerOpts}
      setShowCreateModal={setShowCreateModal}/>
    </div>
    <ul className="list-unstyled ms-5">
      {matchedTriggers.map(trigger => <TriggerListItem key={trigger.id}
        trigger={trigger} studyEnvParams={studyEnvParams}/>)}
    </ul>
  </li>
}
const TriggerListItem = ({ trigger, studyEnvParams }:
  { trigger: Trigger, studyEnvParams: StudyEnvParams }) => {
  return <li className="py-2">
    { (trigger.actionType === 'NOTIFICATION' || trigger.actionType === 'ADMIN_NOTIFICATION') &&
      <div>
        <FontAwesomeIcon icon={faEnvelope} className="me-2"/>
        <Link to={studyEnvTriggerPath(studyEnvParams, trigger)}>
          {triggerName(trigger)}
          <span className="text-muted fst-italic ms-3">
    ({trigger.emailTemplate.localizedEmailTemplates[0].subject})
          </span>
        </Link>
      </div>
    }
    { trigger.actionType === 'TASK_STATUS_CHANGE' &&
      <div>
        <FontAwesomeIcon icon={faTasks} className="me-2"/>
        <Link to={studyEnvTriggerPath(studyEnvParams, trigger)}>
          {triggerName(trigger)}
        </Link>
      </div>

    }
    { trigger.triggerType === 'TASK_REMINDER' &&
      <span className="text-muted fst-italic">
        <FontAwesomeIcon icon={faCalendarAlt} className="ms-3 me-2"/>
        <span>
      reminds after {minutesToDayString(trigger.afterMinutesIncomplete)} (max { trigger.maxNumReminders } reminders)
        </span>
      </span>
    }
  </li>
}


