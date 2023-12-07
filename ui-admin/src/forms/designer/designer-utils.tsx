import React from 'react'
import { FormElement } from '@juniper/ui-core'
import { Button } from 'components/forms/Button'

/** Get the user-facing label for a form element. */
export const getElementLabel = (element: FormElement, path?: string,
  setSelectedElementPath?: (path: string) => void): React.ReactNode => {
  let labelNode
  if ('type' in element && element.type === 'panel') {
    labelNode = <span>Panel <span className="fw-light fst-italic">({element.elements.length} items)</span> </span>
  } else {
    labelNode = <span>{element.name}</span>
  }
  // if no setter is provided, this is a static label
  if (!setSelectedElementPath || !path) {
    return labelNode
  }
  return <Button variant="secondary" onClick={() => setSelectedElementPath(path)}>{labelNode}</Button>
}
