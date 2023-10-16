import React, { useEffect, useState } from 'react'
import { Modal } from 'react-bootstrap'
import Api, { KitType } from 'api/api'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Select from 'react-select'
import {useLoadingEffect} from "../../api/api-utils";

/** Renders a modal for an admin to submit a sample collection kit request. */
export default function RequestKitModal({ studyEnvContext, onDismiss, onSubmit }: {
    studyEnvContext: StudyEnvContextT,
    onDismiss: () => void,
    onSubmit: (kitType: string) => void }) {
  const { portal, study } = studyEnvContext
  const [kitTypes, setKitTypes] = useState<KitType[]>()
  const [kitType, setKitType] = useState('')
  const [error, setError] = useState<string>()
  const kitTypeOptions = kitTypes?.map(kitType => ({ label: kitType.displayName, value: kitType.name }))
  const selectedKitTypeOption = kitTypeOptions?.find(kitTypeOption => kitTypeOption.value === kitType)

  const handleSubmit = () => {
    onSubmit(kitType)
  }

  useLoadingEffect(async () => {
      const fetchedKitTypes = await Api.fetchKitTypes(portal.shortcode, study.shortcode)
      setKitTypes(fetchedKitTypes)
      kitType || setKitType(fetchedKitTypes[0].name)
  })


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
