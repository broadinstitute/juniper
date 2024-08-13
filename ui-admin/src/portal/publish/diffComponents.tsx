import {
  ConfigChange,
  ListChange,
  VersionedConfigChange,
  VersionedEntityChange
} from 'api/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowRight } from '@fortawesome/free-solid-svg-icons'
import React from 'react'
import { Trigger, StudyEnvironmentSurvey, PortalEnvironmentLanguage, KitType } from '@juniper/ui-core'

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

type ConfigChangesProps = {
  configChanges: ConfigChange[],
  selectedChanges: ConfigChange[],
  updateSelectedChanges: (changes: ConfigChange[]) => void
}
/** renders a list of config changes, or "no changes" if empty */
export const ConfigChanges = ({ configChanges, selectedChanges, updateSelectedChanges }: ConfigChangesProps) => {
  if (!configChanges.length) {
    return <span className="fst-italic text-muted">no changes</span>
  }
  /** handles add/remove from the selected changes */
  const updateSelection = (propertyName: string, selected: boolean) => {
    const selectionIndex = selectedChanges.findIndex(change => change.propertyName === propertyName)
    if (selected && selectionIndex < 0) {
      updateSelectedChanges([
        ...selectedChanges,
        configChanges.find(change => change.propertyName === propertyName)!
      ])
    }
    if (!selected && selectionIndex >= 0) {
      const newArr = [...selectedChanges]
      newArr.splice(selectionIndex, 1)
      updateSelectedChanges(newArr)
    }
  }
  return <ul className="list-unstyled">
    {configChanges.map((configChange, index) => {
      const propName = configChange.propertyName
      const selected = !!selectedChanges.find(change => change.propertyName === propName)
      return <li key={index}>
        <ConfigChangeView configChange={configChange} selected={selected}
          setSelected={(isSelected: boolean) => updateSelection(propName, isSelected)}/>
      </li>
    })}
  </ul>
}

const IMMUTABLE_CONFIG_PROPS = ['initialized']

type ConfigChangeViewProps = {
  configChange: ConfigChange,
  selected: boolean,
  setSelected: (selected: boolean) => void
}
/** renders a config change by converting the old and new vals to strings */
export const ConfigChangeView = ({ configChange, selected, setSelected }: ConfigChangeViewProps) => {
  const noVal = <span className="text-muted fst-italic">none</span>
  const oldVal = valuePresent(configChange.oldValue) ? configChange.oldValue.toString() : noVal
  const newVal = valuePresent(configChange.newValue) ? configChange.newValue.toString() : noVal
  const readOnly = IMMUTABLE_CONFIG_PROPS.includes(configChange.propertyName)
  return <div>
    <label>
      {!readOnly && <input type="checkbox" className="me-2" checked={selected} readOnly={readOnly}
        onChange={e => setSelected(e.target.checked)}/>}
      {readOnly && <span className="me-4"></span>}
      {configChange.propertyName}:
      <span className="ms-3">{oldVal}
        <FontAwesomeIcon icon={faArrowRight} className="mx-2 fa-sm"/>
        {newVal}
      </span>
    </label>
  </div>
}

/** helper for null/undefined checking an object */
export const valuePresent = (val: object | boolean | string) => {
  return val !== null && typeof val !== 'undefined'
}

/** returns html for displaying a single version, and 'not present' if null */
export const versionDisplay = (stableId: string, version: number) => {
  if (!stableId) {
    return <span className="fst-italic text-muted">none</span>
  }
  return <span>{stableId} v{version}</span>
}

export type Configable = StudyEnvironmentSurvey | Trigger | PortalEnvironmentLanguage | KitType
type ConfigChangeListViewProps<T extends Configable> = {
  configChangeList: ListChange<T, VersionedConfigChange>,
  selectedChanges: ListChange<T, VersionedConfigChange>,
  setSelectedChanges: (changes: ListChange<T, VersionedConfigChange>) => void,
  renderItemSummary: (item: T) => React.ReactNode
}

