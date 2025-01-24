import React from 'react'
import { Survey as SurveyComponent } from 'survey-react-ui'

import {
  configureModelForPrint,
  makeSurveyJsData,
  Survey,
  surveyJSModelFromForm,
  waitForImages
} from '@juniper/ui-core'

import { Answer } from 'api/api'
import { Button } from 'components/forms/Button'

type DownloadFormViewProps = {
    answers: Answer[],
    resumeData?: string,
    survey: Survey
}


/** renders the form in a fullscreen modal, and pops up the print dialog */
const PrintFormView = ({ survey, answers, resumeData }: DownloadFormViewProps) => {
  const surveyJsData = makeSurveyJsData(resumeData, answers, undefined)
  const surveyJsModel = surveyJSModelFromForm(survey)
  surveyJsModel.data = surveyJsData.data
  configureModelForPrint(surveyJsModel)

  const print = () => {
    waitForImages().then(() => { window.print() })
  }

  return <>
    <div className="d-flex flex-row justify-content-between align-items-baseline mb-2">
      <h5>Print Preview</h5>
      <Button variant="primary" onClick={print}>
        Download / Print
      </Button>
    </div>
    <div className="print">
      <SurveyComponent model={surveyJsModel}/>
    </div>
  </>
}

export default PrintFormView
