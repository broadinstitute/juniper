import React from 'react'
import Modal from 'react-bootstrap/Modal'
import { Button } from 'components/forms/Button'

/**
 * Returns a cohort builder modal
 */
export default function NewSurveyInsightMetricModal({ onDismiss }: {onDismiss: () => void}) {
  return <Modal show={true} className="modal-xl" onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Create a cohort</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <label htmlFor="cohortName" className="h5">New Survey Insight Metric</label>
    </Modal.Body>
    <Modal.Footer>
      <Button variant="primary"
        disabled={false}
        onClick={() => alert('not yet implemented')}
      >Create</Button>
      <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
    </Modal.Footer>
  </Modal>
}
