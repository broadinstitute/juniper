import React, { useState } from 'react'
import { CalculatedValue, Question, SurveyModel } from 'survey-core'

import {
  createAddressValidator,
  makeSurveyJsData,
  PortalEnvironment,
  PortalEnvironmentLanguage,
  surveyJSModelFromForm
} from '@juniper/ui-core'
import Api, { Answer, ConsentForm, Survey } from 'api/api'
import InfoPopup from 'components/forms/InfoPopup'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faDownload } from '@fortawesome/free-solid-svg-icons'
import PrintFormModal from './PrintFormModal'
import { Link, Route, Routes } from 'react-router-dom'
import { renderTruncatedText } from 'util/pageUtils'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'

type SurveyFullDataViewProps = {
  answers: Answer[],
  survey: Survey | ConsentForm,
  resumeData?: string,
  userId?: string,
  studyEnvContext: StudyEnvContextT
}

/** renders every item in a survey response */
export default function SurveyFullDataView({ answers, resumeData, survey, userId, studyEnvContext }:
  SurveyFullDataViewProps) {
  const [showAllQuestions, setShowAllQuestions] = useState(true)
  const [showFullQuestions, setShowFullQuestions] = useState(false)
  const surveyJsData = makeSurveyJsData(resumeData, answers, userId)
  const surveyJsModel = surveyJSModelFromForm(survey)
  surveyJsModel.onServerValidateQuestions.add(createAddressValidator(addr => Api.validateAddress(addr)))
  surveyJsModel.data = surveyJsData!.data
  const answerMap: Record<string, Answer> = {}
  answers.forEach(answer => {
    answerMap[answer.questionStableId] = answer
  })
  let questions = getQuestionsWithComputedValues(surveyJsModel)
  if (!showAllQuestions) {
    questions = questions.filter(q => !!answerMap[q.name])
  }

  const portalEnv = studyEnvContext.portal.portalEnvironments.find((env: PortalEnvironment) =>
    env.environmentName === studyEnvContext.currentEnv.environmentName)
  const supportedLanguages = portalEnv?.supportedLanguages ?? []

  return <div>
    <div className="d-flex d-print-none">
      <div className="d-flex align-items-center">
        <label>
          <input type="checkbox" className="me-2"
            checked={showAllQuestions} onChange={() => setShowAllQuestions(!showAllQuestions)}/>
          Show all questions
        </label>
        <InfoPopup content="Show all questions in the survey, or only questions answered by the participant"/>
      </div>
      <div className="d-flex align-items-center">
        <label className="ms-4">
          <input type="checkbox" className="me-2"
            checked={showFullQuestions} onChange={() => setShowFullQuestions(!showFullQuestions)}/>
          Show full question text
        </label>
        <InfoPopup content="Show full question text vs. truncated to first 100 characters"/>
      </div>
      <div className="ms-auto">
        <Link to="print">
          <FontAwesomeIcon icon={faDownload}/> print/download
        </Link>
      </div>
    </div>
    <hr/>
    <Routes>
      <Route path="print" element={<PrintFormModal answers={answers}
        resumeData={resumeData}
        survey={survey}/>
      }/>
      <Route index element={<dl>
        {questions.map((question, index) =>
          <ItemDisplay key={index} question={question} answerMap={answerMap} supportedLanguages={supportedLanguages}
            surveyVersion={survey.version} showFullQuestions={showFullQuestions}/>)}
      </dl>}/>
    </Routes>
  </div>
}

type ItemDisplayProps = {
  question: Question | CalculatedValue,
  answerMap: Record<string, Answer>,
  surveyVersion: number,
  showFullQuestions: boolean,
  supportedLanguages: PortalEnvironmentLanguage[]
}

/**
 * Renders a single survey question and its answer,
 * with stableId and the viewed language (if applicable)
 */
