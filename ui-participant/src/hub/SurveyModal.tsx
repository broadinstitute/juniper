import React from 'react'
import SurveyView from "./survey/SurveyView";
import Modal from "react-bootstrap/Modal";
import {ModalHeader} from "react-bootstrap";

/** renders a survey but inside a modal */
export default function SurveyModal({onDismiss}: {onDismiss: () => void}) {
    return <Modal show={true} className="modal-lg" onHide={onDismiss} dialogClassName="survey-container-modal">
        <ModalHeader closeButton/>
        <Modal.Body>
            <SurveyView showHeaders={false}/>
        </Modal.Body>
    </Modal>
}