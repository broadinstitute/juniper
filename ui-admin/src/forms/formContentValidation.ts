import { FormContent, Question, TemplatedQuestion } from '@juniper/ui-core'
import { getTableOfContentsTree } from './FormTableOfContents'

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

/** Validate that an object is valid FormContent. */
export const validateFormContent = (formContent: FormContent): string[] => {
  const validationErrors: string[] = []
  const questions = getAllQuestions(formContent)

  //This piggybacks off of the table of contents generator, which
  //ensures that all pages and panels have `elements` properties.
  //It would probably be better to have a separate validator for this,
  //but for now this is a quick way to ensure that all pages and panels
  //have elements.
  try {
    getTableOfContentsTree(formContent)
  } catch (e) {
    // @ts-ignore
    validationErrors.push('A page or panel is misconfigured. ' +
      'This may be due to a page or panel missing the `elements` property.')
  }

  //Validates that all templated questions references a template name that actually exists.
  validationErrors.push(...validateQuestionTemplates(formContent, questions))

  //Validates that all questions have a 'name' field
  validationErrors.push(...validateQuestionNamesExist(questions))

  //Validates that all questions have a 'type' field
  validationErrors.push(...validateQuestionTypesExist(questions))

  return validationErrors
}

type InvalidQuestionTemplate = {
  questionStableId: string
  referencedTemplateId: string
}

const validateQuestionTemplates = (formContent: FormContent, questions: Question[]): string[] => {
  const questionTemplates = formContent.questionTemplates
  if (!questionTemplates) {
    return []
  }

  const questionTemplateNames = questionTemplates.map(qt => qt.name)

  //calls getAllQuestions and returns those that have a question template name
  const templatedQuestions: TemplatedQuestion[] = questions.filter(question =>
    Object.hasOwnProperty.call(question, 'questionTemplateName')
  ) as TemplatedQuestion[]

  const questionsWithInvalidTemplates: InvalidQuestionTemplate[] = templatedQuestions.filter(question =>
    !questionTemplateNames.includes(question.questionTemplateName)
  ).map(question => {
    return {
      questionStableId: question.name,
      referencedTemplateId: question.questionTemplateName
    }
  })

  return questionsWithInvalidTemplates.map(q =>
    `'${q.questionStableId}' references non-existent template '${q.referencedTemplateId}'`)
}

const validateQuestionTypesExist = (questions: Question[]): string[] => {
  const missingFields: string[] = []
  questions.forEach(question => {
    //@ts-ignore
    if (!question.type && !question.questionTemplateName) {
      missingFields.push(`Question ${question.name} is missing a 'type' field.`)
    }
  })
  return missingFields
}

const validateQuestionNamesExist = (questions: Question[]) => {
  const questionsWithoutName: Question[] = []
  questions.forEach(question => {
    if (!question.name) {
      questionsWithoutName.push(question)
    }
  })

  //It's hard to reference individual questions that don't have names, so just return the count
  if (questionsWithoutName.length === 0) {
    return []
  } else if (questionsWithoutName.length === 1) {
    return [`1 question is missing a 'name' field.`]
  } else {
    return [`${questionsWithoutName.length} questions are missing a 'name' field.`]
  }
}

const getAllQuestions = (formContent: FormContent): Question[] => {
  const questions: Question[] = []
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
  return questions
}
