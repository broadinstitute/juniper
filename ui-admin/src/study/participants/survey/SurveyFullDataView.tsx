import React  from 'react'
import { Answer, ConsentForm, Survey } from 'api/api'
import { Model, Question, SurveyModel } from 'survey-core'
import { extractSurveyContent } from 'util/surveyJSUtils'

/** renders every item in a survey response */
export default function SurveyFullDataView({ answers, survey }: {answers: Answer[], survey: Survey | ConsentForm}) {
  const surveyJsModel = new Model(extractSurveyContent(survey))
  console.log(`rendering data for survey ${survey.stableId} -- question text not yet implemented`)
  return <dl>
    {answers.map((answer, index) => <ItemDisplay key={index}
      answer={answer} surveyJsModel={surveyJsModel}/>)}
  </dl>
}

const ItemDisplay = ({ answer, surveyJsModel }: {answer: Answer, surveyJsModel: SurveyModel}) => {
  const question = surveyJsModel.getQuestionByName(answer.questionStableId)
  const displayValue = getDisplayValue(answer, question)
  return <>
    <dt className="fw-normal">
      {renderQuestionText(answer, question)}
      <span className="ms-2 fst-italic text-muted">({answer.questionStableId})</span>
    </dt>
    <dl><pre className="fw-bold">{displayValue}</pre></dl>
  </>
}

export const getDisplayValue = (answer: Answer, question: Question | QuestionWithChoices) => {
  const answerValue = answer.stringValue ?? answer.numberValue ?? answer.objectValue
  if (!question) {
    // if the answer represents a computedValue, we won't have a question for it
    return answerValue
  }
  let displayValue: React.ReactNode = answerValue
  if (question.choices) {
    displayValue = question.choices.find((choice: ItemValue)  => choice.value === answerValue).text ?? answerValue
  }

  if (typeof displayValue === 'object') {
    displayValue = JSON.stringify(answerValue, null, 2)
  } else if (answer.questionStableId.endsWith('signature')) {
    displayValue = <img src={answerValue}/>
  }
  return displayValue
}

type QuestionWithChoices = Question & {
  choices: ItemValue[]
}
type ItemValue = { text: string, value: string }


/** gets the question text -- truncates it at 100 chars */
export const renderQuestionText = (answer: Answer, question: Question) => {
  if (!question) {
    return <span>-</span>
  }
  const questionText = question?.title
  if (questionText && questionText.length > 100) {
    const truncatedText = `${questionText.substring(0, 100)  }...`
    return <span title={questionText}>{truncatedText}</span>
  }
  return <span>{questionText}</span>
}

export enum SourceType {
  PARTICIPANT = 'PARTICIPANT',
  ADMIN = 'ADMIN',
  CLINICAL_RECORD = 'CLINICAL RECORD',
  PROXY = 'PROXY'
}

export type DenormalizedResponse = {
  formStableId: string,
  formVersion: number,
  participantShortcode: string,
  sourceShortcode: string,
  sourceType: SourceType,
  items: DenormalizedResponseItem[]
}

export type DenormalizedResponseItem = {
  stableId: string,
  questionText: string,
  questionType: string,
  simpleValue: string,
  displayValue: string,
  value: string
}
