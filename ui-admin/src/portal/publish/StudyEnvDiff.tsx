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
  return <div className="px-3 mb-5">
    <h2 className="h5">{studyEnvChange.studyShortcode}</h2>
    <div className="my-1">
      <h3 className="h6">Environment config</h3>
      <div className="ms-4">
        <ConfigChanges configChanges={studyEnvChange.configChanges}
          selectedChanges={selectedChanges.configChanges}
          updateSelectedChanges={configChanges => setSelectedChanges({
            ...studyEnvChange,
            configChanges
          })}/>
      </div>
    </div>
    <div className="my-1">
      <h3 className="h6">
        PreEnroll survey</h3>
      <label className="d-flex ms-4">
        { studyEnvChange.preEnrollSurveyChanges.changed &&
          <input type="checkbox" className="me-3" checked={selectedChanges.preEnrollSurveyChanges.changed}
            onChange={e => setSelectedChanges({
              ...selectedChanges,
              preEnrollSurveyChanges: e.target.checked ? studyEnvChange.preEnrollSurveyChanges : { changed: false }
            })}/> }
        <VersionChangeView record={studyEnvChange.preEnrollSurveyChanges}/>
      </label>
    </div>
    <div className="my-1">
      <h3 className="h6">Consents</h3>
      <div className="ms-4">
        <ConfigChangeListView configChangeList={studyEnvChange.consentChanges}
          selectedChanges={selectedChanges.consentChanges}
          setSelectedChanges={consentChanges =>
            setSelectedChanges({ ...selectedChanges, consentChanges })}
          renderItemSummary={renderStudyEnvironmentConsent}/>
      </div>
    </div>
    <div className="my-1">
      <h3 className="h6">Surveys</h3>
      <div className="ms-4">
        <ConfigChangeListView configChangeList={studyEnvChange.surveyChanges}
          selectedChanges={selectedChanges.surveyChanges}
          setSelectedChanges={surveyChanges =>
            setSelectedChanges({ ...selectedChanges, surveyChanges })}
          renderItemSummary={renderStudyEnvironmentSurvey}/>
      </div>
    </div>
    <div className="my-1">
      <h3 className="h6">Notification Configs</h3>
      <div className="ms-4">
        <ConfigChangeListView configChangeList={studyEnvChange.notificationConfigChanges}
          selectedChanges={selectedChanges.notificationConfigChanges}
          setSelectedChanges={notificationConfigChanges =>
            setSelectedChanges({ ...selectedChanges, notificationConfigChanges })}
          renderItemSummary={renderNotificationConfig}/>
      </div>
    </div>
  </div>
}

export default StudyEnvDiff
