import React, { useState } from 'react'
import { Modal } from 'react-bootstrap'
import { ObjectDiff } from '../../util/objectUtils'
import { faArrowRight } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

/** Renders a modal for an admin to submit a sample collection kit request. */
export default function JustifyAndSaveModal({
  saveWithJustification,
  onDismiss,
  header,
  changes
}: {
  saveWithJustification: (justification: string) => void,
  onDismiss: () => void,
  header: string,
  changes: ObjectDiff[]
}) {
  const [justification, setJustification] = useState<string>('')

  const attemptSave = () => {
    if (justification.length !== 0) {
      saveWithJustification(justification)
    }
  }

  return <Modal show={true} onHide={onDismiss} size={'lg'}>
    <Modal.Header closeButton>
      <Modal.Title>{header}</Modal.Title>
    </Modal.Header>
    <Modal.Body>

      <div>
        <p>Changes:</p>
        {changes.map(change =>
          <div>{change.fieldName}: {change.oldValue} <FontAwesomeIcon icon={faArrowRight}/> {change.newValue}</div>
        )}
      </div>
      <p>Please provide a justification for the change(s).</p>
      <textarea className="form-control" rows={3}
        required={true} value={justification}
        onChange={e => setJustification(e.target.value)}/>
    </Modal.Body>
    <Modal.Footer>
      <button className='btn btn-secondary' onClick={onDismiss}>Cancel</button>
      <button className='btn btn-primary' onClick={attemptSave} disabled={justification.length === 0}>
          Save
      </button>
    </Modal.Footer>
  </Modal>
}
