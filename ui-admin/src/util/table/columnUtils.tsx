import {
  CellContext,
  ColumnDef,
  Table
} from '@tanstack/react-table'
import React, { useState } from 'react'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faCheck,
  faColumns
} from '@fortawesome/free-solid-svg-icons'
import Modal from 'react-bootstrap/Modal'
import {
  EnrolleeSearchExpressionResult,
  ExpressionSearchFacets,
  KeyedSearchValueTypeDefinition
} from 'api/api'
import LoadingSpinner from '../LoadingSpinner'
import Select from 'react-select'
import { Link } from 'react-router-dom'
import { checkboxColumnCell } from './tableUtils'
import {
  instantToDefaultString, dateDiff,
  ParticipantTaskStatusOptions
} from '@juniper/ui-core'
import _startCase from 'lodash/startCase'
import { get } from 'lodash'


/**
 * adapted from https://tanstack.com/table/v8/docs/examples/react/column-visibility
 * For now, this control assumes that all the headers are simple strings.
 * */
export function ColumnVisibilityControl<T>({ table, dynamicColOpts }: {table: Table<T>,
  dynamicColOpts?: {
    dynamicFacets: KeyedSearchValueTypeDefinition[],
    facets?: ExpressionSearchFacets,
    setDynamicFacets: (facets: KeyedSearchValueTypeDefinition[]) => void
  }
}) {
  const [show, setShow] = useState(false)
  // cache the dynamic cols so the user can select multiple at once without a refresh
  const [selectedDynamicFacets, setSelectedDynamicFacets] =
    useState<KeyedSearchValueTypeDefinition[]>(dynamicColOpts?.dynamicFacets ?? [])
  const handleClose = () => {
    setShow(false)
    if (dynamicColOpts) {
      dynamicColOpts.setDynamicFacets(selectedDynamicFacets)
    }
  }

  return <div className="ms-auto">
    <Button onClick={() => setShow(!show)}
      variant="light" className="border m-1"
      tooltip={'Show or hide columns'}>
      <FontAwesomeIcon icon={faColumns} className="fa-lg"/> Columns
    </Button>
    { show && <Modal show={show} onHide={handleClose}>
      <Modal.Header closeButton>
        <Modal.Title>
          Toggle column visibility
        </Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <div className="border-b border-black">
          <label>
            <input
              type='checkbox'
              checked={table.getIsAllColumnsVisible()}
              onChange={table.getToggleAllColumnsVisibilityHandler()}
            />
            <span className="ps-2">Toggle All</span>
          </label>
        </div>
        <hr/>
        {table.getAllLeafColumns().map(column => {
          return (
            <div key={column.id} className="pb-1">
              <label>
                <input
                  type='checkbox'
                  checked={column.getIsVisible()}
                  onChange={column.getToggleVisibilityHandler()}
                />
                <span className="ps-2">{ column.columnDef.header as string ?? column.columnDef.id }</span>
              </label>
            </div>
          )
        })}
        {dynamicColOpts && <DynamicColumnControl facets={dynamicColOpts.facets}
          setDynamicFacets={setSelectedDynamicFacets}
          dynamicFacets={selectedDynamicFacets}/>}
      </Modal.Body>
      <Modal.Footer>
        <Button variant="primary" onClick={handleClose}>Ok</Button>
      </Modal.Footer>
    </Modal> }
  </div>
}

export function DynamicColumnControl({ facets, dynamicFacets, setDynamicFacets }:
  {facets?: ExpressionSearchFacets, dynamicFacets: KeyedSearchValueTypeDefinition[],
    setDynamicFacets: (cols: KeyedSearchValueTypeDefinition[]) => void}) {
  const opts = facets ? Object.keys(facets)
    .sort((a, b) => a.localeCompare(b))
    .map(keyName => ({
      value: {
        ...facets[keyName],
        key: keyName
      }, label: keyName
    })) : []

  return <div>
    Add columns
    <LoadingSpinner isLoading={!facets}>
      <Select options={opts}
        isMulti={true}
        onChange={opts => setDynamicFacets(opts.map(opt => opt.value))}
        value={dynamicFacets.map(facet => ({ value: facet, label: facet.key }))}/>
    </LoadingSpinner>
  </div>
}

export type HasEnrolleeShortcode = {
  enrollee?: {
    shortcode: string
  }
}
export const enrolleeShortcodeColumn = <T extends HasEnrolleeShortcode, >(currentEnvPath: string):
  ColumnDef<T> => {
  return {
    header: 'Shortcode',
    accessorKey: 'enrollee.shortcode',
    meta: {
      columnType: 'string'
    },
    cell: info => <Link to={`${currentEnvPath}/participants/${info.getValue()}`}>{info.getValue() as string}</Link>
  }
}

export const enrolleeConsentedColumn = <T extends EnrolleeSearchExpressionResult, >(): ColumnDef<T> => {
  return  {
    header: 'Consented',
    accessorKey: 'enrollee.consented',
    id: 'enrollee.consented',
    meta: {
      columnType: 'boolean',
      filterOptions: [
        { value: true, label: 'Consented' },
        { value: false, label: 'Not Consented' }
      ]
    },
    filterFn: 'equals',
    cell: checkboxColumnCell
  }
}

