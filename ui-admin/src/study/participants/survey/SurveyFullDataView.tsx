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
  const answerValue = answer.stringValue ?? answer.numberValue ?? answer.objectValue ?? answer.booleanValue
  if (!question) {
    // if the answer represents a computedValue, we won't have a question for it
    return answerValue?.toString()
  }
  let displayValue: React.ReactNode = answerValue
  if (question.choices) {
    if (answer.objectValue) {
      const valueArray = JSON.parse(answerValue)
      const textArray = valueArray.map((value: string | number) => getTextForChoice(value, question))
      displayValue = JSON.stringify(textArray)
    } else {
      displayValue = getTextForChoice(answerValue, question)
    }
  }

  if (answer.questionStableId.endsWith('signature')) {
    displayValue = <img src={answerValue}/>
  }
  return displayValue
}

export const getTextForChoice = (value: string | number, question: Question) => {
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
