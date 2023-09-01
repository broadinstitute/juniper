import React, { useState } from 'react'
import { Question } from 'survey-core'

import { surveyJSModelFromForm, makeSurveyJsData } from '@juniper/ui-core'
import { Answer, ConsentForm, Survey } from 'api/api'
import InfoPopup from 'components/forms/InfoPopup'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faDownload } from '@fortawesome/free-solid-svg-icons'
import PrintFormModal from './PrintFormModal'
import { Link, Route, Routes } from 'react-router-dom'
type SurveyFullDataViewProps = {
  answers: Answer[],
  survey: Survey | ConsentForm,
  resumeData?: string,
  userId?: string
}

/** renders every item in a survey response */
export default function SurveyFullDataView({ answers, resumeData, survey, userId }:
  SurveyFullDataViewProps) {
  const [showAllQuestions, setShowAllQuestions] = useState(true)
  const [showFullQuestions, setShowFullQuestions] = useState(false)
  const surveyJsData = makeSurveyJsData(resumeData, answers, userId)
  const surveyJsModel = surveyJSModelFromForm(survey)
  surveyJsModel.data = surveyJsData!.data
  const answerMap: Record<string, Answer> = {}
  answers.forEach(answer => {
    answerMap[answer.questionStableId] = answer
  })
  let questions = surveyJsModel.getAllQuestions().filter(q => q.getType() !== 'html')
  if (!showAllQuestions) {
    questions = questions.filter(q => !!answerMap[q.name])
  }

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
          Show full question texts
        </label>
        <InfoPopup content="Whether truncate question texts longer than 100 characters below"/>
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
          <ItemDisplay key={index} question={question} answerMap={answerMap}
            surveyVersion={survey.version} showFullQuestions={showFullQuestions}/>)}
      </dl>}/>
    </Routes>
  </div>
}

type ItemDisplayProps = {
  question: Question,
  answerMap: Record<string, Answer>,
  surveyVersion: number,
  showFullQuestions: boolean
}

const ItemDisplay = ({ question, answerMap, surveyVersion, showFullQuestions }: ItemDisplayProps) => {
  const answer = answerMap[question.name]
  const displayValue = getDisplayValue(answer, question)
  let stableIdText = question.name
  if (answer && answer.surveyVersion !== surveyVersion) {
    stableIdText = `${answer.questionStableId} v${answer.surveyVersion}`
  }
  return <>
    <dt className="fw-normal">
      {renderQuestionText(answer, question, showFullQuestions)}
      <span className="ms-2 fst-italic text-muted">({stableIdText})</span>
    </dt>
    <dl><pre className="fw-bold">{displayValue}</pre></dl>
  </>
}

/** renders the value of the answer, either as plaintext, a matched choice, or an image for signatures */
export const getDisplayValue = (answer: Answer, question: Question | QuestionWithChoices): React.ReactNode => {
  if (!answer) {
    if (!question.isVisible) {
      return <span className="text-muted fst-italic fw-normal">n/a</span>
    } else {
      return <span className="text-muted fst-italic fw-normal">no answer</span>
    }
  }
  const answerValue = answer.stringValue ?? answer.numberValue ?? answer.objectValue ?? answer.booleanValue
  if (!question) {
    // if the answer represents a computedValue, we won't have a question for it
    return answerValue?.toString() ?? ''
  }
  let displayValue: React.ReactNode = answerValue
  if (question.choices) {
    if (answer.objectValue) {
      const valueArray = JSON.parse(answer.objectValue)
      const textArray = valueArray.map((value: string | number) => getTextForChoice(value, question))
      displayValue = JSON.stringify(textArray)
    } else {
      displayValue = getTextForChoice(answerValue, question)
    }
  }

  if (answer.questionStableId.endsWith('signature')) {
    displayValue = <img src={answer.stringValue}/>
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
export const renderQuestionText = (answer: Answer, question: Question, showFullQuestions: boolean) => {
  if (!question) {
    return <span>-</span>
  }
  const questionText = question?.title
  if (questionText && questionText.length > 100 && !showFullQuestions) {
    const truncatedText = `${questionText.substring(0, 100)  }...`
    return <span title={questionText}>{truncatedText}</span>
  }
  return <span>{questionText}</span>
}
