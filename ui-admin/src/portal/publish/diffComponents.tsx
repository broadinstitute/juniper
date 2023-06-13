import {
  ConfigChange,
  ListChange,
  VersionedConfigChange,
  VersionedEntityChange
} from 'api/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowRight } from '@fortawesome/free-solid-svg-icons'
import React, { useId } from 'react'

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
export const ConfigChangeListView = ({ configChangeList }:
                                      {configChangeList: ListChange<object, VersionedConfigChange>}) => {
  if (!configChangeList.addedItems.length &&
    !configChangeList.addedItems.length && !configChangeList.addedItems.length) {
    return <span className="fst-italic text-muted">no changes</span>
  }
  return <ul className="list-unstyled">
    <li>Added: {configChangeList.addedItems.length}</li>
    <li>Removed: {configChangeList.removedItems.length}</li>
    <li>Changed: {configChangeList.changedItems.length}</li>
  </ul>
}
