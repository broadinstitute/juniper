import React from 'react'
import { FormContentPage, FormElement } from '@juniper/ui-core'
import { Button } from 'components/forms/Button'
import { get } from 'lodash/fp'

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


/** returns the element at the given path in the given object, or undefined if the path is invalid */
export const getSurveyElementFromPath = (elementPath: string, obj: object):
    FormContentPage | FormElement | undefined => {
  try {
    return get(elementPath, obj) as FormContentPage | FormElement
  } catch (e) {
    return undefined
  }
}

/**
 * gets the index of the element in its parent.  if the elementPath given does not end in an elements[]
 * array, undefined is returned
 */
export const getCurrentElementIndex = (elementPath: string): number | undefined => {
  if (!elementPath.includes('.elements[') || !elementPath.endsWith(']')) {
    return undefined
  }
  return parseInt(elementPath.substring(elementPath.lastIndexOf('.elements[') + 10,
    elementPath.lastIndexOf(']')))
}

/** gets the nearest "container" element for the path (e.g. for adding an item to),
 *  which might be the element itself */
export const getContainerElementPath = (elementPath: string, obj: object): string => {
  const currentElement = getSurveyElementFromPath(elementPath, obj)
  let containerPath = elementPath
  if (currentElement && (('type' in currentElement) && !['page', 'panel'].includes(currentElement.type))) {
    containerPath = elementPath.substring(0, elementPath.lastIndexOf('.elements['))
  }
  return containerPath
}
