import { FormContent } from '@juniper/ui-core'
import React from 'react'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import { CalculatedValue } from 'survey-core'
import { SplitCalculatedValueEditor } from 'forms/designer/SplitCalculatedValueEditor'
import {
  DocsKey,
  ZendeskLink
} from 'util/zendeskUtils'

/**
 * A split-view designer for calculated values. On the left side, you can edit the calculated value expression,
 * and on the right side you can see the questions that are used in the expression. If you fill out the questions,
 * you can get a preview of how the value will be calculated.
 */
export const SplitCalculatedValueDesigner = ({ content, onChange }: {
  content: FormContent, onChange: (newContent: FormContent) => void
}) => {
  return <>
    <span className="ms-3 mt-3">
      Derived values allow you to create hidden data calculated based on survey responses.
      See <ZendeskLink doc={DocsKey.DERIVED_VALUES}>here</ZendeskLink> for more information.
    </span>
    <div className="container w-100">
      <div className="py-2">
        {renderNewElementButton(content, onChange, -1)}
      </div>
      {content && content.calculatedValues && content.calculatedValues.map((_, elementIndex) => (
        <div key={elementIndex} className="container">
          <SplitCalculatedValueEditor
            calculatedValueIndex={elementIndex}
            editedContent={content}
            onChange={onChange}/>
          <div className="py-2">
            {renderNewElementButton(content, onChange, elementIndex)}
          </div>
        </div>
      ))}
    </div>
  </>
}

const renderNewElementButton = (
  formContent: FormContent,
  onChange: (newContent: FormContent) => void,
  calculatedValueIdx: number) => {
  return <Button variant="secondary"
    aria-label={`Insert a new derived value`}
    tooltip={`Insert a new derived value`}
    disabled={false}
    onClick={() => {
      const newContent = { ...formContent }

      if (!newContent.calculatedValues) {
        newContent.calculatedValues = []
      }

      newContent.calculatedValues.splice(calculatedValueIdx + 1, 0, new CalculatedValue())
      onChange(newContent)
    }}>
    <FontAwesomeIcon icon={faPlus}/> Insert derived value
  </Button>
}
