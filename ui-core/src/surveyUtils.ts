import './surveyjs'

import { FormContent, FormElement, VersionedForm } from './types/forms'

/** Gets a flattened list of the survey elements */
function getFormElements(formContent: FormContent): FormElement[] {
  return formContent.pages.flatMap(page => page.elements.flatMap(getFormQuestionsHelper))
}

/** Gets a flattened list of elements from a container (page or panel) */
function getFormQuestionsHelper(element: FormElement): FormElement[] {
  return ('type' in element && element.type === 'panel')
    ? element.elements.flatMap(getFormQuestionsHelper)
    : [element]
}

/** transform the stored survey representation into what SurveyJS expects */
export function extractSurveyContent(form: VersionedForm) {
  const parsedSurvey = JSON.parse(form.content) as FormContent
  const questionTemplates = parsedSurvey.questionTemplates
  if (questionTemplates) {
    const elementList = getFormElements(parsedSurvey)
    elementList.forEach(q => {
      if ('questionTemplateName' in q) {
        const templateName = q.questionTemplateName
        const matchedTemplate = questionTemplates.find(qt => qt.name === templateName)
        if (!matchedTemplate) {
          // TODO this is an error we'd want to log in prod systems
          if (process.env.NODE_ENV === 'development') {
            alert(`unmatched template ${templateName}`)
          }
          return
        }
        // create a new question object by merging the existing question into the template.
        // any properties explicitly specified on the question will override those from the template
        const mergedProps = Object.assign({}, matchedTemplate, q)
        Object.assign(q, mergedProps)
      }
    })
  }
  return parsedSurvey
}
