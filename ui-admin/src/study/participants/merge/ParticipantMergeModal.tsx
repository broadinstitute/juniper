import { useNavContext } from '../navbar/NavContextProvider'
import React, { useState } from 'react'
import { Portal, PortalStudy } from '@juniper/ui-core'
import Modal from 'react-bootstrap/Modal'
import { Button } from '../components/forms/Button'
import Select from 'react-select'
import { ExportData } from '../api/api'
import { QueryBuilder } from 'react-querybuilder'
import 'react-querybuilder/dist/query-builder.scss'

export type MergeParticipant = {
  id?: string,
  username: string
}


/**
 * Returns a cohort builder modal
 */
export default function ParticipantMergeModal({ source, target, portalShortcode, onDismiss }:
 {onDismiss: () => void, source: MergeParticipant }) {
  const { portalList } = useNavContext()
  const initialQuery = { combinator: 'and', rules: [] }
  const [query, setQuery] = useState(initialQuery)
  const [cohortName, setCohortName] = useState('')
  const initialPortal = portalList.length == 1 ? portalList[0] : undefined
  const [selectedPortal, setSelectedPortal] = useState<Portal | undefined>(initialPortal)
  const initialStudy = selectedPortal?.portalStudies.length == 1 ? selectedPortal?.portalStudies[0] : undefined
  const [selectedStudy, setSelectedStudy] = useState<PortalStudy | undefined>(initialStudy)
  const [participantFields, setParticipantFields] = useState<ExportData>()


  return <Modal show={true} className="modal-xl" onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Merge Participants</Modal.Title>
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
          <QueryBuilder
            fields={fields}
            query={query}
            onQueryChange={q => setQuery(q)}
            addRuleToNewGroups
            controlClassnames={{ queryBuilder: 'queryBuilder-branches' }}
          /> :
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
