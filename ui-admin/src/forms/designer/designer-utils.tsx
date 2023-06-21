import { FormElement } from '@juniper/ui-core'

/** Get the user-facing label for a form element. */
export const getElementLabel = (element: FormElement): string => {
  if ('type' in element && element.type === 'panel') {
    return `Panel (${element.elements.length} elements)`
  }
  return element.name
}
