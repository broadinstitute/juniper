import React, { useId } from 'react'
import { ConsentForm, Survey } from 'api/api'
import { Model, SurveyModel } from 'survey-core'
import { extractSurveyContent } from '../../../util/surveyJSUtils'

/** renders every item in a survey response */
export default function SurveyFullDataView({ fullData, survey }: {fullData: string, survey: Survey | ConsentForm}) {
  const denormalizedData = JSON.parse(fullData) as DenormalizedResponse
  const surveyJsModel = new Model(extractSurveyContent(survey))
  console.log(`rendering data for survey ${survey.stableId} -- question text not yet implemented`)
  return <div>
    {denormalizedData.items.map((dataItem, index) => <ItemDisplay key={index}
      dataItem={dataItem} surveyJsModel={surveyJsModel}/>)}
  </div>
}

const ItemDisplay = ({ dataItem, surveyJsModel }: {dataItem: DenormalizedResponseItem, surveyJsModel: SurveyModel}) => {
  let displayValue: React.ReactNode = dataItem.displayValue
  if (!displayValue) {
    displayValue = dataItem.simpleValue
  }
  if (typeof displayValue === 'object') {
    displayValue = JSON.stringify(dataItem.displayValue, null, 2)
  } else if (dataItem.stableId.endsWith('signature')) {
    displayValue = <img src={dataItem.value}/>
  }
  const id = useId
  return <div>
    <label htmlFor={`${id}-answerVale`}>
      {renderQuestionText(dataItem, surveyJsModel)}
      <span className="ms-2 fst-italic text-muted">({dataItem.stableId})</span>
    </label>
    <pre className="fw-bold" id={`${id}-answerVale`}>{displayValue}</pre>
  </div>
}

/** gets the question text -- truncates it at 100 chars */
const renderQuestionText = (dataItem: DenormalizedResponseItem, surveyJsModel: SurveyModel) => {
  const questionText = surveyJsModel.getQuestionByName(dataItem.stableId).title
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
