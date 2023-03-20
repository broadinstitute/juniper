import React, {useEffect, useState} from 'react'

import * as SurveyCore from 'survey-core'
import {Model, Question, Serializer, StylesManager, SurveyModel} from 'survey-core'
import {micromark} from 'micromark'
import 'inputmask/dist/inputmask/phone-codes/phone'
// eslint-disable-next-line
// @ts-ignore
import * as widgets from 'surveyjs-widgets'
import {Survey as SurveyJSComponent} from 'survey-react-ui'
import {ResumableData, SurveyJSForm} from 'api/api'
import {useSearchParams} from 'react-router-dom'
import {getSurveyElementList} from './pearlSurveyUtils'


// See https://surveyjs.io/form-library/examples/control-data-entry-formats-with-input-masks/reactjs#content-code
widgets.inputmask(SurveyCore)

const PAGE_NUMBER_PARAM_NAME = 'page'

/** used for paging surveys */
export type PageNumberControl = {
  pageNumber: number | null,
  updatePageNumber: (page: number) => void
}

/**
 * hook for reading/writing pageNumbers to url search params as 'page'
 * */
export function useRoutablePageNumber(): PageNumberControl {
  const [searchParams, setSearchParams] = useSearchParams()
  const pageParam = searchParams.get(PAGE_NUMBER_PARAM_NAME)
  let urlPageNumber = null
  if (pageParam) {
    urlPageNumber = parseInt(pageParam)
  }

  /** update the url with the new page number */
  function updatePageNumber(newPageNumber: number) {
    searchParams.set('page', (newPageNumber).toString())
    setSearchParams(searchParams)
  }

  return {
    pageNumber: urlPageNumber,
    updatePageNumber
  }
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
 */
export function useSurveyJSModel(form: SurveyJSForm, resumeData: ResumableData | null,
                                 onComplete: () => void, pager: PageNumberControl) {
  const [surveyModel, setSurveyModel] = useState<SurveyModel | null>(null)

  /** hand a page change by updating state of both the surveyJS model and our internal state*/
  function handlePageChanged(model: SurveyModel, options: any) { // eslint-disable-line @typescript-eslint/no-explicit-any, max-len
    const newPage = options.newCurrentPage.num
    pager.updatePageNumber(newPage)
  }

  /** syncs the surveyJS survey model with the given data/pageNumber */
  function refreshSurvey(refreshData: ResumableData | null, pagerPageNumber: number | null) {
    StylesManager.applyTheme('defaultV2')
    const newSurveyModel = new Model(extractSurveyContent(form))

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

    newSurveyModel.showTitle = false
    setSurveyModel(newSurveyModel)
  }


  // handle external page number changes, e.g. browser back button
  useEffect(() => {
    if (surveyModel && pager.pageNumber != null) {
      surveyModel.currentPageNo = pager.pageNumber - 1
    }
  }, [pager.pageNumber])

  // load the initial data into the survey on page load
  useEffect(() => refreshSurvey(resumeData, pager.pageNumber), [])

  useEffect(() => {
    // add the event handler here (rather than in initialize)
    // so onComplete has the right scope of the model
    if (surveyModel) {
      surveyModel.onComplete.add(onComplete)
      surveyModel.onCurrentPageChanged.add(handlePageChanged)
      surveyModel.onTextMarkdown.add(applyMarkdown)
    }
  }, [surveyModel])
  const pageNumber = surveyModel ? surveyModel.currentPageNo + 1 : 1
  const SurveyComponent = surveyModel ? <SurveyJSComponent model={surveyModel}/> : <></>
  return {surveyModel, refreshSurvey, pageNumber, SurveyComponent}
}

export const applyMarkdown = (survey: object, options: { text: string, html: string }) => {
  options.html = micromark(options.text)
}

export enum SourceType {
  ENROLLEE = 'ENROLLEE',
  ADMIN = 'ADMIN',
  CLINICAL_RECORD = 'CLINICAL RECORD',
  PROXY = 'PROXY',
  ANON = 'ANON' // for not-logged-in users (e.g. preregistration)
}

export type FormResponseDto = {
  enrolleeId: string,
  sourceType: SourceType,
  resumeData?: string,
  parsedData: {
    items: FormResponseItem[]
  }
}

export type PreRegResponseDto = {
  parsedData: {
    items: FormResponseItem[]
  }
  qualified: boolean
}

export type PreEnrollResponseDto = {
  parsedData: {
    items: FormResponseItem[]
  }
  qualified: boolean,
  studyEnvironmentId: string
}

export type ConsentResponseDto = FormResponseDto & {
  consentFormId: string,
  consented: boolean
}

export type SurveyResponseDto = FormResponseDto & {
  surveyId: string,
  complete: boolean
}

export type FormResponseItem = {
  stableId: string,
  questionText: string,
  questionType: string,
  value: string | object | undefined,
  displayValue: string | object | undefined,
}

export type SurveyJsItem = {
  name: string | number,
  title: string,
  value: object,
  displayValue: string
}

// SurveyJS doesn't seem to export their calculated value type, so we define a shim here
type CalculatedValue = {
  name: string,
  value: string | boolean | null | number | object
}

/**
 * Takes a ConsentForm or Survey object, along with a surveyJS model of the user's input, and generates a response DTO
 */
export function generateFormResponseDto({surveyJSModel, enrolleeId, sourceType}:
                                          {
                                            surveyJSModel: SurveyModel,
                                            enrolleeId: string | null, sourceType: SourceType
                                          }): FormResponseDto {
  const response = {
    enrolleeId,
    sourceType,
    resumeData: JSON.stringify({data: surveyJSModel?.data, currentPageNo: surveyJSModel?.currentPageNo}),
    parsedData: {
      items: []
    }
  } as FormResponseDto

  // the getPlainData call does not include the calculated values, but getAllValues does not include display values,
  // so to get the format we need we call getPlainData for questions, and then combine that with calculatedValues
  const data = surveyJSModel.getPlainData()
  const questionItems = data.map(({name, title, value, displayValue}: SurveyJsItem) => {
    const questionType = surveyJSModel.getQuestionByName(name.toString())?.getType()
    return {
      stableId: name,
      questionText: title,
      questionType,
      value,
      displayValue: displayValue.toString()
    } as FormResponseItem
  })

  const computedValues = getCalculatedValues(surveyJSModel)
  response.parsedData.items = questionItems.concat(computedValues)
  return response
}

/** extract the calculated values as DenormalizedResponseItems */
function getCalculatedValues(surveyJSModel: SurveyModel): FormResponseItem[] {
  return surveyJSModel.calculatedValues.map((calculatedValue: CalculatedValue) => {
    return {
      stableId: calculatedValue.name,
      value: calculatedValue.value,
      questionType: 'calculated'
    } as FormResponseItem
  })
}

/** transform the stored survey representation into what SurveyJS expects */
export function extractSurveyContent(survey: SurveyJSForm) {
  const parsedSurvey = JSON.parse(survey.content)
  const questionTemplates = parsedSurvey.questionTemplates as Question[]
  Serializer.addProperty('survey', {name: 'questionTemplates', category: 'general'})
  Serializer.addProperty('question', {name: 'questionTemplateName', category: 'general'})

  if (questionTemplates) {
    const elementList = getSurveyElementList(parsedSurvey)
    elementList.forEach(q => {
      const templateName = (q as PearlQuestion).questionTemplateName
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

type PearlQuestion = Question & {
  questionTemplateName?: string
}

