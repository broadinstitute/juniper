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

  //Also validates that all pages and panels have an 'elements' property.
  const questions = getAllQuestions(formContent)

  //Validates that all templated questions references a template name that actually exists.
  validationErrors.push(...validateTemplatedQuestions(formContent, questions))

  //Validates that all questions have a 'name' field.
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
  questions.forEach(question => {
    if (!question.name) {
      errors.push(question)
    }
  })

  //It's hard to reference individual questions that don't have names, so just return the count.
  if (errors.length === 0) {
    return []
  } else if (errors.length === 1) {
    return [`1 question is missing a 'name' field.`]
  } else {
    return [`${errors.length} questions are missing a 'name' field.`]
  }
}

/** Returns an array of all Questions in a form, including those in panels. */
export const getAllQuestions = (formContent: FormContent): Question[] => {
  const questions: Question[] = []
  try {
    formContent.pages.forEach(page => {
      page.elements.forEach(element => {
        if ('type' in element && element.type === 'panel') {
          element.elements.forEach(panelElement => {
            questions.push(panelElement as Question)
          })
        } else {
          questions.push(element as Question)
        }
      })
    })
  } catch (e) {
    throw new Error(`Error parsing form. Please ensure that all pages and panels have an 'elements' property.`)
  }
  return questions
}
