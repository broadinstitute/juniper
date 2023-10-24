import React, { useState } from 'react'

import { StudyEnvContextT } from './StudyEnvironmentRouter'
import { Link } from 'react-router-dom'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus'
import CreateSurveyModal from './surveys/CreateSurveyModal'
import { faEllipsisH } from '@fortawesome/free-solid-svg-icons'
import ArchiveSurveyModal from './surveys/ArchiveSurveyModal'
import DeleteSurveyModal from './surveys/DeleteSurveyModal'
import { StudyEnvironmentSurvey } from '@juniper/ui-core'

/** renders the main configuration page for a study environment */
function StudyContent({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { currentEnv } = studyEnvContext
  const contentHeaderStyle = {
    paddingTop: '1em',
    borderBottom: '1px solid #f6f6f6'
  }

  const preEnrollSurvey = currentEnv.preEnrollSurvey
  const isReadOnlyEnv = !(currentEnv.environmentName === 'sandbox')
  const [showCreateSurveyModal, setShowCreateSurveyModal] = useState(false)
  const [showArchiveSurveyModal, setShowArchiveSurveyModal] = useState(false)
  const [showDeleteSurveyModal, setShowDeleteSurveyModal] = useState(false)
  const [selectedSurveyConfig, setSelectedSurveyConfig] = useState<StudyEnvironmentSurvey>()

  currentEnv.configuredSurveys
    .sort((a, b) => a.surveyOrder - b.surveyOrder)
  currentEnv.configuredConsents
    .sort((a, b) => a.consentOrder - b.consentOrder)

  return <div className="container-fluid px-4 py-2">
    <div className="d-flex mb-2">
      <h2 className="fw-bold">Forms & Surveys</h2>
    </div>
    <div className="col-12">
      { currentEnv.studyEnvironmentConfig.initialized && <ul className="list-unstyled">
        <li className="mb-3 bg-white">
          <div style={contentHeaderStyle}>
            <h6>Pre-enrollment questionnaire</h6>
          </div>
          <div className="flex-grow-1 pt-3">
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
          <div className="flex-grow-1 pt-3">
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
          <div className="flex-grow-1 pt-3">
            <ul className="list-unstyled">
              { currentEnv.configuredSurveys.map((surveyConfig, index) => {
                const survey = surveyConfig.survey
                return (
                  <li className="p-1 d-flex align-items-center" key={index}>
                    <div className="d-flex align-items-center">
                      <Link to={`surveys/${survey.stableId}?readOnly=${isReadOnlyEnv}`}>
                        {survey.name} <span className="detail">v{survey.version}</span>
                      </Link>
                    </div>
                    { !isReadOnlyEnv && <div className="nav-item dropdown ms-1">
                      <a className="nav-link" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                        <FontAwesomeIcon icon={faEllipsisH} title="configure survey menu"/>
                      </a>
                      <div className="dropdown-menu">
                        <ul className="list-unstyled">
                          <li>
                            <button className="dropdown-item"
                              onClick={() => {
                                setShowArchiveSurveyModal(!showArchiveSurveyModal)
                                setSelectedSurveyConfig(surveyConfig)
                              }}>
                              Archive
                            </button>
                          </li>
                          <li className="pt-2">
                            <button className="dropdown-item"
                              onClick={() => {
                                setShowDeleteSurveyModal(!showDeleteSurveyModal)
                                setSelectedSurveyConfig(surveyConfig)
                              }}>
                              Delete
                            </button>
                          </li>
                        </ul>
                      </div>
                    </div> }
                  </li>
                )
              })}
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
      { selectedSurveyConfig && <ArchiveSurveyModal studyEnvContext={studyEnvContext}
        selectedSurveyConfig={selectedSurveyConfig}
        show={showArchiveSurveyModal}
        setShow={setShowArchiveSurveyModal}/> }
      { selectedSurveyConfig && <DeleteSurveyModal studyEnvContext={studyEnvContext}
        selectedSurveyConfig={selectedSurveyConfig}
        show={showDeleteSurveyModal}
        setShow={setShowDeleteSurveyModal}/> }
      { !currentEnv.studyEnvironmentConfig.initialized && <div>Not yet initialized</div> }
    </div>
  </div>
}

export default StudyContent
