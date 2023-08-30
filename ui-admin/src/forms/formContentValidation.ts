import { FormContent, Question, TemplatedQuestion } from '@juniper/ui-core'

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
  const questions = getAllQuestions(formContent)

  //Validates that all templated questions references a template name that actually exists.
  validationErrors.push(...validateTemplatedQuestions(formContent, questions))

  //Validates that all questions have a 'name' field and there are no duplicate names.
  validationErrors.push(...validateQuestionNames(questions))

  //Validates that all questions have a 'type' field.
  validationErrors.push(...validateQuestionTypes(questions))

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
    return `'${question.name}' references non-existent template '${question.questionTemplateName}'`
  })
}

/** Returns an array of error messages for all questions that don't have a 'type' field. */
export const validateQuestionTypes = (questions: Question[]): string[] => {
  const errors: string[] = []
  questions.forEach(question => {
    //@ts-ignore
    if (!question.type && !question.questionTemplateName) {
      errors.push(`Question ${question.name} is missing a 'type' field.`)
    }
  })
  return errors
}

/** Returns a message with the number of questions that don't have a 'name' field. */
export const validateQuestionNames = (questions: Question[]) => {
  const errors: Question[] = []
  const errorMessages: string[] = []
  const duplicateNames = questions.filter((question, index) => {
    return questions.findIndex(q => q.name === question.name) !== index
  }).filter(q => q.name !== undefined) //filter questions that don't have names, as this can lead to redundant errors

  duplicateNames.map(question => {
    errorMessages.push(`Duplicate question name: ${ question.name }`)
  })

  questions.forEach(question => {
    if (!question.name) {
      errors.push(question)
    }
  })

  //It's hard to reference individual questions that don't have names, so just return the count.
  if (errors.length === 1) {
    errorMessages.push(`1 question is missing a 'name' field.`)
  } else if (errors.length > 1) {
    errorMessages.push(`${errors.length} questions are missing a 'name' field.`)
  }

  return errorMessages
}

/** Returns an array of all Questions in a form, including those in panels. */
export const getAllQuestions = (formContent: FormContent): Question[] => {
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
