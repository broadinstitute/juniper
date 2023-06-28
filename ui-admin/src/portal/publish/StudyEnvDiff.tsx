import React from 'react'
import { StudyEnvironmentChange } from 'api/api'
import {
  ConfigChangeListView,
  ConfigChanges, renderNotificationConfig,
  renderStudyEnvironmentConsent,
  renderStudyEnvironmentSurvey,
  VersionChangeView
} from './diffComponents'

type StudyEnvDiffProps = {
  studyEnvChange: StudyEnvironmentChange,
  selectedChanges: StudyEnvironmentChange,
  setSelectedChanges: (update: StudyEnvironmentChange) => void
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
const StudyEnvDiff = ({ studyEnvChange, selectedChanges, setSelectedChanges }: StudyEnvDiffProps) => {
  return <div className="px-3">
    <div className="my-1">
      <h3 className="h6">
        Environment config</h3>
      <ConfigChanges configChanges={studyEnvChange.configChanges}
                     selectedChanges={selectedChanges.configChanges}
                     updateSelectedChanges={configChanges => setSelectedChanges({
                       ...studyEnvChange,
                       configChanges
                     })}/>
    </div>
    <div className="my-1">
      <h3 className="h6"><input className="me-2" type={'checkbox'} checked={true} readOnly={true}/>
        PreEnroll survey</h3>
      <VersionChangeView record={studyEnvChange.preEnrollSurveyChanges}/>
    </div>
    <div className="my-1">
      <h3 className="h6"><input className="me-2" type={'checkbox'} checked={true} readOnly={true}/>
        Consents</h3>
      <ConfigChangeListView configChangeList={studyEnvChange.consentChanges}
        renderItemSummary={renderStudyEnvironmentConsent}/>
    </div>
    <div className="my-1">
      <h3 className="h6"><input className="me-2" type={'checkbox'} checked={true} readOnly={true}/>
        Surveys</h3>
      <ConfigChangeListView configChangeList={studyEnvChange.surveyChanges}
        renderItemSummary={renderStudyEnvironmentSurvey}/>
    </div>
    <div className="my-1">
      <h3 className="h6"><input className="me-2" type={'checkbox'} checked={true} readOnly={true}/>
        Notification Configs</h3>
      <ConfigChangeListView configChangeList={studyEnvChange.notificationConfigChanges}
        renderItemSummary={renderNotificationConfig}/>
    </div>
  </div>
}

export default StudyEnvDiff