/** Summary of notification config changes -- doesn't show any detail yet */
export const ConfigChangeListView = <T extends Configable>
  ({ configChangeList, renderItemSummary, selectedChanges, setSelectedChanges }:
                                            ConfigChangeListViewProps<T>) => {
  if (!configChangeList.addedItems.length &&
    !configChangeList.removedItems.length && !configChangeList.changedItems.length) {
    return <span className="fst-italic text-muted">no changes</span>
  }

  /**
   * returns a new array with 'item' inserted or removed according to the isAdd param
   * matchIndex is provided as a separate argument because it's up to the caller to determine if
   * the item is already in the array.
   * */
  const makeModifiedArray = <R, >(array: R[], item: R, { matchIndex, isAdd }: {matchIndex: number, isAdd: boolean}):
    R[] => {
    if (isAdd && matchIndex < 0) {
      return [...array, item]
    }
    if (!isAdd && matchIndex >= 0) {
      const updatedItems = [...array]
      updatedItems.splice(matchIndex, 1)
      return updatedItems
    }
    return [...array]
  }

  return <ul className="list-unstyled">
    {configChangeList.addedItems.length > 0 && <li className="ps-4">Added
      <ul className="list-unstyled">
        {configChangeList.addedItems.map((item, index) => {
          const matchIndex = selectedChanges.addedItems.findIndex(listItem => listItem.id === item.id)
          return <li className="ps-4" key={index}>
            <label className="d-flex align-items-start">
              <input type="checkbox" className="me-3 mt-1"
                checked={matchIndex >= 0}
                onChange={e => {
                  const updatedItems = makeModifiedArray(selectedChanges.addedItems, item,
                    { matchIndex, isAdd: e.target.checked })
                  setSelectedChanges({ ...selectedChanges, addedItems: updatedItems })
                }}/>
              {renderItemSummary(item)}
            </label>
          </li>
        })}
      </ul>
    </li>}
    {configChangeList.removedItems.length > 0 && <li className="ps-4">Removed
      <ul className="list-unstyled">
        {configChangeList.removedItems.map((item, index) => {
          const matchIndex = selectedChanges.removedItems.findIndex(listItem => listItem.id === item.id)
          return <li className="ps-4" key={index}>
            <label className="d-flex align-items-start">
              <input type="checkbox" className="me-3 mt-1"
                checked={matchIndex >= 0}
                onChange={e => {
                  const updatedItems = makeModifiedArray(selectedChanges.removedItems, item,
                    { matchIndex, isAdd: e.target.checked })
                  setSelectedChanges({ ...selectedChanges, removedItems: updatedItems })
                }}/>
              {renderItemSummary(item)}
            </label>
          </li>
        })}
      </ul>
    </li>}
    {configChangeList.changedItems.length > 0 && <li className="ps-4">Changed
      <ul className="list-unstyled">
        {configChangeList.changedItems.map((item, index) => {
          const matchIndex = selectedChanges.changedItems.findIndex(listItem => listItem.sourceId === item.sourceId)
          return <li className="ps-4" key={index}>
            <label className="d-flex align-items-start">
              <input type="checkbox" className="me-3 mt-1"
                checked={matchIndex >= 0}
                onChange={e => {
                  const updatedItems = makeModifiedArray(selectedChanges.changedItems, item,
                    { matchIndex, isAdd: e.target.checked })
                  setSelectedChanges({ ...selectedChanges, changedItems: updatedItems })
                }}/>
              {renderVersionedConfigChange(item)}
            </label>
          </li>
        })}
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

/** summarizes a notification config */
export const renderNotificationConfig = (change: Trigger) => {
  return <span>{change.emailTemplate.name} - {change.triggerType}<span className="text-muted fst-italic ms-2">
    ({change.emailTemplate.stableId} v{change.emailTemplate.version})
  </span></span>
}

/** summarizes a portal language */
export const renderPortalLanguage = (change: PortalEnvironmentLanguage) => {
  return <span>{change.languageName} ({change.languageCode})</span>
}

export const renderKitType = (change: KitType) => {
  return <span>{change.displayName}</span>
}

/** summarizes a change to a versioned entity (name + version) */
export const renderVersionedConfigChange = (change: VersionedConfigChange) => {
  const docChange = change.documentChange
  return <div>
    {docChange.changed && <div>
      {docChange.oldStableId} v{docChange.oldVersion}
      <FontAwesomeIcon icon={faArrowRight} className="px-2 fa-sm"/>
      {docChange.newStableId} v{docChange.newVersion}
    </div>}
    <ul className="list-unstyled ms-4 pb-2">
      {change.configChanges.map((configChange, index) => <li key={index}>
        <span className="me-2">{configChange.propertyName}:</span> {configChange.oldValue?.toString()}
        <FontAwesomeIcon icon={faArrowRight} className="px-2 fa-sm"/>
        {configChange.newValue?.toString()}
      </li>)}
    </ul>
  </div>
}
