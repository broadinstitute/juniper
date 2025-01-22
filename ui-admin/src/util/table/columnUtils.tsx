import { Table } from '@tanstack/react-table'
import React, { useState } from 'react'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faColumns } from '@fortawesome/free-solid-svg-icons'
import Modal from 'react-bootstrap/Modal'
import { StudyEnvParams } from '@juniper/ui-core'
import Api, { ExpressionSearchFacets } from 'api/api'
import { useLoadingEffect } from 'api/api-utils'
import LoadingSpinner from '../LoadingSpinner'
import Select from 'react-select'


/**
 * adapted from https://tanstack.com/table/v8/docs/examples/react/column-visibility
 * For now, this control assumes that all the headers are simple strings.
 * */
export function ColumnVisibilityControl<T>({ table, dynamicColOpts }: {table: Table<T>,
  dynamicColOpts?: {
    dynamicCols: string[], setDynamicCols: (cols: string[]) => void, studyEnvParams: StudyEnvParams }
}) {
  const [show, setShow] = useState(false)
  // cache the dynamic cols so the user can select multiple at once without a refresh
  const [selectedDynamicCols, setSelectedDynamicCols] = useState<string[]>(dynamicColOpts?.dynamicCols ?? [])
  const handleClose = () => {
    setShow(false)
    if (dynamicColOpts) {
      dynamicColOpts.setDynamicCols(selectedDynamicCols)
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
        {dynamicColOpts && <DynamicColumnControl studyEnvParams={dynamicColOpts.studyEnvParams}
          setDynamicCols={setSelectedDynamicCols} dynamicCols={selectedDynamicCols}/>}
      </Modal.Body>
      <Modal.Footer>
        <Button variant="primary" onClick={handleClose}>Ok</Button>
      </Modal.Footer>
    </Modal> }
  </div>
}

export function DynamicColumnControl({ studyEnvParams, dynamicCols, setDynamicCols }:
  {studyEnvParams: StudyEnvParams, dynamicCols: string[], setDynamicCols: (cols: string[]) => void}) {
  const [facets, setFacets] = useState<ExpressionSearchFacets>({})
  const { isLoading } = useLoadingEffect(async () => {
    const loadedFacets = await Api.getExpressionSearchFacets(
      studyEnvParams.portalShortcode,
      studyEnvParams.studyShortcode,
      studyEnvParams.envName)
    setFacets(loadedFacets)
  }, [], 'Failed to load cohort criteria options')
  const opts = Object.keys(facets)
    .sort((a, b) => a.localeCompare(b))
    .map(facet => ({ value: facet, label: facet }))

  return <div>
    Add columns
    <LoadingSpinner isLoading={isLoading}>
      <Select options={opts}
        isMulti={true} onChange={opts => setDynamicCols(opts.map(opt => opt.value))}
        value={dynamicCols.map(col => ({ value: col, label: col }))}/>
    </LoadingSpinner>
  </div>
}
