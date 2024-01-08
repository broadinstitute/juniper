import { useNavContext } from '../navbar/NavContextProvider'
import React, { useEffect, useState } from 'react'
import { Portal, PortalStudy } from '@juniper/ui-core'
import Modal from 'react-bootstrap/Modal'
import { Button } from '../components/forms/Button'
import Select from 'react-select'
import useReactSingleSelect from '../util/react-select-utils'
import Api, { ExportData } from '../api/api'
import { useLoadingEffect } from 'api/api-utils'
import { Field, QueryBuilder } from 'react-querybuilder'
import 'react-querybuilder/dist/query-builder.css'
import { QueryBuilderBootstrap } from '@react-querybuilder/bootstrap'
import { Store } from 'react-notifications-component'
import { failureNotification } from '../util/notifications'

/**
 * Returns a cohort builder modal
 */
export default function CreateNewCohortModal({ onDismiss }: {onDismiss: () => void}) {
  const { portalList } = useNavContext()
  const initialQuery = { combinator: 'and', rules: [] }
  const [query, setQuery] = useState(initialQuery)
  const [cohortName, setCohortName] = useState('')
  const [selectedPortal, setSelectedPortal] = useState<Portal>()
  const [selectedStudy, setSelectedStudy] = useState<PortalStudy>()
  const [participantFields, setParticipantFields] = useState<ExportData>()

  useLoadingEffect(async () => {
    if (!selectedStudy || !selectedPortal) { return }
    const response = await Api.exportEnrollees(
      selectedPortal.shortcode,
      selectedStudy.study.shortcode,
      'live', { fileFormat: 'JSON', limit: 0 })
    const result = await response.json()
    if (!response.ok) {
      Store.addNotification(failureNotification('Failed to load cohort criteria options', result.message))
    } else {
      setParticipantFields(result)
    }
  }, [selectedStudy])

  const fields: Field[] = participantFields?.columnKeys.map(field => ({
    name: field,
    label: field
  })) || []

  useEffect(() => {
    setQuery(initialQuery)
  }, [selectedStudy, selectedPortal])

  const {
    onChange: portalOnChange, options: portalOptions,
    selectedOption: selectedPortalOption, selectInputId: selectPortalInputId
  } =
    useReactSingleSelect(
      portalList,
      (portal: Portal) => ({ label: portal.name, value: portal }),
      setSelectedPortal,
      selectedPortal)

  const {
    onChange: studyOnChange, options: studyOptions,
    selectedOption: selectedStudyOption, selectInputId: selectStudyInputId
  } =
    useReactSingleSelect(
      selectedPortal?.portalStudies || [],
      (study: PortalStudy) => ({ label: study.study.name, value: study }),
      setSelectedStudy,
      selectedStudy)

  return <Modal show={true} className="modal-xl" onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Create a cohort</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <label htmlFor="cohortName" className="h5">Cohort Name</label>
      <p className="text-muted mb-2">Give your cohort a unique name. You can reuse this cohort when targeting
        this same subset of participants for new opportunities.</p>
      <div className="container">
        <div className="row my-3">
          <input type="text" className="form-control" id="cohortName"
            onChange={e => setCohortName(e.target.value)} value={cohortName}/>
        </div>
      </div>
      <h5>Cohort Source</h5>
      <p className="text-muted mb-2">Choose the source of your cohort participants.</p>
      <div className="my-3">
        <label htmlFor={selectPortalInputId}>Portal</label>
        <Select inputId={selectPortalInputId} options={portalOptions}
          value={selectedPortalOption} onChange={portalOnChange}/>
        <label htmlFor={selectStudyInputId} className="mt-3">Study</label>
        <Select isDisabled={!selectedPortal} inputId={selectStudyInputId} options={studyOptions}
          value={selectedStudyOption} onChange={studyOnChange}/>
      </div>
      <hr/>
      <h5>Cohort Criteria</h5>
      <span className="text-muted mb-2">Choose the criteria for your cohort based on previous
        survey responses from the participants in your studies. You may define as many rules
        as you would like to narrow down your cohort.
      </span>
      <div className="mt-3">
        { selectedStudy ?
          <QueryBuilderBootstrap>
            <QueryBuilder
              fields={fields}
              query={query}
              onQueryChange={q => setQuery(q)}
              addRuleToNewGroups
              controlClassnames={{ queryBuilder: 'queryBuilder-branches' }}
            />
          </QueryBuilderBootstrap> :
          <span className="text-muted fst-italic">You must select a study before selecting cohort criteria</span> }
      </div>

    </Modal.Body>
    <Modal.Footer>
      <Button variant="primary"
        disabled={!cohortName || !selectedStudy}
        onClick={() => alert('not yet implemented')}
      >Create</Button>
      <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
    </Modal.Footer>
  </Modal>
}
