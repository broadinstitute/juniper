import { FormContent, TemplatedQuestion } from '@juniper/ui-core'
import { getTableOfContentsTree } from './FormTableOfContents'

/** Validate that an object is valid FormContent. */
export const validateFormContent = (rawFormContent: unknown): FormContent => {
  let parsedFormContent: FormContent

  //Checks for basic JSON validity, and returns the syntax error if invalid
  //As a result, the error message is not very user-friendly because it's
  //the underlying exception message, but it can provide some useful context
  try {
    parsedFormContent = JSON.parse(rawFormContent as string) as FormContent
  } catch (e) {
    // @ts-ignore
    throw new Error(`${e.name}: ${e.message}`)
  }

  //This will ensure that. It takes advantage of the fact that the
  //table of contents tree will fail to render. Ideally, the ToC would
  //be resilient but for the moment I'm just gonna use that as a shortcut
  try {
    getTableOfContentsTree(parsedFormContent)
  } catch (e) {
    // @ts-ignore
    throw new Error(`Error: a page or panel is misconfigured. ${e.message}`)
  }

  //Validates that all templated questions references a template name
  //that actually exists.
  try {
    validateQuestionTemplates(parsedFormContent)
  } catch (e) {
    // @ts-ignore
    throw new Error(`Error: ${e.message}`)
  }

  return parsedFormContent
}

type InvalidQuestionTemplate = {
  questionStableId: string
  referencedTemplateId: string
}

const validateQuestionTemplates = (formContent: FormContent): void => {
  const questionTemplates = formContent.questionTemplates
  if (!questionTemplates) {
    return
  }

  const questionTemplateNames = questionTemplates.map(qt => qt.name)

  const templatedQuestions: TemplatedQuestion[] = formContent.pages.flatMap(page =>
    page.elements.filter(element =>
      Object.hasOwnProperty.call(element, 'questionTemplateName')
    )
  ) as TemplatedQuestion[]

  const questionsWithInvalidTemplates: InvalidQuestionTemplate[] = templatedQuestions.filter(question =>
    !questionTemplateNames.includes(question.questionTemplateName)
  ).map(question => {
    return {
      questionStableId: question.name,
      referencedTemplateId: question.questionTemplateName
    }
  })

  if (questionsWithInvalidTemplates.length > 0) {
    throw new Error(
      `The following question(s) reference a question template that doesn't exist: 
      ${questionsWithInvalidTemplates.map(q =>
        `${q.questionStableId} (references template ${q.referencedTemplateId})`).join(', ')}`
    )
  }
}
