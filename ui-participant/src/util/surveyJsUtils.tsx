import React, {useState, useEffect, useRef} from "react";

import {Model, StylesManager, Question, Serializer} from "survey-core";
import {SurveyModel, Survey as SurveyJSComponent} from "survey-react-ui";
import {ConsentForm, ResumableData, Survey, SurveyJSForm} from "api/api";
import {useSearchParams} from "react-router-dom";
import {getSurveyElementList} from "./pearlSurveyUtils";


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
  // newPageNumber should
  function updatePageNumber(newPageNumber: number) {
    setSearchParams({page: (newPageNumber).toString()})
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
 * @param pageNumber the page number to display
 * @param updatePageNumber a function to update the page number in response to user actions
 * @return the surveyModel, a refresh function, and the current page number suitable for display (1-indexed)
 */
export function useSurveyJSModel(form: SurveyJSForm, resumeData: ResumableData | null,
                                 onComplete: () => void, pager: PageNumberControl) {
  const [surveyModel, setSurveyModel] = useState<SurveyModel | null>(null)

  function handlePageChanged(model: SurveyModel, options: any) {
    const newPage = options.newCurrentPage.num
    pager.updatePageNumber(newPage)
  }

  /** syncs the surveyJS survey model with the given data/pageNumber */
  function refreshSurvey(refreshData: ResumableData | null, pagerPageNumber: number | null) {
    StylesManager.applyTheme("defaultV2");
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
      //surveyModel.onTextMarkdown.add(applyMarkedMarkdown)
    }
  }, [surveyModel])
  const pageNumber = surveyModel ? surveyModel.currentPageNo + 1 : 1
  const SurveyComponent = surveyModel ? <SurveyJSComponent model={surveyModel}/> : <></>
  return {surveyModel, refreshSurvey, pageNumber, SurveyComponent }
}

export enum SourceType {
  PARTICIPANT = "PARTICIPANT",
  ADMIN = "ADMIN",
  CLINICAL_RECORD = "CLINICAL RECORD",
  PROXY = "PROXY",
  ANON = 'ANON' // for not-logged-in users (e.g. preregistration)
}

export type DenormalizedResponse = {
  formStableId: string,
  formVersion: number,
  participantShortcode: string,
  sourceShortcode: string,
  sourceType: SourceType,
  parsedData: {
    items: DenormalizedResponseItem[]
  }
}

export type DenormalizedPreRegResponse = DenormalizedResponse & {
  qualified: boolean
}

export type DenormalizedResponseItem = {
  stableId: string,
  questionText: string,
  questionType: string,
  answerValue: any,
  answerDisplayValue: any,
}

export type SurveyJsItem = {
  name: string,
  title: string,
  value: object,
  displayValue: string
}

/** write out the responses to the survey in a denormalized way, so that, e.g., the question text is preserved alongside
 * the answers
 */
export function generateDenormalizedData({survey, surveyJSModel, participantShortcode,
                                           sourceShortcode, sourceType}:
                                           {survey: Survey | ConsentForm, surveyJSModel: SurveyModel,
                                             participantShortcode: string,
                                             sourceShortcode: string, sourceType: SourceType}) : DenormalizedResponse {
  const response = {
    formStableId: survey.stableId,
    formVersion: survey.version,
    participantShortcode,
    sourceShortcode,
    sourceType,
    parsedData: {
      items: []
    }
  } as DenormalizedResponse
  const data = surveyJSModel.getPlainData()
  response.parsedData.items = data.map(({name, title, value, displayValue}: SurveyJsItem) => {
    return {
      stableId: name,
      questionText: title,
      questionType: surveyJSModel.getQuestionByName(name)?.getType(),
      value: value,
      displayValue: displayValue
    }
  })

  return response
}

/** transform the stored survey representation into what SurveyJS expects */
export function extractSurveyContent(survey: SurveyJSForm) {
  const parsedSurvey = JSON.parse(survey.content)
  const questionTemplates = parsedSurvey.questionTemplates as Question[]
  Serializer.addProperty('survey', { name: 'questionTemplates', category: 'general' })
  Serializer.addProperty('question', { name: 'questionTemplateName', category: 'general' })

  if (questionTemplates) {
    const elementList = getSurveyElementList(parsedSurvey)
    elementList.forEach(q => {
      const templateName = (q as PearlQuestion).questionTemplateName
      if (templateName) {
        const matchedTemplate = questionTemplates.find(qt => qt.name === templateName)
        if (!matchedTemplate) {
          // TODO this is an error we'd want to log in prod systems
          if (process.env.NODE_ENV === 'development') {
            alert("unmatched template " + templateName)
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

