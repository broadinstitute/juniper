import React, { useState } from 'react'
import Modal from 'react-bootstrap/Modal'
import { Button } from '../components/forms/Button'
import 'react-querybuilder/dist/query-builder.scss'
import { useLoadingEffect } from '../../../api/api-utils'

/**
 * Returns a cohort builder modal
 */
export default function ParticipantMergeModal({ source, target, portalShortcode, onDismiss }:
 {onDismiss: () => void, source?: string, target?: string, portalShortcode: string }) {
  const [sourceEmail, setSourceEmail] = useState(source)
  const [targetEmail, setTargetEmail] = useState(target)
  const [mergePlan, setMergePlan] = useState<MergePlan>()

  const { isLoading, reload } = useLoadingEffect(() => {
    if (!sourceEmail || !targetEmail) { return }
    const mergePlan = Api.fetchMergePlan(portalShortcode, sourceEmail, targetEmail)
    setMergePlan(mergePlan)
  },  [sourceEmail, targetEmail])

  return <Modal show={true} className="modal-xl" onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Merge Participants</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <div className="d-flex">
        <div className="container">
          <h5>Source Account</h5>
          <p className="text-muted mb-2">The account that you want to merge.</p>
          <div className="row my-3">
            <input type="text" className="form-control" id="source"
              onChange={e => setSourceEmail(e.target.value)} value={sourceEmail}/>
          </div>
        </div>
        <div className="container">
          <h5>Target Account</h5>
          <p className="text-muted mb-2">The account that you want to keep.</p>
          <div className="row my-3">
            <input type="text" className="form-control" id="target"
              onChange={e => setTargetEmail(e.target.value)} value={targetEmail}/>
          </div>
        </div>
      </div>

    </Modal.Body>
    <Modal.Footer>
      <Button variant="primary"
        disabled={!mergePlan}
        onClick={() => alert('not yet implemented')}
      >Create</Button>
      <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
    </Modal.Footer>
  </Modal>
}
