import { FormContent } from '@juniper/ui-core'
import { isPlainObject } from 'lodash'
import { getTableOfContentsTree } from './FormTableOfContents'

/** Validate that an object is valid FormContent. */
export const validateFormContent = (rawFormContent: unknown): FormContent => {
  let parsedFormContent: FormContent
  try {
    parsedFormContent = JSON.parse(rawFormContent as string) as FormContent
  } catch (e) {
    // @ts-ignore
    throw new Error(`JSON ${e.name}: ${e.message}`)
  }

  try {
    getTableOfContentsTree(parsedFormContent)
  } catch {
    throw new Error('Error parsing JSON: a page or panel has been misconfigured')
  }

  if (!isPlainObject(parsedFormContent)) {
    console.log('huh')
    throw new Error(`Invalid form, expected an object`)
  }

  return parsedFormContent
}
