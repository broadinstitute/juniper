import React, { useContext, useState } from 'react'

import { paramsFromContext, StudyEnvContextT } from './StudyEnvironmentRouter'
import { Link } from 'react-router-dom'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus'
import CreateSurveyModal from './surveys/CreateSurveyModal'
import { faEllipsisH } from '@fortawesome/free-solid-svg-icons'
import ArchiveSurveyModal from './surveys/ArchiveSurveyModal'
import DeleteSurveyModal from './surveys/DeleteSurveyModal'
import { StudyEnvironmentSurvey, StudyEnvironmentSurveyNamed, SurveyType } from '@juniper/ui-core'
import CreateConsentModal from './consents/CreateConsentModal'
import { Button, IconButton } from 'components/forms/Button'
import CreatePreEnrollSurveyModal from './surveys/CreatePreEnrollSurveyModal'
import { renderPageHeader } from 'util/pageUtils'

import Api from 'api/api'
import { PortalContext, PortalContextT } from 'portal/PortalProvider'
import LoadingSpinner from 'util/LoadingSpinner'
import { doApiLoad, useLoadingEffect } from '../api/api-utils'
import _uniq from 'lodash/uniq'
import SurveyEnvironmentTable from './surveys/SurveyEnvironmentTable'


/** renders the main configuration page for a study environment */
function StudyContent({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { currentEnv } = studyEnvContext
  const portalContext = useContext(PortalContext) as PortalContextT


  const preEnrollSurvey = currentEnv.preEnrollSurvey
  const isReadOnlyEnv = !(currentEnv.environmentName === 'sandbox')
  const [configuredSurveys, setConfiguredSurveys] = useState<StudyEnvironmentSurveyNamed[]>([])
  const [showCreateConsentModal, setShowCreateConsentModal] = useState(false)
  const [showArchiveSurveyModal, setShowArchiveSurveyModal] = useState(false)
  const [showDeleteSurveyModal, setShowDeleteSurveyModal] = useState(false)
  const [showCreatePreEnrollSurveyModal, setShowCreatePreEnrollModal] = useState(false)
  const [selectedSurveyConfig, setSelectedSurveyConfig] = useState<StudyEnvironmentSurvey>()
  const [createSurveyType, setCreateSurveyType] = useState<SurveyType>()

  currentEnv.configuredConsents
    .sort((a, b) => a.consentOrder - b.consentOrder)

  const { isLoading, setIsLoading } = useLoadingEffect(async () => {
    const response = await Api.findConfiguredSurveys(
      studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode, undefined, true, undefined)
    setConfiguredSurveys(response.map(config => ({
      ...config,
      envName: studyEnvContext.study.studyEnvironments
        .find(env => env.id === config.studyEnvironmentId)!.environmentName
    })))
  })
  const updateConfiguredSurvey = async (surveyConfig: StudyEnvironmentSurvey) => {
    doApiLoad(async () => {
      await Api.updateConfiguredSurvey(studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode, currentEnv.environmentName, surveyConfig)
      await portalContext.reloadPortal(studyEnvContext.portal.shortcode)
    }, { setIsLoading })
  }

  const researchSurveyStableIds =  _uniq(configuredSurveys
    .filter(configSurvey => configSurvey.survey.surveyType === 'RESEARCH')
    .sort((a, b) => a.surveyOrder - b.surveyOrder)
    .map(configSurvey => configSurvey.survey.stableId))
  const outreachSurveyStableIds =  _uniq(configuredSurveys
    .filter(configSurvey => configSurvey.survey.surveyType === 'OUTREACH')
    .sort((a, b) => a.surveyOrder - b.surveyOrder)
    .map(configSurvey => configSurvey.survey.stableId))
  const consentSurveyStableIds =  _uniq(configuredSurveys
    .filter(configSurvey => configSurvey.survey.surveyType === 'CONSENT')
    .sort((a, b) => a.surveyOrder - b.surveyOrder)
    .map(configSurvey => configSurvey.survey.stableId))

  return <div className="container-fluid px-4 py-2">
    { renderPageHeader('Forms & Surveys') }
    <LoadingSpinner isLoading={isLoading}>
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
              </ul>
            </div>
            <div className="flex-grow-1 pt-3">
              <SurveyEnvironmentTable stableIds={consentSurveyStableIds}
                studyEnvParams={paramsFromContext(studyEnvContext)}
                configuredSurveys={configuredSurveys}
                setSelectedSurveyConfig={setSelectedSurveyConfig}
                updateConfiguredSurvey={updateConfiguredSurvey}
                setShowDeleteSurveyModal={setShowDeleteSurveyModal}
                setShowArchiveSurveyModal={setShowArchiveSurveyModal}
                showArchiveSurveyModal={showArchiveSurveyModal}
                showDeleteSurveyModal={showDeleteSurveyModal}
              />
              <div>
                <Button variant="secondary" data-testid={'addConsentSurvey'} onClick={() => {
                  setCreateSurveyType('CONSENT')
                }}>
                  <FontAwesomeIcon icon={faPlus}/> Add
                </Button>
              </div>
            </div>
          </li>
          <li className="mb-3 rounded-2 p-3" style={{ background: '#efefef' }}>
            <h6>Research Surveys</h6>
            <div className="flex-grow-1 pt-3">
              <SurveyEnvironmentTable stableIds={researchSurveyStableIds}
                studyEnvParams={paramsFromContext(studyEnvContext)}
                configuredSurveys={configuredSurveys}
                setSelectedSurveyConfig={setSelectedSurveyConfig}
                updateConfiguredSurvey={updateConfiguredSurvey}
                setShowDeleteSurveyModal={setShowDeleteSurveyModal}
                setShowArchiveSurveyModal={setShowArchiveSurveyModal}
                showArchiveSurveyModal={showArchiveSurveyModal}
                showDeleteSurveyModal={showDeleteSurveyModal}
              />
              <div>
                <Button variant="secondary" data-testid={'addResearchSurvey'} onClick={() => {
                  setCreateSurveyType('RESEARCH')
                }}>
                  <FontAwesomeIcon icon={faPlus}/> Add
                </Button>
              </div>
            </div>
          </li>
          <li className="mb-3 rounded-2 p-3" style={{ background: '#efefef' }}>
            <h6>Outreach</h6>
            <div className="flex-grow-1 pt-3">
              <SurveyEnvironmentTable stableIds={outreachSurveyStableIds}
                studyEnvParams={paramsFromContext(studyEnvContext)}
                configuredSurveys={configuredSurveys}
                setSelectedSurveyConfig={setSelectedSurveyConfig}
                updateConfiguredSurvey={updateConfiguredSurvey}
                setShowDeleteSurveyModal={setShowDeleteSurveyModal}
                setShowArchiveSurveyModal={setShowArchiveSurveyModal}
                showArchiveSurveyModal={showArchiveSurveyModal}
                showDeleteSurveyModal={showDeleteSurveyModal}
              />
              <div>
                <Button variant="secondary" data-testid={'addOutreachSurvey'} onClick={() => {
                  setCreateSurveyType('OUTREACH')
                }}>
                  <FontAwesomeIcon icon={faPlus}/> Add
                </Button>
              </div>
            </div>
          </li>
        </ul> }
        { createSurveyType && <CreateSurveyModal studyEnvContext={studyEnvContext} type={createSurveyType}
          onDismiss={() => setCreateSurveyType(undefined)}/> }
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
    </LoadingSpinner>
  </div>
}


export default StudyContent
