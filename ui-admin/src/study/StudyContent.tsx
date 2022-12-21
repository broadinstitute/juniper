import React from 'react'
import Select from 'react-select'

import { useStudyEnvironmentOutlet } from './StudyEnvironmentProvider'
import { Link } from 'react-router-dom'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus'
import { faExternalLink } from '@fortawesome/free-solid-svg-icons/faExternalLink'

/** renders the main configuration page for a study environment */
function StudyContent() {
  const { study, currentEnv } = useStudyEnvironmentOutlet()
  const contentHeaderStyle = {
    marginRight: '1em',
    paddingRight: '1em',
    borderRight: '1px solid #ccc',
    minWidth: '200px',
    maxWidth: '200px'
  }
  const consequenceOptions = [
    { label: 'Send blood kit', value: 'bloodKit' },
    { label: 'Request medical records', value: 'medRecords' }
  ]

  const triggerOptions = [
    { label: 'completed', value: 'completion' },
    { label: 'started', value: 'start' }
  ]

  const triggerTarget = [
    { label: 'Enrollment', value: 'enrollment' },
    { label: 'Consent', value: 'consent' },
    { label: 'Medical History form', value: 'medicalHistory' }
  ]
  const participantRootPath = process.env.REACT_APP_PARTICIPANT_APP_ROOT
  const participantProtocol = process.env.REACT_APP_PARTICIPANT_APP_PROTOCOL
  const participantHost = `${currentEnv.environmentName.toLowerCase()}.${study.shortcode}.${participantRootPath}`
  const participantUrl = `${participantProtocol}://${participantHost}`

  const preRegSurvey = currentEnv.preRegSurvey
  const envConfig = currentEnv.studyEnvironmentConfig

  return <div className="StudyContent container">
    <div className="row">
      <div className="col-12 p-3">
        <h4>{currentEnv.environmentName.toLowerCase()} environment</h4>
        <a href={participantUrl} target="_blank">Participant view <FontAwesomeIcon icon={faExternalLink}/></a>
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
              <h6>Pre-registration questionnaire</h6>
            </div>
            <div className="flex-grow-1">
              { preRegSurvey && <div>
                {preRegSurvey.name} <span className="detail">v{preRegSurvey.version}</span>
                <Link to={`preReg/${preRegSurvey.stableId}?mode=view`} className="ms-4">
                  view
                </Link>
                <Link to={`preReg/${preRegSurvey.stableId}`} className="ms-3">edit</Link>
              </div>}
            </div>
          </li>
          <li className="list-group-item d-flex">
            <div style={contentHeaderStyle}>
              <h6>Consent forms</h6>
            </div>
            <div className="flex-grow-1">
              not yet implemented
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
              <h6>Triggers & Integrations</h6>
            </div>
            <div className="flex-grow-1">
              <ul className="list-group">
                <li className="list-group-item d-flex">
                  <Select options={consequenceOptions}/> &nbsp; when &nbsp;
                  <Select options={triggerTarget}/> &nbsp; is &nbsp;
                  <Select options={triggerOptions}/>
                  <button className="btn btn-secondary">
                    <FontAwesomeIcon icon={faPlus}></FontAwesomeIcon> Add
                  </button>
                </li>
              </ul>
            </div>
          </li>
        </ul> }
        { !currentEnv.studyEnvironmentConfig.initialized && <div>Not yet initialized</div> }
      </div>
    </div>
  </div>
}

export default StudyContent
