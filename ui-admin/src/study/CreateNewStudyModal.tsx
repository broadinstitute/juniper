import React, {useId, useState} from 'react'
import Modal from "react-bootstrap/Modal";
import {useNavContext} from "../navbar/NavContextProvider";
import Select from "react-select";
import {Portal} from "@juniper/ui-core";
import useReactSingleSelect from "util/react-select-utils";
import LoadingSpinner from "../util/LoadingSpinner";

export default function CreateNewStudyModal({onDismiss}: {onDismiss: () => void}) {
    const { portalList } = useNavContext()
    const [ studyName, setStudyName ] = useState('')
    const [ studyShortcode, setStudyShortcode] = useState('')
    const {onChange, options, selectedItem, selectedOption, selectInputId} =
        useReactSingleSelect(portalList, (portal: Portal) => ({label: portal.name, value: portal}), portalList[0])

    const [isLoading, setIsLoading] = useState(false)
    const createStudy = () => {
        Api.create
    }

    return <Modal show={true} className="modal-lg" onHide={onDismiss}>
        <Modal.Header closeButton>
            <Modal.Title>Create study</Modal.Title>
        </Modal.Header>
        <Modal.Body>
            <form onSubmit={e => e.preventDefault()} className="py-3">
                <label htmlFor={selectInputId}>
                    Portal:
                </label>
                <Select inputId={selectInputId} options={options} value={selectedOption} onChange={onChange}/>
                <label className="form-label mt-3" htmlFor="studyName">
                    Study name
                </label>
                <input type="text" size={20} id="studyName" className="form-control" va/>
                <label className="form-label mt-3" htmlFor="studyName">
                    Study shortcode
                </label>
                <input type="text" size={20} id="studyShortcode" className="form-control"/>
            </form>
        </Modal.Body>
        <Modal.Footer>
            <LoadingSpinner isLoading={isLoading}>
                <button
                    className="btn btn-primary"
                    disabled={!studyName || !studyShortcode}
                    onClick={createStudy}
                >Create</button>
                <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
            </LoadingSpinner>
        </Modal.Footer>
    </Modal>
}

