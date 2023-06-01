import React, { useEffect, useState } from 'react'
import { Modal } from 'react-bootstrap'
import Api, { Enrollee, KitType } from 'api/api'
import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import Select from 'react-select'

/** Renders a modal for an admin to submit a sample collection kit request. */
export default function RequestKitModal({ enrollee, studyEnvContext, onDismiss, onSubmit }: {
    enrollee: Enrollee,
    studyEnvContext: StudyEnvContextT,
    onDismiss: () => void,
    onSubmit: () => void }) {
  const { currentEnv, portal, study } = studyEnvContext
  const [kitTypes, setKitTypes] = useState<KitType[]>()
  const [kitType, setKitType] = useState('')
  const kitTypeOptions = kitTypes?.map(kitType => ({ label: kitType.displayName, value: kitType.name }))
  const selectedKitTypeOption = kitTypeOptions?.find(kitTypeOption => kitTypeOption.value === kitType)

  const createKitRequest = async () => {
    await Api.createKitRequest(
      portal.shortcode, study.shortcode, currentEnv.environmentName, enrollee.shortcode, kitType)
  }

  const handleSubmit = async () => {
    await createKitRequest()
    onSubmit()
  }

  useEffect(() => {
    const loadKitTypes = async () => {
      const fetchedKitTypes = await Api.fetchKitTypes(portal.shortcode, study.shortcode)
      setKitTypes(fetchedKitTypes)
      kitType || setKitType(fetchedKitTypes[0].name)
    }

    loadKitTypes()
  }, [])

  return <Modal show={true} onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Request a kit</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      <form onSubmit={e => e.preventDefault()}>
        <div>
          <label className='form-label'>
            Kit type
            <Select options={kitTypeOptions} value={selectedKitTypeOption}
              onChange={option => setKitType(option?.value ?? '')}/>
          </label>
        </div>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <button className='btn btn-secondary' onClick={onDismiss}>Cancel</button>
      <button className='btn btn-primary' onClick={handleSubmit}>Request Kit</button>
    </Modal.Footer>
  </Modal>
}
