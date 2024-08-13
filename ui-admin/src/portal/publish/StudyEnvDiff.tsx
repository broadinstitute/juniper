import React from 'react'
import { StudyEnvironmentChange } from 'api/api'
import {
  ConfigChangeListView,
  ConfigChanges, renderKitType, renderNotificationConfig,
  renderStudyEnvironmentSurvey,
  VersionChangeView
} from './diffComponents'

type StudyEnvDiffProps = {
  studyName: string,
  studyEnvChange: StudyEnvironmentChange,
  selectedChanges: StudyEnvironmentChange,
  setSelectedChanges: (update: StudyEnvironmentChange) => void
}

const StudyEnvDiff = ({ studyName, studyEnvChange, selectedChanges, setSelectedChanges }: StudyEnvDiffProps) => {
  return <div className="px-3 my-2 py-2" style={{ backgroundColor: '#ededed' }}>
    <h2 className="h5 pb-2">{studyName}</h2>
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
        {studyEnvChange.preEnrollSurveyChanges.changed &&
            <input type="checkbox" className="me-3" checked={selectedChanges.preEnrollSurveyChanges.changed}
              onChange={e => setSelectedChanges({
                ...selectedChanges,
                preEnrollSurveyChanges: e.target.checked ? studyEnvChange.preEnrollSurveyChanges : { changed: false }
              })}/>}
        <VersionChangeView record={studyEnvChange.preEnrollSurveyChanges}/>
      </label>
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
        <ConfigChangeListView configChangeList={studyEnvChange.triggerChanges}
          selectedChanges={selectedChanges.triggerChanges}
          setSelectedChanges={triggerChanges =>
            setSelectedChanges({ ...selectedChanges, triggerChanges })}
          renderItemSummary={renderNotificationConfig}/>
      </div>
    </div>
    <div className="my-1">
      <h3 className="h6">Kit Types</h3>
      <div className="ms-4">
        <ConfigChangeListView configChangeList={studyEnvChange.kitTypeChanges}
          selectedChanges={selectedChanges.kitTypeChanges}
          setSelectedChanges={kitTypeChanges =>
            setSelectedChanges({ ...selectedChanges, kitTypeChanges })}
          renderItemSummary={renderKitType}/>
      </div>
    </div>
  </div>
}

export default StudyEnvDiff
