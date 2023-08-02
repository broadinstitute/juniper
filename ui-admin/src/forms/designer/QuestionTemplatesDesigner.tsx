import React, { useState } from 'react'

import { FormContent } from '@juniper/ui-core'

import { QuestionTemplateList } from './QuestionTemplateList'
import { Button } from '../../components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import { Modal } from 'react-bootstrap'
import { NewQuestionForm } from './NewQuestionForm'

type QuestionTemplatesDesignerProps = {
  formContent: FormContent
  readOnly: boolean
  onChange: (newValue: FormContent) => void
}

/** UI for editing question templates in a form. */
export const QuestionTemplatesDesigner = (props: QuestionTemplatesDesignerProps) => {
  const { formContent, readOnly, onChange } = props
  const { questionTemplates = [] } = formContent
  const [showCreateQuestionTemplateModal, setShowCreateQuestionTemplateModal] = useState(false)

  return (
    <>
      <h2>Question Templates</h2>

      <div className="mb-3">
        <Button
          disabled={readOnly}
          tooltip="Add a question template."
          variant="secondary"
          onClick={() => {
            setShowCreateQuestionTemplateModal(true)
          }}
        >
          <FontAwesomeIcon icon={faPlus}/> Add question template
        </Button>
      </div>

      {showCreateQuestionTemplateModal && (
        <Modal show className="modal-lg" onHide={() => setShowCreateQuestionTemplateModal(false)}>
          <Modal.Header closeButton>New Question Template</Modal.Header>
          <Modal.Body>
            <NewQuestionForm
              readOnly={readOnly}
              questionTemplates={[]} //Question templates can't be based off of other templates, so don't pass any in
              onCreate={newQuestionTemplate => {
                setShowCreateQuestionTemplateModal(false)
                onChange({
                  ...formContent,
                  questionTemplates: [
                    ...formContent.questionTemplates || [],
                    newQuestionTemplate
                  ]
                })
              }}
            />
          </Modal.Body>
        </Modal>
      )}

      {questionTemplates.length === 0
        ? (
          <p>This form does not contain any question templates.</p>
        ) : (
          <QuestionTemplateList
            formContent={formContent}
            readOnly={readOnly}
            onChange={onChange}
          />
        )}
    </>
  )
}
