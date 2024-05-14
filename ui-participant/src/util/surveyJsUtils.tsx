import _union from 'lodash/union'
import _keys from 'lodash/keys'
import _isEqual from 'lodash/isEqual'
import { micromark } from 'micromark'
import { SurveyModel } from 'survey-core'

import {
  SURVEY_JS_OTHER_SUFFIX, UserResumeData
} from '@juniper/ui-core'

import { Answer } from 'api/api'

import '../components/ThemedSurveyAddressValidation'


type UseSurveyJsModelOpts = {
  extraCssClasses?: Record<string, string>,
  extraVariables?: Record<string, unknown>
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const applyMarkdown = (survey: object, options: { text: string, html: string }) => {
  const markdownText = micromark(options.text)
  // chop off <p> tags.
  // See https://surveyjs.io/form-library/examples/edit-survey-questions-markdown/reactjs#content-code
  if (markdownText.startsWith('<p>') && markdownText.endsWith('</p>')) {
    options.html = markdownText.substring(3, markdownText.length - 4)
  }
}

export type SurveyJsItem = {
  name: string | number,
  title: string,
  value: object,
  displayValue: string
}

export type SurveyJsValueType = string | boolean | number | object | null

/**
 * get resumeData suitable for including on a form response, current a map of userId -> data
 * resetPageNumber can be set to true for cases where the user will expect to go back to the beginning
 * of the survey next time they visit it (such as after completing a survey)
 * */
export function getResumeData(surveyJSModel: SurveyModel,
  participantUserId: string | null,
  resetPageNumber = false): string {
  const resumeData: Record<string, UserResumeData> = {}
  if (participantUserId) {
    // if this is a complete submission, the user will expect to come back to the beginning
    const currentPageNo = resetPageNumber ? 1 : surveyJSModel.currentPageNo + 1
    resumeData[participantUserId] = { currentPageNo }
  }
  return JSON.stringify(resumeData)
}

/** converts the given model into a list of answers, or an empty array if undefined */
export function getSurveyJsAnswerList(surveyJSModel: SurveyModel, selectedLanguage?: string): Answer[] {
  if (!surveyJSModel.data) {
    return []
  }
  return Object.entries(surveyJSModel.data)
    // don't make answers for the descriptive sections
    .filter(([key]) => {
      return !key.endsWith(SURVEY_JS_OTHER_SUFFIX) && surveyJSModel.getQuestionByName(key)?.getType() !== 'html'
    })
    .map(([key, value]) => makeAnswer(value as SurveyJsValueType, key, surveyJSModel.data, selectedLanguage))
}

/** return an Answer for the given value.  This should be updated to take some sort of questionType/dataType param */
export function makeAnswer(value: SurveyJsValueType, questionStableId: string,
  surveyJsData: Record<string, SurveyJsValueType>, viewedLanguage?: string): Answer {
  const answer: Answer = { questionStableId }
  if (viewedLanguage) {
    answer.viewedLanguage = viewedLanguage
  }
  if (typeof value === 'string') {
    answer.stringValue = value
  } else if (typeof value == 'number') {
    answer.numberValue = value
  } else if (typeof value == 'boolean') {
    answer.booleanValue = value
  } else if (value) {
    answer.objectValue = JSON.stringify(value)
  }
  if (surveyJsData[questionStableId + SURVEY_JS_OTHER_SUFFIX]) {
    // surveyJS "other" descriptions are always strings
    answer.otherDescription = surveyJsData[questionStableId + SURVEY_JS_OTHER_SUFFIX] as string
  } else if (questionStableId.endsWith(SURVEY_JS_OTHER_SUFFIX)) {
    const baseStableId = questionStableId.substring(0, questionStableId.lastIndexOf('-'))
    return makeAnswer(surveyJsData[baseStableId], baseStableId, surveyJsData, viewedLanguage)
  }
  return answer
}

/** compares two surveyModel.data objects and returns a list of answers corresponding to updates */
export function getUpdatedAnswers(original: Record<string, SurveyJsValueType>,
  updated: Record<string, SurveyJsValueType>, viewedLanguage?: string): Answer[] {
  const allKeys = _union(_keys(original), _keys(updated))
  const updatedKeys = allKeys.filter(key => !_isEqual(original[key], updated[key]))
    .map(key => key.endsWith(SURVEY_JS_OTHER_SUFFIX) ? key.substring(0, key.lastIndexOf(SURVEY_JS_OTHER_SUFFIX)) : key)
  const dedupedKeys = Array.from(new Set(updatedKeys).values())

  return dedupedKeys.map(key => makeAnswer(updated[key], key, updated, viewedLanguage))
}

/** get a merge of both the explicit answer data and the calculated values */
export function getDataWithCalculatedValues(model: SurveyModel) {
  const calculatedHash: Record<string, object> = {}
  model.calculatedValues.forEach(val => {
    if (val.includeIntoResult) {
      calculatedHash[val.name] = val.value
    }
  })
  return {
    ...model.data,
    ...calculatedHash
  }
}

