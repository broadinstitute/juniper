import './surveyjs'

import { Question } from 'survey-core'

import { ConsentForm, ElementBase, ElementContainer, JuniperQuestion, JuniperSurvey, Survey } from './types/forms'

/** Gets a flattened list of the survey elements */
export function getSurveyElementList(surveyModel: JuniperSurvey) {
  return surveyModel.pages.map(page => {
    return getContainerElementList(page as unknown as ElementContainer, true)
  }).flat()
}

/** Gets a flattened list of elements from a container (page or panel) */
export function getContainerElementList(container: ElementContainer, isPage: boolean):
  (ElementBase | ElementContainer | JuniperQuestion)[] {
  const containerEl = { ...container, type: isPage ? 'page' : 'panel' }
  const containerChildren = container.elements.map((element: (ElementBase | ElementContainer)) => {
    if ((element as ElementContainer).elements) {
      return getContainerElementList(element as ElementContainer, false)
    }
    return element
  })
  return [containerEl, ...containerChildren].flat()
}

/** transform the stored survey representation into what SurveyJS expects */
export function extractSurveyContent(survey: ConsentForm | Survey) {
  const parsedSurvey = JSON.parse(survey.content)
  const questionTemplates = parsedSurvey.questionTemplates as Question[]
  if (questionTemplates) {
    const elementList = getSurveyElementList(parsedSurvey)
    elementList.forEach(q => {
      const templateName = (q as JuniperQuestion).questionTemplateName
      if (templateName) {
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
