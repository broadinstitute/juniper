import {
  ConfigChange,
  ListChange,
  VersionedConfigChange,
  VersionedEntityChange
} from 'api/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowRight } from '@fortawesome/free-solid-svg-icons'
import React, { useId } from 'react'
import { NotificationConfig, StudyEnvironmentConsent, StudyEnvironmentSurvey } from '@juniper/ui-core/build/types/study'

/**
 * returns html for displaying the differences in versions.  this does not yet include support
 * for links to the versions
 */
export const VersionChangeView = ({ record }: {record: VersionedEntityChange}) => {
  if (!record.changed) {
    return <span className="fst-italic text-muted">no changes</span>
  }
  return <div>
    {versionDisplay(record.oldStableId, record.oldVersion)}
    <FontAwesomeIcon icon={faArrowRight} className="mx-2 fa-sm"/>
    {versionDisplay(record.newStableId, record.newVersion)}
  </div>
}

/** renders a list of config changes, or "no changes" if empty */
export const ConfigChanges = ({ configChanges }: {configChanges: ConfigChange[]}) => {
  if (!configChanges.length) {
    return <span className="fst-italic text-muted">no changes</span>
  }
  return <ul className="list-unstyled">
    {configChanges.map((configChange, index) => <li key={index}>
      <ConfigChangeView configChange={configChange}/>
    </li>)}
  </ul>
}

/** renders a config change by converting the old and new vals to strings */
export const ConfigChangeView = ({ configChange }: {configChange: ConfigChange}) => {
  const noVal = <span className="text-muted fst-italic">none</span>
  const oldVal = valuePresent(configChange.oldValue) ? configChange.oldValue.toString() : noVal
  const newVal = valuePresent(configChange.newValue) ? configChange.newValue.toString() : noVal
  const id = useId()
  return <div>
    <label htmlFor={`${id}-changes`}>{configChange.propertyName}:</label>
    <span id={`${id}-changes`} className="ms-3">{oldVal}
      <FontAwesomeIcon icon={faArrowRight} className="mx-2 fa-sm"/>
      {newVal}
    </span>
  </div>
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const valuePresent = (val: object) => {
  return val !== null && typeof val !== 'undefined'
}

/**
 * returns html for displaying a single version, and 'not present' if null
 */
export const versionDisplay = (stableId: string, version: number) => {
  if (!stableId) {
    return <span className="fst-italic text-muted">none</span>
  }
  return <span>{stableId} v{version}</span>
}

/** Summary of notification config changes -- doesn't show any detail yet */
export const ConfigChangeListView = <T, >({ configChangeList, changeItemSummaryFunc }:
                                      {configChangeList: ListChange<T, VersionedConfigChange>,
                                      changeItemSummaryFunc: (item: T) => React.ReactNode}) => {
  if (!configChangeList.addedItems.length &&
    !configChangeList.removedItems.length && !configChangeList.changedItems.length) {
    return <span className="fst-italic text-muted">no changes</span>
  }
  return <ul className="list-unstyled">
    {configChangeList.addedItems.length > 0 && <li className="ps-4">Added: {configChangeList.addedItems.length}
      <ul className="list-unstyled">
        {configChangeList.addedItems.map((item, index) => <li className="ps-4" key={index}>
          {changeItemSummaryFunc(item)}
        </li>)}
      </ul>
    </li>}
    {configChangeList.removedItems.length > 0 && <li className="ps-4">Removed: {configChangeList.removedItems.length}
      <ul className="list-unstyled">
        {configChangeList.removedItems.map((item, index) => <li className="ps-4" key={index}>
          {changeItemSummaryFunc(item)}
        </li>)}
      </ul>
    </li>}
    {configChangeList.changedItems.length > 0 && <li className="ps-4">Changed: {configChangeList.changedItems.length}
      <ul className="list-unstyled">
        {configChangeList.changedItems.map((item, index) => <li className="ps-4" key={index}>
          {renderVersionedConfigChange(item)}
        </li>)}
      </ul>
    </li>}
  </ul>
}

/** summarizes a configured survey */
export const renderStudyEnvironmentSurvey = (change: StudyEnvironmentSurvey) => {
  return <span>{change.survey.name} <span className="text-muted fst-italic">
    ({change.survey.stableId} v{change.survey.version})
  </span></span>
}

/** summarizes a configured consent */
export const renderStudyEnvironmentConsent = (change: StudyEnvironmentConsent) => {
  return <span>{change.consentForm.name} <span className="text-muted fst-italic">
    ({change.consentForm.stableId} v{change.consentForm.version})
  </span></span>
}

/** summarizes a notification config */
export const renderNotificationConfig = (change: NotificationConfig) => {
  return <span>{change.emailTemplate.name} - {change.notificationType}<span className="text-muted fst-italic ms-2">
    ({change.emailTemplate.stableId} v{change.emailTemplate.version})
  </span></span>
}

/** summarizes a change to a versioned entity (name + version) */
export const renderVersionedConfigChange = (change: VersionedConfigChange) => {
  const docChange = change.documentChange
  return <div>
    <ul>
      {change.configChanges.map((configChange, index) => <li key={index}>
        {configChange.propertyName} {configChange.oldValue?.toString()} -&gt; {configChange.newValue?.toString()}
      </li>)}
    </ul>
    {docChange.changed && <div>
      {docChange.oldStableId} v{docChange.oldVersion} -&gt; {docChange.newStableId} v{docChange.newVersion}
    </div>}
  </div>
}
