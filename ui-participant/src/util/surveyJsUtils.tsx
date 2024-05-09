import classNames from 'classnames'
import { get, set } from 'lodash'
import _union from 'lodash/union'
import _keys from 'lodash/keys'
import _isEqual from 'lodash/isEqual'
import { micromark } from 'micromark'
import React, { useEffect, useState } from 'react'
import { SurveyModel } from 'survey-core'
import { Survey as SurveyJSComponent } from 'survey-react-ui'

import {
  createAddressValidator, PageNumberControl,
  SURVEY_JS_OTHER_SUFFIX,
  surveyJSModelFromForm,
  SurveyJsResumeData,
  useI18n, UserResumeData
} from '@juniper/ui-core'

import Api, { Answer, Survey } from 'api/api'
import { usePortalEnv } from 'providers/PortalProvider'

import '../components/ThemedSurveyAddressValidation'
import { useActiveUser } from '../providers/ActiveUserProvider'
import { useUser } from '../providers/UserProvider'


type UseSurveyJsModelOpts = {
  extraCssClasses?: Record<string, string>,
  extraVariables?: Record<string, unknown>
}

/**
 * handle setting up the surveyJS model for the given form/survey.
 * Two main goals are:
 *  1. provide a single interface point between our components and surveyJs so we can augment as needed
 *  2. Keep the SurveyJS model that we create in state, so that a rerender of this component does not destroy
 *  survey progress
 *
 * @param form a survey or ConsentForm - expects a content property that is a string that specifies the surveyJS survey
 * @param resumeData surveyJS resumable data, taken from surveyJSModel.data.  Note that the 'currentPageNo' of this
 * object will be ignored in favor of the pageNumber param below
 * @param onComplete handler for when the survey is complete.  Note that surveyjs by default will immediately hide the
 * survey on completion and display a completion banner.  To continue displaying the form, use the
 * `refreshSurvey` function
 * @param pager the control object for paging the survey
 * @param opts optional configuration for the survey
 * @param opts.extraCssClasses mapping of element to CSS classes to add to that element. See
 * https://surveyjs.io/form-library/examples/survey-customcss/reactjs#content-docs for a list of available elements.
 * @param opts.extraVariables extra variables you might want to include for a specific survey type that would not
 * be useful for all surveys (e.g., {isProxyEnrollment} for the pre-enroll survey)
 */
export function useSurveyJSModel(
  form: Survey,
  resumeData: SurveyJsResumeData | null,
  onComplete: () => void,
  pager: PageNumberControl,
  opts: UseSurveyJsModelOpts = {}
) {
  const {
    extraCssClasses = {},
    extraVariables = {}
  } = opts

  const { portalEnv } = usePortalEnv()
  const { i18n } = useI18n()

  const { profile } = useActiveUser()
  const { user, enrollees } = useUser()
  const proxyProfile = enrollees.find(enrollee => enrollee.participantUserId === user?.id && enrollee.profile)?.profile

  const [surveyModel, setSurveyModel] = useState<SurveyModel>(newSurveyJSModel(resumeData, pager.pageNumber))

  /** hand a page change by updating state of both the surveyJS model and our internal state*/
  function handlePageChanged(model: SurveyModel, options: any) { // eslint-disable-line @typescript-eslint/no-explicit-any, max-len
    const newPage = options.newCurrentPage.num
    pager.updatePageNumber(newPage)
  }

  /** returns a surveyJS survey model with the given data/pageNumber */
  function newSurveyJSModel(refreshData: SurveyJsResumeData | null, pagerPageNumber: number | null) {
    const newSurveyModel = surveyJSModelFromForm(form)

    Object.entries(extraCssClasses).forEach(([elementPath, className]) => {
      set(newSurveyModel.css, elementPath, classNames(get(newSurveyModel.css, elementPath), className))
    })

    if (refreshData) {
      newSurveyModel.data = refreshData.data
    }

    // default to first page
    let pageNumber = 0
    if (pagerPageNumber != null) {
      // if pager page is specified, use that
      // the user-visible page param is 1-indexed, but surveyJS page numbers are 0-indexed
      pageNumber = pagerPageNumber - 1
    } else if (refreshData) {
      // otherwise pick up where the user left off
      pageNumber = refreshData.currentPageNo
    }
    newSurveyModel.currentPageNo = pageNumber
    newSurveyModel.setVariable('profile', profile)
    newSurveyModel.setVariable('proxyProfile', proxyProfile)
    newSurveyModel.setVariable('portalEnvironmentName', portalEnv.environmentName)
    Object.keys(extraVariables).forEach(key => {
      newSurveyModel.setVariable(key, extraVariables[key])
    })
    newSurveyModel.onComplete.add(onComplete)
    newSurveyModel.onCurrentPageChanged.add(handlePageChanged)
    newSurveyModel.onTextMarkdown.add(applyMarkdown)
    newSurveyModel.completedHtml = '<div></div>'  // the application UX will handle showing any needed messages
    newSurveyModel.onServerValidateQuestions.add(createAddressValidator(addr => Api.validateAddress(addr), i18n))
    return newSurveyModel
  }

  const refreshSurvey = (refreshData: SurveyJsResumeData | null, pagerPageNumber: number | null) => {
    setSurveyModel(newSurveyJSModel(refreshData, pagerPageNumber))
  }

  // handle external page number changes, e.g. browser back button
  useEffect(() => {
    if (surveyModel && pager.pageNumber != null) {
      surveyModel.currentPageNo = pager.pageNumber - 1
    }
  }, [pager.pageNumber])

  const pageNumber = surveyModel ? surveyModel.currentPageNo + 1 : 1
  const SurveyComponent = surveyModel ? <SurveyJSComponent model={surveyModel}/> : <></>
  return { surveyModel, refreshSurvey, pageNumber, SurveyComponent, setSurveyModel }
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

