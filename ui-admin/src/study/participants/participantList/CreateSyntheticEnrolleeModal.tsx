import React, { useState } from 'react'
import { Modal } from 'react-bootstrap'
import Api from 'api/api'
import { paramsFromContext, StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Select from 'react-select'
import { doApiLoad } from 'api/api-utils'
import LoadingSpinner from 'util/LoadingSpinner'
import { successNotification } from 'util/notifications'
import { Store } from 'react-notifications-component'
import useReactSingleSelect from 'util/react-select-utils'
import { TextInput } from '../../../components/forms/TextInput'
import InfoPopup from '../../../components/forms/InfoPopup'

const ENROLLEE_TYPES = ['NEW', 'CONSENTED', 'ALL_COMPLETE']

/** Renders a modal for an admin to submit a sample collection kit request. */
export default function CreateSyntheticEnrolleeModal({
  studyEnvContext, onDismiss, onSubmit
}: {
  studyEnvContext: StudyEnvContextT,
  onDismiss: () => void
  onSubmit: () => void }) {
  const [enrolleeType, setEnrolleeType] = useState<string | undefined>('NEW')
  const [username, setUsername] = useState<string>()
  const [isLoading, setIsLoading] = useState(false)

  const { onChange, options, selectedOption, selectInputId } =
    useReactSingleSelect(
      ENROLLEE_TYPES,
      (enrolleeType: string) => ({ label: enrolleeType.toLowerCase(), value: enrolleeType }),
      setEnrolleeType,
      enrolleeType)

  const handleSubmit = async () => {
    if (!enrolleeType) {
      return
    }
    doApiLoad(async () => {
      await Api.populateEnrollee(paramsFromContext(studyEnvContext), enrolleeType, username)
      Store.addNotification(successNotification('Enrollee created'))
      onSubmit()
    }, { setIsLoading })
  }

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Create synthetic enrollee</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <div>
          <label className="form-label fw-semibold me-2" htmlFor={selectInputId}>Enrollee type</label>
          <InfoPopup content={<span>
              Type determines how much content is auto-populated for the enrollee.
          </span>}/>
          <Select inputId={selectInputId} options={options} value={selectedOption} onChange={onChange}/>
        </div>
        <div className="mt-3">
          <TextInput label='Username' id='username' onChange={setUsername} value={username}
            placeholder="leave blank for auto-generated"
          />
        </div>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <button className='btn btn-primary' onClick={handleSubmit}>Create</button>
        <button className='btn btn-secondary' onClick={onDismiss}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}
