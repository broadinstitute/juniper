import React, { useState } from 'react'

import { StudyEnvContextT } from './StudyEnvironmentRouter'
import { Link } from 'react-router-dom'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus'
import CreateSurveyModal from './surveys/CreateSurveyModal'
import { faEllipsisH } from '@fortawesome/free-solid-svg-icons'
import ArchiveSurveyModal from './surveys/ArchiveSurveyModal'
import DeleteSurveyModal from './surveys/DeleteSurveyModal'
import { StudyEnvironmentSurvey, Survey, SurveyType } from '@juniper/ui-core'
import CreateConsentModal from './consents/CreateConsentModal'
import { Button, IconButton } from 'components/forms/Button'
import CreatePreEnrollSurveyModal from './surveys/CreatePreEnrollSurveyModal'
import { renderPageHeader } from 'util/pageUtils'

/** renders the main configuration page for a study environment */
function StudyContent({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { currentEnv } = studyEnvContext
  const preEnrollSurvey = currentEnv.preEnrollSurvey
  const isReadOnlyEnv = !(currentEnv.environmentName === 'sandbox')
  const [showCreateSurveyModal, setShowCreateSurveyModal] = useState(false)
  const [showCreateConsentModal, setShowCreateConsentModal] = useState(false)
  const [showArchiveSurveyModal, setShowArchiveSurveyModal] = useState(false)
  const [showDeleteSurveyModal, setShowDeleteSurveyModal] = useState(false)
  const [showCreatePreEnrollSurveyModal, setShowCreatePreEnrollModal] = useState(false)
  const [selectedSurveyConfig, setSelectedSurveyConfig] = useState<StudyEnvironmentSurvey>()
  const [createSurveyType, setCreateSurveyType] = useState<SurveyType>('RESEARCH')
  const configuredResearchSurveys =  currentEnv.configuredSurveys
    .filter(configSurvey => configSurvey.survey.surveyType === 'RESEARCH')
    .sort((a, b) => a.surveyOrder - b.surveyOrder)
  const configuredOutreachSurveys =  currentEnv.configuredSurveys
    .filter(configSurvey => configSurvey.survey.surveyType === 'OUTREACH')
    .sort((a, b) => a.surveyOrder - b.surveyOrder)
  currentEnv.configuredConsents
    .sort((a, b) => a.consentOrder - b.consentOrder)

  return <div className="container-fluid px-4 py-2">
    { renderPageHeader('Forms & Surveys') }
    <div className="col-12">
      { currentEnv.studyEnvironmentConfig.initialized && <ul className="list-unstyled">
        <li className="mb-3 rounded-2 p-3" style={{ background: '#efefef' }}>
          <h6>Pre-enrollment questionnaire</h6>
          <div className="flex-grow-1 pt-3">
            { preEnrollSurvey && <ul className="list-unstyled">
              <li className="d-flex align-items-center">
                <Link to={`preEnroll/${preEnrollSurvey.stableId}?readOnly=${isReadOnlyEnv}`}>
                  {preEnrollSurvey.name} <span className="detail">v{preEnrollSurvey.version}</span>
                </Link>

                { !isReadOnlyEnv && <div className="nav-item dropdown ms-1">
                  <IconButton icon={faEllipsisH}  data-bs-toggle="dropdown"
                    aria-expanded="false" aria-label="configure pre-enroll menu"/>
                  <div className="dropdown-menu">
                    <ul className="list-unstyled">
                      <li>
                        <button className="dropdown-item"
                          onClick={() => alert('To remove a pre-enroll survey, contact support')}>
                          Remove
                        </button>
                      </li>
                    </ul>
                  </div>
                </div> }
              </li>
            </ul>}
            { (!preEnrollSurvey && !isReadOnlyEnv) && <Button variant="secondary"
              data-testid={'addPreEnroll'}
              onClick={() => {
                setShowCreatePreEnrollModal(!showCreatePreEnrollSurveyModal)
              }}>
              <FontAwesomeIcon icon={faPlus}/> Add
            </Button>
            }
          </div>
        </li>
        <li className="mb-3 rounded-2 p-3" style={{ background: '#efefef' }}>
          <h2 className="h6">Consent forms</h2>
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
              <li>
                { !isReadOnlyEnv && <button className="btn btn-secondary" data-testid={'addConsent'} onClick={() => {
                  setShowCreateConsentModal(!showCreateConsentModal)
                }}>
                  <FontAwesomeIcon icon={faPlus}/> Add
                </button> }
              </li>
            </ul>
          </div>
        </li>
        <li className="mb-3 rounded-2 p-3" style={{ background: '#efefef' }}>
          <h6>Research Surveys</h6>
          <div className="flex-grow-1 pt-3">
            <ul className="list-unstyled">
              { configuredResearchSurveys.map((surveyConfig, index) => {
                const survey = surveyConfig.survey
                return <SurveyListItem key={index} survey={survey} surveyConfig={surveyConfig}
                  setSelectedSurveyConfig={setSelectedSurveyConfig}
                  setShowDeleteSurveyModal={setShowDeleteSurveyModal}
                  setShowArchiveSurveyModal={setShowArchiveSurveyModal}
                  showArchiveSurveyModal={showArchiveSurveyModal}
                  showDeleteSurveyModal={showDeleteSurveyModal}
                  isReadOnlyEnv={isReadOnlyEnv}/>
              })}
              {!isReadOnlyEnv && <li>
                <Button variant="secondary" data-testid={'addResearchSurvey'} onClick={() => {
                  setShowCreateSurveyModal(!showCreateSurveyModal)
                  setCreateSurveyType('RESEARCH')
                }}>
                  <FontAwesomeIcon icon={faPlus}/> Add
                </Button>
              </li> }
            </ul>
          </div>
        </li>
        <li className="mb-3 rounded-2 p-3" style={{ background: '#efefef' }}>
          <h6>Outreach</h6>
          <div className="flex-grow-1 pt-3">
            <ul className="list-unstyled">
              { configuredOutreachSurveys.map((surveyConfig, index) => {
                const survey = surveyConfig.survey
                return <SurveyListItem key={index} survey={survey} surveyConfig={surveyConfig}
                  setSelectedSurveyConfig={setSelectedSurveyConfig}
                  setShowDeleteSurveyModal={setShowDeleteSurveyModal}
                  setShowArchiveSurveyModal={setShowArchiveSurveyModal}
                  showArchiveSurveyModal={showArchiveSurveyModal}
                  showDeleteSurveyModal={showDeleteSurveyModal}
                  isReadOnlyEnv={isReadOnlyEnv}
                />
              })}
              {!isReadOnlyEnv && <li>
                <Button variant="secondary" data-testid={'addOutreachSurvey'} onClick={() => {
                  setShowCreateSurveyModal(!showCreateSurveyModal)
                  setCreateSurveyType('OUTREACH')
                }}>
                  <FontAwesomeIcon icon={faPlus}/> Add
                </Button>
              </li> }
            </ul>
          </div>
        </li>
      </ul> }
      { showCreateSurveyModal && <CreateSurveyModal studyEnvContext={studyEnvContext} type={createSurveyType}
        onDismiss={() => setShowCreateSurveyModal(false)}/> }
      { (showArchiveSurveyModal && selectedSurveyConfig) && <ArchiveSurveyModal studyEnvContext={studyEnvContext}
        selectedSurveyConfig={selectedSurveyConfig}
        onDismiss={() => setShowArchiveSurveyModal(false)}/> }
      { (showDeleteSurveyModal && selectedSurveyConfig) && <DeleteSurveyModal studyEnvContext={studyEnvContext}
        selectedSurveyConfig={selectedSurveyConfig}
        onDismiss={() => setShowDeleteSurveyModal(false)}/> }
      { showCreateConsentModal && <CreateConsentModal studyEnvContext={studyEnvContext}
        onDismiss={() => setShowCreateConsentModal(false)}/>}
      { showCreatePreEnrollSurveyModal && <CreatePreEnrollSurveyModal studyEnvContext={studyEnvContext}
        onDismiss={() => setShowCreatePreEnrollModal(false)}/> }
      { !currentEnv.studyEnvironmentConfig.initialized && <div>Not yet initialized</div> }
    </div>
  </div>
}

type SurveyListItemProps = {
    survey: Survey,
    surveyConfig: StudyEnvironmentSurvey,
    isReadOnlyEnv: boolean,
    setSelectedSurveyConfig: (config: StudyEnvironmentSurvey) => void,
  showDeleteSurveyModal: boolean,
    setShowDeleteSurveyModal: (show: boolean) => void,
  showArchiveSurveyModal: boolean,
    setShowArchiveSurveyModal: (show: boolean) => void,
}

const SurveyListItem = (props: SurveyListItemProps) => {
  const {
    survey, surveyConfig, isReadOnlyEnv,
    setSelectedSurveyConfig, setShowDeleteSurveyModal, setShowArchiveSurveyModal,
    showDeleteSurveyModal, showArchiveSurveyModal
  } = props
  return <li className="p-1 d-flex align-items-center">
    <div className="d-flex align-items-center">
      <Link to={`surveys/${survey.stableId}?readOnly=${isReadOnlyEnv}`}>
        {survey.name}
        <span className="mx-1 detail">v{survey.version}</span>
        {survey.required && <span className="detail">(required)</span>}
      </Link>
    </div>
    { !isReadOnlyEnv && <div className="nav-item dropdown ms-1">
      <IconButton icon={faEllipsisH}  data-bs-toggle="dropdown"
        aria-expanded="false" aria-label="configure survey menu"/>
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
}

export default StudyContent
