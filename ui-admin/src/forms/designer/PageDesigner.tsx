import React, { useState } from 'react'
import { Modal } from 'react-bootstrap'

import { FormContent, FormContentPage, FormElement, HtmlElement, Question } from '@juniper/ui-core'

import { Button } from 'components/forms/Button'

import { PageElementList } from './PageElementList'
import { NewPanelForm } from './NewPanelForm'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'

/** Can the given FormElement be included in a panel (is it a Question or HtmlElement)? */
export const canBeIncludedInPanel = (element: FormElement): element is HtmlElement | Question => {
  if ('type' in element && element.type === 'panel') {
    return false
  } else {
    // This assignment has TS check that element's type is correctly narrowed to HtmlElement | Question.
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    const question: HtmlElement | Question = element
    return true
  }
}

export type PageDesignerProps = {
  readOnly: boolean
  formContent: FormContent
  value: FormContentPage
  onChange: (newValue: FormContentPage) => void
    selectedElementPath: string,
    setSelectedElementPath: (path: string) => void
  addNextQuestion: () => void
}

/** UI for editing a page of a form. */
export const PageDesigner = (props: PageDesignerProps) => {
  const {
    readOnly, formContent, value, onChange,
    selectedElementPath, setSelectedElementPath, addNextQuestion
  } = props

  const [showCreatePanelModal, setShowCreatePanelModal] = useState(false)
  const pageNum = getPageNumberFromPath(selectedElementPath)
  return (
    <div>
      <h2>Page {pageNum !== undefined ? pageNum + 1 : ''}</h2>

      <div className="mb-3">
        <Button
          disabled={readOnly}
          tooltip="Create a new question."
          variant="secondary"
          onClick={addNextQuestion}
        >
          <FontAwesomeIcon icon={faPlus}/> Add question
        </Button>
        <Button
          disabled={readOnly || value.elements.filter(canBeIncludedInPanel).length === 0}
          tooltip="Group some elements into a panel."
          variant="secondary"
          onClick={() => {
            setShowCreatePanelModal(true)
          }}
        >
          <FontAwesomeIcon icon={faPlus}/> Add panel
        </Button>
      </div>

      <PageElementList
        readOnly={readOnly}
        value={value.elements}
        selectedElementPath={selectedElementPath}
        setSelectedElementPath={setSelectedElementPath}
        onChange={newValue => {
          onChange({ ...value, elements: newValue })
        }}
      />

      {showCreatePanelModal && (
        <Modal show onHide={() => setShowCreatePanelModal(false)}>
          <Modal.Header closeButton>New Panel</Modal.Header>
          <Modal.Body>
            <NewPanelForm
              availableElements={value.elements.filter(canBeIncludedInPanel)}
              onCreate={newPanel => {
                const panelElements = newPanel.elements.map(element => element.name)
                const indexOfFirstElementInPanel = value.elements
                  .findIndex(element => 'name' in element && panelElements.includes(element.name))

                setShowCreatePanelModal(false)
                onChange({
                  ...value,
                  elements: [
                    ...value.elements.slice(0, indexOfFirstElementInPanel),
                    newPanel,
                    ...value.elements.slice(indexOfFirstElementInPanel)
                      .filter(element => !('name' in element) || !panelElements.includes(element.name))
                  ]
                })
              }}
            />
          </Modal.Body>
        </Modal>
      )}
    </div>
  )
}

const getPageNumberFromPath = (path: string) => {
  const matchResult = path.match('pages\\[(\\d+)\\]')
  if (matchResult) {
    return parseInt(matchResult[1])
  }
  return undefined
}
