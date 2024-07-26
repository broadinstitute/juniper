import { concat, flow, uniq, without } from 'lodash/fp'
import React, { useState } from 'react'

import { FormPanel } from '@juniper/ui-core'

import { Button } from 'components/forms/Button'
import { Checkbox } from 'components/forms/Checkbox'

type NewPanelFormProps = {
  availableElements: FormPanel['elements']
  onCreate: (newPanel: FormPanel) => void
}

/** UI for creating a new panel. */
export const NewPanelForm = (props: NewPanelFormProps) => {
  const { availableElements, onCreate } = props

  const [selectedElements, setSelectedElements] = useState<string[]>([])

  return (
    <>
      <fieldset className="pb-3 border-bottom mb-3">
        <legend className="form-label fs-6 mb-2">Select elements to include</legend>

        {availableElements.map(element => {
          return (
            <div key={element.name} className="mb-2">
              <Checkbox
                checked={selectedElements.includes(element.name)}
                label={element.name}
                onChange={checked => {
                  if (checked) {
                    setSelectedElements(flow(concat([element.name]), uniq))
                  } else {
                    setSelectedElements(without([element.name]))
                  }
                }}
              />
            </div>
          )
        })}
      </fieldset>
      <div className="text-align-right">
        <Button
          disabled={selectedElements.length === 0}
          tooltip={
            selectedElements.length === 0
              ? 'Select questions to create a panel'
              : undefined
          }
          variant="primary"
          onClick={() => {
            onCreate({
              title: '',
              type: 'panel',
              elements: availableElements.filter(element => selectedElements.includes(element.name))
            })
          }}
        >
          Create panel
        </Button>
      </div>
    </>
  )
}
