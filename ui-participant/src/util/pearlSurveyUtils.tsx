import { IPage, Question } from 'survey-core'

/** these types are vague as we're still deciding how much custom stuff we need on top of SurveyJS */
export type PearlSurvey = {
  pages: IPage[],
  questionTemplates: PearlQuestion[]
}

/** things that we need from SurveyJS elements to render the sheet view */
export type ElementBase = {
  name: string,
  type: string,
  title: string
}

/** Encompasses SurveyJS pages and panels. */
export type ElementContainer = {
  name: string,
  elements: ElementBase[]
}

/**
 * We're extending SurveyJS to support templates -- the idea that a common question format may recur many times
 * in a survey, and so should only be coded once
 */
export type PearlQuestion = Question & {
  questionTemplateName?: string,
  type: string
}

/** Gets a flattened list of the survey elements */
export function getSurveyElementList(surveyModel: PearlSurvey) {
  return surveyModel.pages.map(page => {
    return getContainerElementList(page, true)
  }).flat()
}

/** Gets a flattened list of elements from a container (page or panel) */
export function getContainerElementList(container: ElementContainer, isPage: boolean):
  (ElementBase | ElementContainer | PearlQuestion)[] {
  const containerEl = { ...container, type: isPage ? 'page' : 'panel' }
  const containerChildren = container.elements.map((element: (ElementBase | ElementContainer)) => {
    if ((element as ElementContainer).elements) {
      return getContainerElementList(element as ElementContainer, false)
    }
    return element
  })
  return [containerEl, ...containerChildren].flat()
}
