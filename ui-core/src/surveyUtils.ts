import './surveyjs'

import { cloneDeep } from 'lodash'
import { SurveyModel } from 'survey-core'

import { FormContent, FormElement, VersionedForm } from './types/forms'

/** Gets a flattened list of the survey elements */
export function getFormElements(formContent: FormContent): FormElement[] {
  return formContent.pages.flatMap(page => page.elements.flatMap(getFormQuestionsHelper))
}

/** Gets a flattened list of elements from a container (page or panel) */
function getFormQuestionsHelper(element: FormElement): FormElement[] {
  return ('type' in element && element.type === 'panel')
    ? element.elements.flatMap(getFormQuestionsHelper)
    : [element]
}

const applyDefaultSurveyConfig = (surveyModel: SurveyModel): void => {
  surveyModel.focusFirstQuestionAutomatic = false
  surveyModel.showTitle = false
  surveyModel.widthMode = 'static'
}

/** Create a SurveyJS SurveyModel from a Juniper FormContent object. */
export const surveyJSModelFromFormContent = (formContent: FormContent): SurveyModel => {
  const formContentClone = cloneDeep(formContent)
  const questionTemplates = formContentClone.questionTemplates
  if (questionTemplates) {
    const elementList = getFormElements(formContentClone)
    elementList.forEach(q => {
      if ('questionTemplateName' in q) {
        const templateName = q.questionTemplateName
        const matchedTemplate = questionTemplates.find(qt => qt.name === templateName)
        if (!matchedTemplate) {
          throw new Error(`Unknown question template: ${templateName}`)
        }
        // create a new question object by merging the existing question into the template.
        // any properties explicitly specified on the question will override those from the template
        const mergedProps = Object.assign({}, matchedTemplate, q)
        Object.assign(q, mergedProps)
      }
    })
  }

  const model = new SurveyModel(formContentClone)
  applyDefaultSurveyConfig(model)
  return model
}

/** Get a VersionedForm's form content. */
const getFormContent = (form: VersionedForm): FormContent => {
  return JSON.parse(form.content) as FormContent
}

/** Create a SurveyJS SurveyModel from a Juniper VersionedForm object. */
export const surveyJSModelFromForm = (form: VersionedForm): SurveyModel => {
  return surveyJSModelFromFormContent(getFormContent(form))
}
