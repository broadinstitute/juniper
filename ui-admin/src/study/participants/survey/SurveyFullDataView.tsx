import React from 'react'
import { Question, SurveyModel } from 'survey-core'

import { surveyJSModelFromForm } from '@juniper/ui-core'

import { Answer, ConsentForm, Survey } from 'api/api'

/** renders every item in a survey response */
export default function SurveyFullDataView({ answers, survey }: {answers: Answer[], survey: Survey | ConsentForm}) {
  const surveyJsModel = surveyJSModelFromForm(survey)
  console.log(`rendering data for survey ${survey.stableId} -- question text not yet implemented`)
  return <dl>
    {answers.map((answer, index) => <ItemDisplay key={index}
      answer={answer} surveyJsModel={surveyJsModel} surveyVersion={survey.version}/>)}
  </dl>
}

const ItemDisplay = ({ answer, surveyJsModel, surveyVersion }: {answer: Answer,
  surveyJsModel: SurveyModel, surveyVersion: number}) => {
  const question = surveyJsModel.getQuestionByName(answer.questionStableId)
  const displayValue = getDisplayValue(answer, question)
  let stableIdText = answer.questionStableId
  if (answer.surveyVersion != surveyVersion) {
    stableIdText = `${answer.questionStableId} v${answer.surveyVersion}`
  }
  return <>
    <dt className="fw-normal">
      {renderQuestionText(answer, question)}
      <span className="ms-2 fst-italic text-muted">({stableIdText})</span>
    </dt>
    <dl><pre className="fw-bold">{displayValue}</pre></dl>
  </>
}

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const getDisplayValue = (answer: Answer, question: Question | QuestionWithChoices) => {
  const answerValue = answer.stringValue ?? answer.numberValue ?? answer.objectValue ?? answer.booleanValue
  if (!question) {
    // if the answer represents a computedValue, we won't have a question for it
    return answerValue?.toString()
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

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const getTextForChoice = (value: string | number | boolean | undefined, question: Question) => {
  return question.choices.find((choice: ItemValue)  => choice.value === value)?.text ?? value
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
