import { marked } from 'marked'
import { useState, useEffect } from 'react'
import { SurveyModel } from 'survey-core'
import { SurveyCreator } from 'survey-creator-react'

import { extractSurveyContent } from '@juniper/ui-core'

import { ConsentForm, Survey } from 'api/api'

type SurveyJsOptionConfig = {
  html: string,
  text: string
}

/**
 * This class serves as an interface between SurveyJS models and the rendering.  Ideally, all
 * invocations of SurveyJS functionality travel through here.
 * When modifying this file, think carefully about anything that should or shouldn't be ported over to the
 * ui-participant surveyJsUtils.  For now, we are keeping the admin and participant ui totally separate to support
 * faster iteration on admin UI.  This decision should be continually re-evaluated.
 *
 * */

/** parses the markdown for the given text */
function applyMarkedMarkdown(survey: SurveyModel, options: SurveyJsOptionConfig) {
  options.html = marked.parse(options.text)
}


/**
 * Generate a surveyJS creator object for use in a survey editing view
 * @param survey
 * @param onChange
 */
export function useSurveyJSCreator(survey: Survey | ConsentForm, onChange: () => void) {
  const [surveyJSCreator, setSurveyJSCreator] = useState<SurveyCreator | null>(null)
  useEffect(() => {
    const creatorOptions = {
      showLogicTab: false,
      isAutoSave: false,
      showSurveyTitle: false
    }
    const newSurveyJSCreator = new SurveyCreator(creatorOptions)
    // surveyJS initializes the creator from a string, so we have to create the transformed model, and
    // then convert it back to a string
    newSurveyJSCreator.text = JSON.stringify(extractSurveyContent(survey))
    newSurveyJSCreator.survey.title = survey.name
    newSurveyJSCreator.onModified.add(onChange)

    newSurveyJSCreator.onPreviewSurveyCreated.add((sender: object, options: SurveyCreator) => {
      options.survey.onTextMarkdown.add(applyMarkedMarkdown)
    })
    setSurveyJSCreator(newSurveyJSCreator)
  }, [])

  return { surveyJSCreator }
}
