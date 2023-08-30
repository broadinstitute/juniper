import { FormContent, HtmlElement, Question, TemplatedQuestion } from '@juniper/ui-core'

/** Returns a validated FormContent object, or throws an error if invalid. */
export const validateFormJson = (rawFormContent: unknown): FormContent => {
  //Checks for basic JSON validity, and returns the syntax error if invalid
  //As a result, the error message is not very user-friendly because it's
  //the underlying exception message, but it can provide some useful context
  try {
    return JSON.parse(rawFormContent as string) as FormContent
  } catch (e) {
    // @ts-ignore
    throw new Error(`${e.name}: ${e.message}`)
  }
}

/** Reasonable attempt to validate that an object is valid FormContent.
 *  This may not cover all cases, but it will catch the common/critical cases */
export const validateFormContent = (formContent: FormContent): string[] => {
  const validationErrors: string[] = []

  //Gets all questions and validates that all pages and panels have an 'elements' property.
  const questions = getAllElements(formContent)

  //Validates that all templated questions references a template name that actually exists.
  validationErrors.push(...validateTemplatedQuestions(formContent, questions))

  //Validates that all questions have a 'name' field and there are no duplicate names.
  validationErrors.push(...validateElementNames(questions))

  //Validates that all questions have a 'type' field.
  validationErrors.push(...validateElementTypes(questions))

  return validationErrors
}

/** Returns an array of error messages for all templated questions that reference a template name that doesn't exist. */
export const validateTemplatedQuestions = (formContent: FormContent, questions: Question[]): string[] => {
  const questionTemplates = formContent.questionTemplates || []
  const questionTemplateNames = questionTemplates.map(qt => qt.name)

  const templatedQuestions: TemplatedQuestion[] = questions.filter(question =>
    Object.hasOwnProperty.call(question, 'questionTemplateName')
  ) as TemplatedQuestion[]

  return templatedQuestions.filter(question =>
    !questionTemplateNames.includes(question.questionTemplateName)
  ).map(question => {
    return `'${question.name}' references non-existent question template '${question.questionTemplateName}'`
  })
}

/** Returns an array of error messages for all elements that don't have a 'type' field. */
export const validateElementTypes = (elements: (Question | HtmlElement)[]): string[] => {
  const errors: string[] = []
  elements.forEach(element => {
    // @ts-ignore
    if (!element.type && !element.questionTemplateName) {
      errors.push(`Element ${element.name} is missing a 'type' field.`)
    }
  })
  return errors
}

/** Returns a message with the number of elements that don't have a 'name' field. */
export const validateElementNames = (elements: (Question | HtmlElement)[]) => {
  const errorMessages: string[] = []

  //Find all elements that have duplicate names
  const duplicateNames = elements.filter((element, index) => {
    return elements.findIndex(e => e.name === element.name) !== index
  }).filter(q => q.name !== undefined) //filter elements that don't have names, as this can lead to redundant errors

  duplicateNames.map(question => {
    errorMessages.push(`Duplicate element name: ${ question.name }`)
  })

  //Find all elements that don't have a name
  const invalidElements: (Question | HtmlElement)[] = []
  elements.forEach(element => {
    if (!element.name) {
      invalidElements.push(element)
    }
  })

  //It's hard to reference individual elements that don't have names, so just return the count.
  if (invalidElements.length === 1) {
    errorMessages.push(`1 element is missing a 'name' field.`)
  } else if (invalidElements.length > 1) {
    errorMessages.push(`${invalidElements.length} elements are missing a 'name' field.`)
  }

  return errorMessages
}

/** Returns an array of all Questions in a form, including those in panels. */
export const getAllElements = (formContent: FormContent): Question[] => {
  if (!('pages' in formContent)) {
    throw new Error(`Error parsing form. Please ensure that the form has a 'pages' property.`)
  }

  const pages = formContent.pages

  const pageElements = pages.flatMap(page => {
    if (!('elements' in page)) {
      throw new Error(`Error parsing form. Please ensure that all pages have an 'elements' property.`)
    } else {
      return page.elements
    }
  })

  const flattenedElements = pageElements.flatMap(element => {
    if ('type' in element && element.type === 'panel') {
      if (!('elements' in element)) {
        throw new Error(`Error parsing form. Please ensure that all panels have an 'elements' property.`)
      } else {
        return element.elements
      }
    } else {
      return element as Question
    }
  })

  return flattenedElements as Question[]
}
