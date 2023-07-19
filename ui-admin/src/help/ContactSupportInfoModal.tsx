import React from 'react'
import Modal from 'react-bootstrap/Modal'

export default function ContactSupportInfoModal({ onHide }: { onHide: () => void }) {
  return <Modal show={true} onHide={onHide}>
    <Modal.Header closeButton>
      <Modal.Title>
                Questions or problems?
      </Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <p>
                Email <a href="mailto:support@juniper.terra.bio">support@juniper.terra.bio</a>
      </p>
      <p>
                Please include the URL of any page you have a question/problem about in your email.
      </p>
    </Modal.Body>
    <Modal.Footer>
      <button className="btn btn-secondary" onClick={onHide}>Ok</button>
    </Modal.Footer>
  </Modal>
}
