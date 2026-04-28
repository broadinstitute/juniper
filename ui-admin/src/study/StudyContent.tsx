import React, {
  useContext,
  useState
} from 'react'

import {
  paramsFromContext,
  StudyEnvContextT
} from './StudyEnvironmentRouter'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons/faPlus'
import CreateSurveyModal from './surveys/CreateSurveyModal'
import DeleteSurveyModal from './surveys/DeleteSurveyModal'
import DeactivateSurveyModal from './surveys/DeactivateSurveyModal'
import CancelSurveyTasksModal from './surveys/CancelSurveyTasksModal'
import ActivateSurveyModal from './surveys/ActivateSurveyModal'
import {
  StudyEnvironmentSurvey,
  StudyEnvironmentSurveyNamed,
  SurveyType
} from '@juniper/ui-core'
import {
  Button
} from 'components/forms/Button'
import CreatePreEnrollSurveyModal from './surveys/CreatePreEnrollSurveyModal'
import { renderPageHeader } from 'util/pageUtils'

import Api from 'api/api'
import {
  PortalContext,
  PortalContextT
} from 'portal/PortalProvider'
import LoadingSpinner from 'util/LoadingSpinner'
import {
  doApiLoad,
  useLoadingEffect
} from '../api/api-utils'
import _uniq from 'lodash/uniq'
import SurveyEnvironmentTable from './surveys/SurveyEnvironmentTable'
import { RequireUserPermission } from 'util/RequireUserPermission'