/** returns a column definition for a given facet */
export const getDynamicColumn = <T extends EnrolleeSearchExpressionResult, >(facet: KeyedSearchValueTypeDefinition):
  ColumnDef<T> => {
  let field = facet.key
  const columnType = facet.type.toLowerCase()
  let cellFn = undefined
  if (facet.type === 'INSTANT') {
    cellFn = (info: CellContext<T, unknown>) => instantToDefaultString(info.getValue() as number)
  } else if (facet.type === 'BOOLEAN') {
    cellFn = (info: CellContext<T, unknown>) =>
      info.getValue() ? <FontAwesomeIcon icon={faCheck}/> : ''
  }
  if (field.startsWith('user')) {
    field = field.replace('user.', 'participantUser.')
  }
  if (field.startsWith('answer')) {
    return dynamicAnswerColumn(facet)
  } else if (field.startsWith('task')) {
    return dynamicTaskColumn(facet)
  } else if (field.startsWith('fileUpload.') || field.startsWith('fileDownload.')) {
    return dynamicFileColumn(facet)
  } else {
    const colDef: ColumnDef<T> = {
      id: field,
      header: _startCase(field.replace('.', ' ')),
      accessorKey: field,
      meta: {
        columnType
      }
    }
    if (cellFn) {
      colDef.cell = cellFn
    }
    return colDef
  }
}

const dynamicTaskColumn = <T extends EnrolleeSearchExpressionResult, >(facet: KeyedSearchValueTypeDefinition):
  ColumnDef<T> => {
  const { taskStableId, field, header } = parseTaskFacet(facet)

  const colDef: ColumnDef<T> = {
    id: facet.key,
    header,
    accessorFn: info => {
      const task = info.tasks.find(task => task.targetStableId === taskStableId)
      if (field === 'assigned') {
        return task != null && task.status !== 'REMOVED'
      } else if (field === 'status') {
        return ParticipantTaskStatusOptions.find(opt => opt.value === task?.status)?.label || task?.status || ''
      } else if (field === 'completedDaysAgo') {
        if (!task?.completedAt) {
          return ''
        }
        return dateDiff(new Date(), new Date(task.completedAt * 1000))
      }

      return get(task, field)
    },
    meta: {
      columnType: facet.type.toLowerCase()
    }
  }
  if (field === 'assigned') {
    colDef.cell = (info: CellContext<T, unknown>) => info.getValue() ? <FontAwesomeIcon icon={faCheck}/> : ''
  }
  return colDef
}

const dynamicAnswerColumn = <T extends EnrolleeSearchExpressionResult, >(facet: KeyedSearchValueTypeDefinition):
  ColumnDef<T> => {
  const { questionStableId, surveyStableId, header } = parseAnswerFacet(facet)
  return {
    id: facet.key,
    header,
    accessorFn: info => {
      const answer = info.answers.find(ans =>
        ans.surveyStableId === surveyStableId && ans.questionStableId === questionStableId)
      // we can add code here at a later time to map answer stableId string values to choice labels
      return answer?.stringValue ?? answer?.booleanValue ?? answer?.numberValue ?? answer?.objectValue ?? ''
    },
    meta: {
      columnType: facet.type.toLowerCase()
    }
  }
}

const dynamicFileColumn = <T extends EnrolleeSearchExpressionResult, >(facet: KeyedSearchValueTypeDefinition):
  ColumnDef<T> => {
  const isDownload = facet.key.startsWith('fileDownload.')
  const questionStableId = facet.key.split('.')[1]
  const header = _startCase(`${questionStableId} ${isDownload ? 'downloaded' : 'uploaded'}`)

  return {
    id: facet.key,
    header,
    accessorFn: info => {
      const filesForQuestion = (info.participantFiles ?? [])
        .filter(f => f.associatedAnswers.some(a => a.questionStableId === questionStableId))
      if (filesForQuestion.length === 0) { return false }
      if (!isDownload) { return true }
      return filesForQuestion.some(f => f.downloads.some(d => d.participantUserId != null))
    },
    cell: (info: CellContext<T, unknown>) => info.getValue() ? <FontAwesomeIcon icon={faCheck}/> : '',
    meta: { columnType: 'boolean' }
  }
}

export function parseTaskFacet(facet: KeyedSearchValueTypeDefinition) {
  const key = facet.key
  const [, taskStableId, field] = key.split('.')

  let header = taskStableId
  if (taskStableId.match(/^[a-z]{2}_[a-z]{2}_/)) {
    header = taskStableId.slice(5)
  }

  return { taskStableId, field, header: _startCase(`${header} ${field}`) }
}
export function parseAnswerFacet(facet: KeyedSearchValueTypeDefinition) {
  const field = facet.key
  const [, surveyStableId, questionStableId] = field.split('.')
  let header = _startCase(questionStableId)
  if (questionStableId.match(/^[a-z]{2}_[a-z]{2}_/)) {
    header = _startCase(questionStableId.slice(5))
  }
  return { surveyStableId, questionStableId, header }
}
