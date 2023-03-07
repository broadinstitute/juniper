import _flatten from 'lodash/flatten'
import _camelCase from 'lodash/camelCase'
import { IPage, Question } from 'survey-core'

/**
 * A set of utilities for processing "Pearl" surveys, which are currently defined as SurveyJS surveys but with
 * additional support for question templates.
 */

/** dictionary of common alternate ways of phrasing answer choices, and stableIds we'd like to use for them */
const CHOICE_VALUE_MAPPINGS: Record<string, string> = {
  notSure: 'unsure',
  preferNotToAnswer: 'preferNoAnswer',
  'noneOfThese': 'none',
  'noneOfTheseDescribeMe': 'none',
  'dontKnowTheAnswer': 'dontKnow'
}

export type PanelObj = {
  type: string,
  title: string,
  visibleIf: string,
  elements: QuestionObj[]
}

export type QuestionObj = {
  namePrefix: string,
  nameSuffix: string,
  type: string,
  choices?: Choice[],
  title?: string,
  questionTemplateName?: string,
  visibleIf?: string,
  otherText?: string,
  otherPlaceholder?: string,
  isRequired?: boolean
}

export type Choice = {
  text: string,
  value: string
}

/** these types are vague as we're still deciding how much custom stuff we need on top of SurveyJS */
export type PearlSurvey = {
  pages: IPage[],
  questionTemplates: PearlQuestion[]
}

/** things that we need from SurveyJS elements to render the sheet view */
export type ElementBase = {
  name: string,
  type?: string,
  title?: string
}

/** Encompasses SurveyJS pages and panels. */
export type ElementContainer = ElementBase & {
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

/** gets all the elements in a flat list */
export function getSurveyElementList(surveyModel: PearlSurvey): ElementBase[] {
  return _flatten(surveyModel.pages.map(page => {
    return getContainerElementList(page, true)
  }))
}

/** gets containers in a flat list */
export function getContainerElementList(container: ElementContainer, isPage: boolean): ElementBase[] {
  const containerEl = { ...container, type: isPage ? 'page' : 'panel' }
  const containerChildren: (ElementBase | ElementBase[])[] = container.elements
    .map((element: ElementBase | ElementContainer) => {
      if ((element as ElementContainer).elements) {
        return getContainerElementList(element as ElementContainer, false)
      }
      return element as ElementBase
    })
  return _flatten([containerEl, ...containerChildren])
}

/** renders a choice text into a stableId-suitable string */
export function getValueForChoice(choiceText: string) {
  let value =_camelCase(choiceText)
  value = value.replace(/[^a-zA-Z\d]/g, '')
  if (CHOICE_VALUE_MAPPINGS[value]) {
    value = CHOICE_VALUE_MAPPINGS[value]
  }
  return value
}

/** Converts a question to JSON in a more human-readable form than default JSON.stringify. Most notably,
 * choice objects are rendered on a single line.  This reduces the overall file length of many surveys by a factor
 * of 3x or more.
 */
export function questionToJson(questionObj: QuestionObj, indentLevel= 0, excludeType=false): string {
  const leadingIndent = ' '.repeat(indentLevel)
  const visibleIfString = questionObj.visibleIf ? `\n${leadingIndent}  "visibleIf": "${questionObj.visibleIf}",` : ''
  const isRequiredString = questionObj.isRequired ? `\n${leadingIndent}  "isRequired": true,` : ''
  const typeString = excludeType ? '' : `\n${leadingIndent}  "type": "${questionObj.type}",`
  if (questionObj.choices) {
    const numChoices = questionObj.choices.length
    const otherBlock = !questionObj.otherText ? '' : `\n${leadingIndent}  "showOtherItem": true,
${leadingIndent}  "otherText": "${questionObj.otherText}",
${leadingIndent}  "otherPlaceholder": "${questionObj.otherPlaceholder}",`
    const choicesAsJson = questionObj.choices.map((c, index) => {
      const trailingChar = index === numChoices - 1 ? '' : ','
      return `${leadingIndent}    {"text": "${c.text}", "value": "${c.value}"}${trailingChar}`
    }).join('\n')
    return `${leadingIndent}{
${leadingIndent}  "name": "${questionObj.namePrefix}${questionObj.nameSuffix}",${typeString}
${leadingIndent}  "title": "${questionObj.title}",${visibleIfString}${otherBlock}${isRequiredString}
${leadingIndent}  "choices": [
${choicesAsJson}
${leadingIndent}  ]
${leadingIndent}}`
  }
  return `${leadingIndent}{
${leadingIndent}  "name": "${questionObj.namePrefix}${questionObj.nameSuffix}", 
${leadingIndent}  "type": "${questionObj.type}",${visibleIfString}${isRequiredString}
${leadingIndent}  "questionTemplateName": "${questionObj.questionTemplateName}"
}`
}

/** Renders a panel to JSON, converting the contained questions using questionToJson */
export function panelObjToJson(panelObj: PanelObj): string {
  const elementsJson = panelObj.elements.map(element => {
    return questionToJson(element, 4, true)
  }).join(',\n')
  return `{
  "type": "${panelObj.type}",
  "title": "${panelObj.title}",
  "visibleIf": "${panelObj.visibleIf}",
  "elements": [
${elementsJson}
  ]
}`
}

/** gets the json representation of a panel */
export function panelObjsToJson(panelObjs: PanelObj[] | null): string {
  if (!panelObjs) {
    return ''
  }
  return panelObjs.map(panelObjToJson).join(',\n')
}

/**
 * Given a string (likely pasted from a Word document), take the best guess at converting it into a question object.
 * Roughly speaking, the first line will be assumed to be the question, following lines are choices.  StableIds
 * will be assigned based on comelCasing choices, and/or mapping to known standard values.
 */
export function questionFromRawText(rawText: string): QuestionObj {
  const textLines = rawText.trim().split('\n')
  const choices = textLines.slice(1).map(choice => {
    return {
      text: choice.trim(),
      value: getValueForChoice(choice)
    }
  })

  const newQuestionObj: QuestionObj = {
    title: textLines[0],
    choices,
    type: choices.length > 0 ? 'radiogroup' : 'text',
    nameSuffix: '',
    namePrefix: '',
    otherText: '',
    otherPlaceholder: 'Please specify',
    isRequired: false
  }
  return newQuestionObj
}

