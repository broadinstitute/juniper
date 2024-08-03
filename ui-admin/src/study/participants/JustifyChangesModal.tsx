import React, { useState } from 'react'
import { Modal } from 'react-bootstrap'
import { ObjectDiff } from '@juniper/ui-core'
import { faArrowRight } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

/**
 * Renders a modal which requires an admin to justify changes to a participant's data. The
 * `changes` prop can be calculated by running `objectUtils.findDifferencesBetweenObjects`
 * on the object before and after the changes.
 */
export default function JustifyChangesModal({
  saveWithJustification,
  onDismiss,
  changes,
  bodyText,
  confirmText,
  animated = true
}: {
  saveWithJustification: (justification: string) => void,
  onDismiss: () => void,
  changes?: ObjectDiff[],
  bodyText?: React.ReactNode,
  confirmText?: string,
  animated?: boolean
}) {
  const [justification, setJustification] = useState<string>('')

  const attemptSave = () => {
    if (justification.length !== 0) {
      saveWithJustification(justification)
    }
  }

  return <Modal show={true} onHide={onDismiss} size={'lg'} animation={animated}>
    <Modal.Header closeButton>
      <Modal.Title>
        Add Justification
        <p className={'fw-light small'}>Participant data requires additional justification for audit purposes.</p>
      </Modal.Title>
    </Modal.Header>
    <Modal.Body>
      {bodyText}
      {(changes && changes.length > 0) && <div className="border-start border-3 p-1 ps-2 border-warning w-75 ms-4 mb-4"
        style={{ backgroundColor: '#f2f2f2' }}>
        <p className={'fw-bold mb-0'}>Pending Changes</p>
        {changes.map((change, idx) =>
          <p key={idx} className="mb-0">
            {change.fieldName}: {change.oldValue} <FontAwesomeIcon icon={faArrowRight}/> {change.newValue}
          </p>
        )}

      </div>}
      <h6>Description:</h6>
      <textarea className="form-control" rows={3}
        required={true} value={justification}
        placeholder={'Why are you making this change?'}
        onChange={e => setJustification(e.target.value)}/>
    </Modal.Body>
    <Modal.Footer>
      <button className='btn btn-secondary' onClick={onDismiss}>Cancel</button>
      <button className='btn btn-primary' onClick={attemptSave} disabled={justification.length === 0}>
        {confirmText || 'Save & Complete Change'}
      </button>
    </Modal.Footer>
  </Modal>
}
