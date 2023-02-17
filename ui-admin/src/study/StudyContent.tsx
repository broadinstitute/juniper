import React from 'react'

import { StudyEnvContextT } from './StudyEnvironmentRouter'
import { Link } from 'react-router-dom'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus'
import { faExternalLink } from '@fortawesome/free-solid-svg-icons/faExternalLink'
import Api from 'api/api'
import NotificationConfigTypeDisplay, { deliveryTypeDisplayMap } from './notifications/NotifcationConfigTypeDisplay'

/** renders the main configuration page for a study environment */
function StudyContent({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { currentEnv, portal } = studyEnvContext
  const contentHeaderStyle = {
    marginRight: '1em',
    paddingRight: '1em',
    borderRight: '1px solid #ccc',
    minWidth: '200px',
    maxWidth: '200px'
  }

  const preEnrollSurvey = currentEnv.preEnrollSurvey
  const envConfig = currentEnv.studyEnvironmentConfig

  return <div className="StudyContent container">
    <div className="row">
      <div className="col-12 p-3">
        <h4>{currentEnv.environmentName.toLowerCase()} environment</h4>
        <a href={Api.getParticipantLink(portal.shortcode, currentEnv.environmentName)}
          target="_blank">Participant view <FontAwesomeIcon icon={faExternalLink}/></a>
        { currentEnv.studyEnvironmentConfig.initialized && <ul className="list-group">
          <li className="list-group-item d-flex">
            <div className="flex-grow-1">
              <div className="form-group">
                <div className="form-group-item">
                  <label>Password protected:</label> { envConfig.passwordProtected ? 'Yes' : 'No'}
                  <br/>
                  <label>Accepting enrollment: </label> { envConfig.acceptingEnrollment ? 'Yes' : 'No'}
                </div>
              </div>
            </div>
          </li>
          <li className="list-group-item d-flex">
            <div style={contentHeaderStyle}>
              <h6>Pre-enrollment questionnaire</h6>
            </div>
            <div className="flex-grow-1">
              { preEnrollSurvey && <div>
                {preEnrollSurvey.name} <span className="detail">v{preEnrollSurvey.version}</span>
                <Link to={`preEnroll/${preEnrollSurvey.stableId}?mode=view`} className="ms-4">
                  view
                </Link>
                <Link to={`preEnroll/${preEnrollSurvey.stableId}`} className="ms-3">edit</Link>
              </div>}
            </div>
          </li>
          <li className="list-group-item d-flex">
            <div style={contentHeaderStyle}>
              <h6>Consent forms</h6>
            </div>
            <div className="flex-grow-1">
              <ul className="list-group">
                { currentEnv.configuredConsents.map(config => {
                  const consentForm = config.consentForm
                  return <li className="list-group-item" key={consentForm.stableId}>
                    {consentForm.name} <span className="detail">v{consentForm.version}</span>
                    <Link to={`consentForms/${consentForm.stableId}?mode=view`} className="ms-4">view</Link>
                    { <Link to={`consentForms/${consentForm.stableId}`} className="ms-3">
                      edit</Link> }
                  </li>
                }) }
              </ul>
            </div>
          </li>
          <li className="list-group-item d-flex">
            <div style={contentHeaderStyle}>
              <h6>Surveys</h6>
            </div>
            <div className="flex-grow-1">
              <ul className="list-group">
                { currentEnv.configuredSurveys.map(surveyConfig => {
                  const survey = surveyConfig.survey
                  return <li className="list-group-item" key={survey.stableId}>
                    {survey.name} <span className="detail">v{survey.version}</span>
                    <Link to={`surveys/${survey.stableId}?mode=view`} className="ms-4">view</Link>
                    { <Link to={`surveys/${survey.stableId}`} className="ms-3">
                      edit</Link> }
                  </li>
                }) }
              </ul>
            </div>
          </li>
          <li className="list-group-item d-flex">
            <div style={contentHeaderStyle}>
              <h6>Participant Notifications</h6>
            </div>
            <div className="flex-grow-1">
              <ul className="list-group">
                { currentEnv.notificationConfigs.map(config => <li className="list-group-item" key={config.id}>
                  <div className="d-flex justify-content-between">
                    <NotificationConfigTypeDisplay config={config}/>
                    <span>{deliveryTypeDisplayMap[config.deliveryType]}</span>
                    <Link to={`notificationConfigs/${config.id}`}>Configure</Link>
                  </div>
                </li>
                ) }
              </ul>
              <button className="btn btn-secondary" onClick={() => alert('not yet implemented')}>
                <FontAwesomeIcon icon={faPlus}/> Add
              </button>
            </div>
          </li>
        </ul> }
        { !currentEnv.studyEnvironmentConfig.initialized && <div>Not yet initialized</div> }
      </div>
    </div>
  </div>
}

export default StudyContent
