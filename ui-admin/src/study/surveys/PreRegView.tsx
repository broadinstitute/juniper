import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Store } from 'react-notifications-component'

import {
  StudyEnvContextT,
  studyEnvSiteContentPath
} from 'study/StudyEnvironmentRouter'
import Api, { Survey } from 'api/api'

import { successNotification } from 'util/notifications'
import SurveyEditorView from './SurveyEditorView'
import LoadingSpinner from 'util/LoadingSpinner'
import {
  SaveableFormProps,
  useLoadedSurvey,
  useSurveyParams
} from './SurveyView'
import { doApiLoad } from 'api/api-utils'
import { PortalEnvContext } from 'portal/PortalRouter'
import {
  DocsKey,
  ZendeskLink
} from 'util/zendeskUtils'
import InfoPopup from 'components/forms/InfoPopup'

/** Preregistration editor.  This shares a LOT in common with SurveyView and PreEnrollView,
 * but it's expected they will diverge over
 * time as the mechanics for creating/editing pre-reg surveys are likely to be different */
function RawPreRegView({ studyEnvContext, portalEnvContext, survey, readOnly = false }:
                              {studyEnvContext: StudyEnvContextT, portalEnvContext: PortalEnvContext,
                                  survey: Survey, readOnly?: boolean}) {
  const { portal, portalEnv } = portalEnvContext
  const navigate = useNavigate()
  const [currentSurvey, setCurrentSurvey] = useState(survey)

  /** saves as a new version and updates the study environment accordingly */
  async function createNewVersion(changes: SaveableFormProps): Promise<void> {
    const newSurvey = { ...currentSurvey, ...changes }
    doApiLoad(async () => {
      const updatedSurvey = await Api.createNewSurveyVersion(portal.shortcode, newSurvey)
      Store.addNotification(successNotification(`Survey saved successfully`))
      setCurrentSurvey(updatedSurvey)
      const updatedEnv = { ...portalEnv, preRegSurveyId: updatedSurvey.id }
      await Api.updatePortalEnv(portal.shortcode,
        portalEnv.environmentName, updatedEnv)
      Store.addNotification(successNotification(`Environment updated`))
      portalEnvContext.reloadPortal(portal.shortcode)
    })
  }

  return <>
    <div className="d-flex align-items-center">
      <h2 className="h3 ms-2">Pre-registration survey </h2>
      <InfoPopup placement="bottom" content={<span>
        Pre-registration surveys are shown to participants before they create a user account on the portal.
        <ZendeskLink doc={DocsKey.PREREG_SURVEYS} className="ms-4">More documentation.</ZendeskLink>
      </span>}/>

    </div>

    <SurveyEditorView
      studyEnvContext={studyEnvContext}
      currentForm={currentSurvey}
      readOnly={readOnly}
      onCancel={() => navigate(studyEnvSiteContentPath(portal.shortcode,
        studyEnvContext.study.shortcode, portalEnv.environmentName))}
      onSave={createNewVersion}
    />
  </>
}

/** routable component for survey editing */
function PreRegView({ portalEnvContext, studyEnvContext }: {portalEnvContext: PortalEnvContext,
studyEnvContext: StudyEnvContextT}) {
  const { isReadOnly } = useSurveyParams()
  const { portalEnv, portal } = portalEnvContext
  const appliedReadOnly = isReadOnly || portalEnv.environmentName !== 'sandbox'

  const envSurvey = portalEnv.preRegSurvey
  if (!envSurvey) {
    return <div>
            No pre-registration survey is configured for this portal and environment.
    </div>
  }
  const { isLoading, survey } = useLoadedSurvey(portal.shortcode, envSurvey.stableId, envSurvey.version)

  return <>
    { isLoading && <LoadingSpinner/> }
    { !isLoading && <RawPreRegView portalEnvContext={portalEnvContext} studyEnvContext={studyEnvContext}
      survey={survey!} readOnly={appliedReadOnly}/> }
  </>
}

export default PreRegView