export const ItemDisplay = ({
  question, answerMap, surveyVersion, showFullQuestions, supportedLanguages
}: ItemDisplayProps) => {
  const answer = answerMap[question.name]
  const answerLanguage = supportedLanguages.find(lang => lang.languageCode === answer?.viewedLanguage)
  const displayValue = getDisplayValue(answer, question)
  let stableIdText = question.name
  if (answer && answer.surveyVersion !== surveyVersion) {
    stableIdText = `${answer.questionStableId} v${answer.surveyVersion}`
  }
  if ((question as CalculatedValue).expression) {
    stableIdText += ' -- derived'
  }
  return <>
    <dt className="fw-normal">
      {renderQuestionText(answer, question, showFullQuestions)}
      <span className="ms-2 fst-italic text-muted">
        ({stableIdText}) {answerLanguage && ` (Answered in ${answerLanguage.languageName})`}
      </span>
    </dt>
    <dl>
      <pre className="fw-bold">{displayValue}</pre>
    </dl>
  </>
}

/** renders the value of the answer, either as plaintext, a matched choice, or an image for signatures */
export const getDisplayValue = (answer: Answer,
  question: Question | QuestionWithChoices | CalculatedValue): React.ReactNode => {
  const isCalculatedValue = !!(question as CalculatedValue).expression
  if (!answer) {
    if (!(question as Question).isVisible || isCalculatedValue) {
      return <span className="text-muted fst-italic fw-normal">n/a</span>
    } else {
      return <span className="text-muted fst-italic fw-normal">no answer</span>
    }
  }
  const answerValue = answer.stringValue ?? answer.numberValue ?? answer.objectValue ?? answer.booleanValue

  let displayValue: React.ReactNode = answerValue
  if ((question as Question).choices) {
    if (answer.objectValue) {
      const valueArray = JSON.parse(answer.objectValue)
      const textArray = valueArray.map((value: string | number) => getTextForChoice(value, question as Question))
      displayValue = JSON.stringify(textArray)
    } else {
      displayValue = getTextForChoice(answerValue, question as Question)
    }
  }
  if (answer.booleanValue !== undefined) {
    displayValue = answer.booleanValue ? 'True' : 'False'
  }
  if (answer.questionStableId.endsWith('signature')) {
    displayValue = <img src={answer.stringValue}/>
  }
  if (answer.otherDescription) {
    displayValue = `${displayValue} - ${answer.otherDescription}`
  }
  return displayValue
}

/**
 * matches the answer stableId with the choice of the question.  note that this is not yet version-safe
 * and so, e.g.,  answers to choices that no longer exist may not render correctly
 */
export const getTextForChoice = (value: string | number | boolean | undefined, question: Question) => {
  return question.choices.find((choice: ItemValue)  => choice.value === value)?.text ?? value
}

type QuestionWithChoices = Question & {
  choices: ItemValue[]
}
type ItemValue = { text: string, value: string }


/** gets the question text -- truncates it at 100 chars */
export const renderQuestionText = (answer: Answer,
  question: Question | CalculatedValue,
  showFullQuestions: boolean) => {
  if (!question) {
    return <span>-</span>
  }
  const questionText = (question as Question).title
  return renderTruncatedText(questionText, showFullQuestions ? 10000 : 100)
}

/**
 * returns the last stableId that this calculatedValue is dependent on, or null
 * if it is independent.
 * e.g. if the expression is "{heightInInches} * 2.54", this will return "heightInInches"
 * This should match the logic in SurveyParseUtils.java
 */
export function getUpstreamStableId(calculatedValue: CalculatedValue): string | undefined {
  const match = calculatedValue.expression.match(/.*\{(.+?)\}.*/)
  return match ? match[1] : undefined
}

/**
 * returns an array of the questions for display, which excludes html elements, and includes
 * calculatedValues that have includeIntoResult
 */
export function getQuestionsWithComputedValues(model: SurveyModel) {
  const questionsAndVals: (Question | CalculatedValue)[] = model
    .getAllQuestions().filter(q => q.getType() !== 'html')
  model.calculatedValues
    .filter(calculatedValue => calculatedValue.includeIntoResult)
    .forEach(calculatedValue => {
      // figure out where in the question list to insert this, based on which questions the computation uses.
      const upstreamStableId = getUpstreamStableId(calculatedValue)
      if (!upstreamStableId) {
        questionsAndVals.push(calculatedValue)
      } else {
        const spliceIndex = questionsAndVals.findIndex(question => question.name === upstreamStableId)
        questionsAndVals.splice(spliceIndex, 0, calculatedValue)
      }
    })
  return questionsAndVals
}

