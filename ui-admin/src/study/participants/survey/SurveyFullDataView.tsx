import React from 'react'
import { ConsentForm, Survey } from 'api/api'

export default function SurveyFullDataView({ fullData, survey }: {fullData: string, survey: Survey | ConsentForm}) {
  const denormalizedData = JSON.parse(fullData) as DenormalizedResponse

  return <div>
    {denormalizedData.items.map(dataItem => {
      let displayValue: any = dataItem.displayValue
      if (!displayValue) {
        displayValue = dataItem.simpleValue
      }
      if (typeof displayValue === 'object') {
        displayValue = JSON.stringify(dataItem.displayValue, null, 2)
      } else if (dataItem.stableId === 'signature') {
        displayValue = <img src={dataItem.simpleValue}/>
      }
      return <div>
        <label>{dataItem.questionText} <span className="detail">({dataItem.stableId})</span></label>
        <pre>{displayValue}</pre>
      </div>
    })}
  </div>
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
  simpleValue: any,
  displayValue: any,
}
