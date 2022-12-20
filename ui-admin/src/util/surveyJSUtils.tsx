import { useState, useEffect } from 'react'
import { SurveyModel } from 'survey-react-ui'
import { ConsentForm, Survey } from 'api/api'
import { SurveyCreator } from 'survey-creator-react'
import { Question, Serializer } from 'survey-core'
import { marked } from 'marked'
import { getSurveyElementList } from './pearlSurveyUtils'

type SurveyJsOptionConfig = {
  html: string,
  text: string
}

/**
 * This class serves as an interface between SurveyJS models and the rendering.  Ideally, all
 * invocations of SurveyJS functionality travel through here.
 * When modifying this file, think carefully about anything that should or shouldn't be ported over to the
 * ui-participant surveyJSUtils.  For now, we are keeping the admin and participant ui totally separate to support
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
    newSurveyJSCreator.onGenerateNewName.add(() => { console.log('newName') })
    newSurveyJSCreator.onItemValueAdded.add(() => { console.log('item!') })
    newSurveyJSCreator.onCollectionItemAllowOperations.add(() => { console.log('collection') })
    setSurveyJSCreator(newSurveyJSCreator)
  }, [])

  return { surveyJSCreator }
}

/** transform the stored survey representation into what SurveyJS expects */
function extractSurveyContent(survey: Survey | ConsentForm) {
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
            alert(`unmatched template ${  templateName}`)
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


