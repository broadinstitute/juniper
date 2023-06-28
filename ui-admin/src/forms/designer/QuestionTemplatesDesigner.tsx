import React from 'react'

import { FormContent } from '@juniper/ui-core'

import { QuestionTemplateList } from './QuestionTemplateList'

type QuestionTemplatesDesignerProps = {
  formContent: FormContent
  readOnly: boolean
  onChange: (newValue: FormContent) => void
}

/** UI for editing question templates in a form. */
export const QuestionTemplatesDesigner = (props: QuestionTemplatesDesignerProps) => {
  const { formContent, readOnly, onChange } = props
  const { questionTemplates = [] } = formContent

  return (
    <>
      <h2>Question Templates</h2>
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
