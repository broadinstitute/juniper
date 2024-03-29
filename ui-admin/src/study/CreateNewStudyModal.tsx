import React, { useState } from 'react'
import Modal from 'react-bootstrap/Modal'
import { useNavContext } from '../navbar/NavContextProvider'
import Select from 'react-select'
import { Portal } from '@juniper/ui-core'
import useReactSingleSelect from 'util/react-select-utils'
import LoadingSpinner from '../util/LoadingSpinner'
import Api, { StudyTemplate } from '../api/api'
import { doApiLoad } from '../api/api-utils'
import { successNotification } from '../util/notifications'
import { Store } from 'react-notifications-component'
import InfoPopup from 'components/forms/InfoPopup'
import { Button } from 'components/forms/Button'

/** allows users to select a portal and then create a study within that portal */
export default function CreateNewStudyModal({ onDismiss }: {onDismiss: () => void}) {
  const { portalList, reload } = useNavContext()
  const [studyName, setStudyName] = useState('')
  const [studyShortcode, setStudyShortcode] = useState('')
  const [template, setTemplate] = useState<{ value: string, label: string } | undefined>(
    { value: 'BASIC', label: 'Basic' }
  )
  const templateOptions = [
    { value: 'BASIC', label: 'Basic' }
  ]
  const [selectedPortal, setSelectedPortal] = useState<Portal | undefined>(portalList[0])
  const { onChange, options, selectedOption, selectInputId } =
        useReactSingleSelect(
          portalList,
          (portal: Portal) => ({ label: portal.name, value: portal }),
          setSelectedPortal,
          selectedPortal)

  const [isLoading, setIsLoading] = useState(false)
  const createStudy = async () => {
    if (!selectedPortal) { return }
    doApiLoad(async () => {
      await Api.createStudy(selectedPortal.shortcode, {
        shortcode: studyShortcode, name: studyName, template: template?.value as StudyTemplate
      })
      Store.addNotification(successNotification('Study created'))
      reload()
    }, { setIsLoading })
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
        <input type="text" size={20} id="studyName" className="form-control" value={studyName}
          onChange={e => setStudyName(e.target.value)}/>
        <label className="form-label mt-3" htmlFor="studyShortcode">
                    Study shortcode
        </label>
        <InfoPopup content={`Unique identifier that will be used in urls and data exports to 
                    indicate this study.  Must be all lowercase with no spaces`}/>
        <input type="text" size={20} id="studyShortcode" className="form-control" value={studyShortcode}
          onChange={e => setStudyShortcode(e.target.value.trim().toLowerCase())}/>

        <label className="form-label mt-3" htmlFor="studyTemplate">
          Study Template
        </label>
        <Select
          id="studyTemplate"
          value={template}
          onChange={val => val ? setTemplate(val) : setTemplate(undefined)}
          options={templateOptions} isClearable={true}/>
      </form>
    </Modal.Body>
    <Modal.Footer>
      <LoadingSpinner isLoading={isLoading}>
        <Button variant="primary"
          disabled={!studyName || !studyShortcode}
          onClick={createStudy}
        >Create</Button>
        <button className="btn btn-secondary" onClick={onDismiss}>Cancel</button>
      </LoadingSpinner>
    </Modal.Footer>
  </Modal>
}

