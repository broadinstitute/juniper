import _camelCase from 'lodash/camelCase'
import { QuestionChoice, I18nSurveyElement, SurveyQuestionAddressValidation, PortalEnvironmentLanguage }
  from '@juniper/ui-core'
import { ReactQuestionFactory } from 'survey-react-ui'
import React from 'react'

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
  choices?: QuestionChoice[],
  title?: string,
  questionTemplateName?: string,
  visibleIf?: string,
  otherText?: string,
  otherPlaceholder?: string,
  isRequired?: boolean
}

/** converts the specified text into a suitable stableId */
export function generateStableId(text: string) {
  let value =_camelCase(text)
  value = value.replace(/[^a-zA-Z\d]/g, '')
  return value
}

/** renders a choice text into a stableId-suitable string */
export function getValueForChoice(choiceText: string) {
  let value = generateStableId(choiceText)
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
${leadingIndent}  "name": "${questionObj.namePrefix}${questionObj.nameSuffix}",${typeString}${visibleIfString}
${leadingIndent}  "questionTemplateName": "${questionObj.questionTemplateName}"${isRequiredString}
${leadingIndent}}`
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

  const title = textLines[0].trim()
  const newQuestionObj: QuestionObj = {
    title,
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

// register address validation type
ReactQuestionFactory.Instance.registerQuestion('addressvalidation', props => {
  return React.createElement(SurveyQuestionAddressValidation, props)
})

/**
 * Takes in an I18nSurveyElement and either returns the default value (if I18nSurveyElement is in the
 * internationalized format), or the string itself (if I18nSurveyElement is a simple string).'
 * Once the survey designer is fully
 */
export const i18nSurveyText = (element?: I18nSurveyElement, languageCode = 'default') => {
  if (element === undefined) {
    return ''
  }
  return typeof element === 'string' ? element : element[languageCode]
}

/**
 * Updates a surveyJS text element with handling for either simple string or i18n map cases
 */
export const updateI18nSurveyText = ({
  oldValue, // the full prior value
  valueText, // the text of the new value for the given language,
  languageCode, // the language code of the new value
  supportedLanguages
}: {
  oldValue: I18nSurveyElement | undefined,
  valueText: string,
  supportedLanguages: PortalEnvironmentLanguage[],
  languageCode: string}): I18nSurveyElement => {
  if (typeof oldValue === 'string') {
    if (supportedLanguages.length <= 1) {
      // if we're just dealing with simple strings, and there's only one language, we can just return the string
      return valueText
    } else {
      // otherwise, we need to convert what was simple string to an I18nMap
      const langMap: I18nSurveyElement = {}
      supportedLanguages.forEach(lang => {
        langMap[lang.languageCode] = oldValue
      })
      langMap[languageCode] = valueText
      return langMap
    }
  }
  return { ...(oldValue as object), [languageCode]: valueText }
}