/** renders the main configuration page for a study environment */
function StudyContent({ studyEnvContext }: {studyEnvContext: StudyEnvContextT}) {
  const { currentEnv } = studyEnvContext
  const portalContext = useContext(PortalContext) as PortalContextT

  const [configuredSurveys, setConfiguredSurveys] = useState<StudyEnvironmentSurveyNamed[]>([])
  const [showDeleteSurveyModal, setShowDeleteSurveyModal] = useState(false)
  const [showDeactivateSurveyModal, setShowDeactivateSurveyModal] = useState(false)
  const [showCancelTasksSurveyModal, setShowCancelTasksSurveyModal] = useState(false)
  const [showActivateSurveyModal, setShowActivateSurveyModal] = useState(false)
  const [showCreatePreEnrollSurveyModal, setShowCreatePreEnrollModal] = useState(false)
  const [selectedSurveyConfig, setSelectedSurveyConfig] = useState<StudyEnvironmentSurveyNamed>()
  const [createSurveyType, setCreateSurveyType] = useState<SurveyType>()

  const { isLoading, setIsLoading } = useLoadingEffect(async () => {
    const response = await Api.findConfiguredSurveys(
      studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode, undefined, undefined, undefined)
    setConfiguredSurveys(response.map(config => ({
      ...config,
      envName: studyEnvContext.study.studyEnvironments
        .find(env => env.id === config.studyEnvironmentId)!.environmentName
    })))
  })
  const updateConfiguredSurveys = async (surveyConfigs: StudyEnvironmentSurvey[]) => {
    doApiLoad(async () => {
      await Api.updateConfiguredSurveys(studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode, currentEnv.environmentName, surveyConfigs)
      await portalContext.reloadPortal(studyEnvContext.portal.shortcode)
    }, { setIsLoading })
  }

  function getUniqueStableIdsForType(configuredSurveys: StudyEnvironmentSurveyNamed[], surveyType: string) {
    return _uniq(configuredSurveys
      .filter(configSurvey => configSurvey.survey.surveyType === surveyType)
      .sort((a, b) => (b.active ? 1 : 0) - (a.active ? 1 : 0) || a.surveyOrder - b.surveyOrder)
      .map(configSurvey => configSurvey.survey.stableId))
  }

  const researchSurveyStableIds = getUniqueStableIdsForType(configuredSurveys, 'RESEARCH')
  const outreachSurveyStableIds = getUniqueStableIdsForType(configuredSurveys, 'OUTREACH')
  const consentSurveyStableIds = getUniqueStableIdsForType(configuredSurveys, 'CONSENT')
  const adminFormStableIds = getUniqueStableIdsForType(configuredSurveys, 'ADMIN')
  const documnetRequestStableIds = getUniqueStableIdsForType(configuredSurveys, 'DOCUMENT_REQUEST')
  const preEnrollStableIds = getUniqueStableIdsForType(configuredSurveys, 'PRE_ENROLL')

  return <div className="container-fluid px-4 py-2">
    { renderPageHeader('Forms & Surveys') }
    <LoadingSpinner isLoading={isLoading}>
      <div className="col-12">
        { currentEnv.studyEnvironmentConfig.initialized && <ul className="list-unstyled">
          <li className="mb-3 rounded-2 p-3" style={{ background: '#efefef' }}>
            <h2 className="h6">Pre-Enroll Surveys</h2>
            <div className="flex-grow-1 pt-3">
              <SurveyEnvironmentTable
                key={studyEnvContext.currentEnvPath}
                stableIds={preEnrollStableIds}
                studyEnvParams={paramsFromContext(studyEnvContext)}
                configuredSurveys={configuredSurveys}
                setSelectedSurveyConfig={setSelectedSurveyConfig}
                updateConfiguredSurveys={updateConfiguredSurveys}
                setShowDeleteSurveyModal={setShowDeleteSurveyModal}
                showDeleteSurveyModal={showDeleteSurveyModal}
                showDeactivateSurveyModal={showDeactivateSurveyModal}
                setShowDeactivateSurveyModal={setShowDeactivateSurveyModal}
                showCancelTasksSurveyModal={showCancelTasksSurveyModal}
                setShowCancelTasksSurveyModal={setShowCancelTasksSurveyModal}
                showActivateSurveyModal={showActivateSurveyModal}
                setShowActivateSurveyModal={setShowActivateSurveyModal}
              />
              <div>
                <Button variant="secondary" data-testid={'addPreenrollSurvey'} onClick={() => {
                  setCreateSurveyType('PRE_ENROLL')
                }}>
                  <FontAwesomeIcon icon={faPlus}/> Add
                </Button>
              </div>
            </div>
          </li>
          <li className="mb-3 rounded-2 p-3" style={{ background: '#efefef' }}>
            <h2 className="h6">Consent Forms</h2>
            <div className="flex-grow-1 pt-3">
              <SurveyEnvironmentTable
                key={studyEnvContext.currentEnvPath}
                stableIds={consentSurveyStableIds}
                studyEnvParams={paramsFromContext(studyEnvContext)}
                configuredSurveys={configuredSurveys}
                setSelectedSurveyConfig={setSelectedSurveyConfig}
                updateConfiguredSurveys={updateConfiguredSurveys}
                setShowDeleteSurveyModal={setShowDeleteSurveyModal}
                showDeleteSurveyModal={showDeleteSurveyModal}
                showDeactivateSurveyModal={showDeactivateSurveyModal}
                setShowDeactivateSurveyModal={setShowDeactivateSurveyModal}
                showCancelTasksSurveyModal={showCancelTasksSurveyModal}
                setShowCancelTasksSurveyModal={setShowCancelTasksSurveyModal}
                showActivateSurveyModal={showActivateSurveyModal}
                setShowActivateSurveyModal={setShowActivateSurveyModal}
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
              <SurveyEnvironmentTable
                key={studyEnvContext.currentEnvPath}
                stableIds={researchSurveyStableIds}
                studyEnvParams={paramsFromContext(studyEnvContext)}
                configuredSurveys={configuredSurveys}
                setSelectedSurveyConfig={setSelectedSurveyConfig}
                updateConfiguredSurveys={updateConfiguredSurveys}
                setShowDeleteSurveyModal={setShowDeleteSurveyModal}
                showDeleteSurveyModal={showDeleteSurveyModal}
                showDeactivateSurveyModal={showDeactivateSurveyModal}
                setShowDeactivateSurveyModal={setShowDeactivateSurveyModal}
                showCancelTasksSurveyModal={showCancelTasksSurveyModal}
                setShowCancelTasksSurveyModal={setShowCancelTasksSurveyModal}
                showActivateSurveyModal={showActivateSurveyModal}
                setShowActivateSurveyModal={setShowActivateSurveyModal}
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
            <h6>Study Staff Forms</h6>
            <div className="flex-grow-1 pt-3">
              <SurveyEnvironmentTable
                key={studyEnvContext.currentEnvPath}
                stableIds={adminFormStableIds}
                studyEnvParams={paramsFromContext(studyEnvContext)}
                configuredSurveys={configuredSurveys}
                setSelectedSurveyConfig={setSelectedSurveyConfig}
                updateConfiguredSurveys={updateConfiguredSurveys}
                setShowDeleteSurveyModal={setShowDeleteSurveyModal}
                showDeleteSurveyModal={showDeleteSurveyModal}
                showDeactivateSurveyModal={showDeactivateSurveyModal}
                setShowDeactivateSurveyModal={setShowDeactivateSurveyModal}
                showCancelTasksSurveyModal={showCancelTasksSurveyModal}
                setShowCancelTasksSurveyModal={setShowCancelTasksSurveyModal}
                showActivateSurveyModal={showActivateSurveyModal}
                setShowActivateSurveyModal={setShowActivateSurveyModal}
              />
              <div>
                <Button variant="secondary" data-testid={'addAdminForm'} onClick={() => {
                  setCreateSurveyType('ADMIN')
                }}>
                  <FontAwesomeIcon icon={faPlus}/> Add
                </Button>
              </div>
            </div>
          </li>
          <RequireUserPermission superuser>
            <li className="mb-3 rounded-2 p-3" style={{ background: '#efefef' }}>
              <h6>Document Requests</h6>
              <div className="flex-grow-1 pt-3">
                <SurveyEnvironmentTable
                  key={studyEnvContext.currentEnvPath}
                  stableIds={documnetRequestStableIds}
                  studyEnvParams={paramsFromContext(studyEnvContext)}
                  configuredSurveys={configuredSurveys}
                  setSelectedSurveyConfig={setSelectedSurveyConfig}
                  updateConfiguredSurveys={updateConfiguredSurveys}
                  setShowDeleteSurveyModal={setShowDeleteSurveyModal}
                  showDeleteSurveyModal={showDeleteSurveyModal}
                  showDeactivateSurveyModal={showDeactivateSurveyModal}
                  setShowDeactivateSurveyModal={setShowDeactivateSurveyModal}
                  showCancelTasksSurveyModal={showCancelTasksSurveyModal}
                  setShowCancelTasksSurveyModal={setShowCancelTasksSurveyModal}
                  showActivateSurveyModal={showActivateSurveyModal}
                  setShowActivateSurveyModal={setShowActivateSurveyModal}
                />
                <div>
                  <Button variant="secondary" data-testid={'addDocumentRequest'} onClick={() => {
                    setCreateSurveyType('DOCUMENT_REQUEST')
                  }}>
                    <FontAwesomeIcon icon={faPlus}/> Add
                  </Button>
                </div>
              </div>
            </li>
          </RequireUserPermission>
          <li className="mb-3 rounded-2 p-3" style={{ background: '#efefef' }}>
            <h6>Outreach</h6>
            <div className="flex-grow-1 pt-3">
              <SurveyEnvironmentTable
                key={studyEnvContext.currentEnvPath}
                stableIds={outreachSurveyStableIds}
                studyEnvParams={paramsFromContext(studyEnvContext)}
                configuredSurveys={configuredSurveys}
                setSelectedSurveyConfig={setSelectedSurveyConfig}
                updateConfiguredSurveys={updateConfiguredSurveys}
                setShowDeleteSurveyModal={setShowDeleteSurveyModal}
                showDeleteSurveyModal={showDeleteSurveyModal}
                showDeactivateSurveyModal={showDeactivateSurveyModal}
                setShowDeactivateSurveyModal={setShowDeactivateSurveyModal}
                showCancelTasksSurveyModal={showCancelTasksSurveyModal}
                setShowCancelTasksSurveyModal={setShowCancelTasksSurveyModal}
                showActivateSurveyModal={showActivateSurveyModal}
                setShowActivateSurveyModal={setShowActivateSurveyModal}
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
        </ul>}
        {createSurveyType && <CreateSurveyModal studyEnvContext={studyEnvContext} type={createSurveyType}
          onDismiss={() => setCreateSurveyType(undefined)}/>}
        {(showDeleteSurveyModal && selectedSurveyConfig) && <DeleteSurveyModal studyEnvContext={studyEnvContext}
          selectedSurveyConfig={selectedSurveyConfig}
          onDismiss={() => setShowDeleteSurveyModal(false)}/>}
        {(showDeactivateSurveyModal && selectedSurveyConfig) && <DeactivateSurveyModal studyEnvContext={studyEnvContext}
          selectedSurveyConfig={selectedSurveyConfig}
          onDismiss={() => setShowDeactivateSurveyModal(false)}/>}
        {(showCancelTasksSurveyModal && selectedSurveyConfig) && <CancelSurveyTasksModal
          studyEnvContext={studyEnvContext}
          selectedSurveyConfig={selectedSurveyConfig}
          onDismiss={() => setShowCancelTasksSurveyModal(false)}/>}
        {(showActivateSurveyModal && selectedSurveyConfig) && <ActivateSurveyModal studyEnvContext={studyEnvContext}
          selectedSurveyConfig={selectedSurveyConfig}
          onDismiss={() => setShowActivateSurveyModal(false)}/>}
        {showCreatePreEnrollSurveyModal && <CreatePreEnrollSurveyModal studyEnvContext={studyEnvContext}
          onDismiss={() => setShowCreatePreEnrollModal(false)}/>}
        {!currentEnv.studyEnvironmentConfig.initialized && <div>Not yet initialized</div>}
      </div>
    </LoadingSpinner>
  </div>
}


export default StudyContent
