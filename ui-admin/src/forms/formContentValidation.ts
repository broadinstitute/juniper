import { FormContent } from '@juniper/ui-core'
import { getTableOfContentsTree } from './FormTableOfContents'

/** Validate that an object is valid FormContent. */
export const validateFormContent = (rawFormContent: unknown): FormContent => {
  let parsedFormContent: FormContent
  try {
    parsedFormContent = JSON.parse(rawFormContent as string) as FormContent
  } catch (e) {
    // @ts-ignore
    throw new Error(`${e.name}: ${e.message}`)
  }

  try {
    getTableOfContentsTree(parsedFormContent)
  } catch (e) {
    // @ts-ignore
    throw new Error(`Error: a page or panel is misconfigured. ${e.message}`)
  }

  return parsedFormContent
}
