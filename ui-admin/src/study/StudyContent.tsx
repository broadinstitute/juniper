import React, { useState } from 'react'

import { StudyEnvContextT } from './StudyEnvironmentRouter'
import { Link } from 'react-router-dom'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus'
import { faExternalLink } from '@fortawesome/free-solid-svg-icons/faExternalLink'
import Api, { PortalEnvironmentConfig } from 'api/api'
import NotificationConfigTypeDisplay, { deliveryTypeDisplayMap } from './notifications/NotifcationConfigTypeDisplay'
import { faEdit } from '@fortawesome/free-solid-svg-icons'
import { useConfig } from '../providers/ConfigProvider'
import CreateSurveyModal from './surveys/CreateSurveyModal'

/** renders the main configuration page for a study environment */
function StudyContent({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { currentEnv, portal } = studyEnvContext
  const contentHeaderStyle = {
    padding: '1em 0 0 1em',
    borderBottom: '1px solid #f6f6f6'
  }


  const preEnrollSurvey = currentEnv.preEnrollSurvey
  const envConfig = currentEnv.studyEnvironmentConfig
  const portalEnvConfig = portal.portalEnvironments
    .find(env => env.environmentName === currentEnv.environmentName)?.portalEnvironmentConfig as PortalEnvironmentConfig
  const zoneConfig = useConfig()
  const isReadOnlyEnv = !(currentEnv.environmentName === 'sandbox')
  const [showCreateSurveyModal, setShowCreateSurveyModal] = useState(false)

  currentEnv.configuredSurveys
    .sort((a, b) => a.surveyOrder - b.surveyOrder)
  currentEnv.configuredConsents
    .sort((a, b) => a.consentOrder - b.consentOrder)

  return <div className="container row">
    <div className="col-12 p-3">
      <a href={Api.getParticipantLink(portalEnvConfig, zoneConfig.participantUiHostname,
        portal.shortcode, currentEnv.environmentName)}
      target="_blank">Participant view <FontAwesomeIcon icon={faExternalLink}/></a>
      { currentEnv.studyEnvironmentConfig.initialized && <ul className="list-unstyled">
        <li className="mb-3 bg-white">
          <div style={contentHeaderStyle}>
            <h6>Pre-enrollment questionnaire</h6>
          </div>
          <div className="flex-grow-1 p-3">
            { preEnrollSurvey && <ul className="list-unstyled"><li>
              <Link to={`preEnroll/${preEnrollSurvey.stableId}?readOnly=${isReadOnlyEnv}`}>
                {preEnrollSurvey.name} <span className="detail">v{preEnrollSurvey.version}</span>
              </Link>
            </li></ul>}
          </div>
        </li>
        <li className="mb-3 bg-white">
          <div style={contentHeaderStyle}>
            <h6>Consent forms</h6>
          </div>
          <div className="flex-grow-1 p-3">
            <ul className="list-unstyled">
              { currentEnv.configuredConsents.map((config, index) => {
                const consentForm = config.consentForm
                return <li key={index}>
                  <Link to={`consentForms/${consentForm.stableId}?readOnly=${isReadOnlyEnv}`}>
                    {consentForm.name} <span className="detail">v{consentForm.version}</span>
                  </Link>
                </li>
              }) }
            </ul>
          </div>
        </li>
        <li className="mb-3 bg-white">
          <div style={contentHeaderStyle}>
            <h6>Surveys</h6>
          </div>
          <div className="flex-grow-1 p-3">
            <ul className="list-unstyled">
              { currentEnv.configuredSurveys.map((surveyConfig, index) => {
                const survey = surveyConfig.survey
                return <li className="p-1" key={index}>
                  <Link to={`surveys/${survey.stableId}?readOnly=${isReadOnlyEnv}`}>
                    {survey.name} <span className="detail">v{survey.version}</span>
                  </Link>
                </li>
              }) }
              <li>
                <button className="btn btn-secondary" data-testid={'addSurvey'} onClick={() => {
                  setShowCreateSurveyModal(!showCreateSurveyModal)
                }}>
                  <FontAwesomeIcon icon={faPlus}/> Add
                </button>
              </li>
            </ul>

          </div>

        </li>
      </ul> }
      { <CreateSurveyModal studyEnvContext={studyEnvContext}
        isReadOnlyEnv={isReadOnlyEnv}
        show={showCreateSurveyModal}
        setShow={setShowCreateSurveyModal}/> }
      { !currentEnv.studyEnvironmentConfig.initialized && <div>Not yet initialized</div> }
    </div>
  </div>
}

export default StudyContent
