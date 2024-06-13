import React, { useEffect } from 'react'
import { Survey as SurveyComponent } from 'survey-react-ui'

import {
  surveyJSModelFromForm,
  makeSurveyJsData,
  waitForImages,
  Survey,
  configureModelForPrint
} from '@juniper/ui-core'

import { Answer } from 'api/api'
import Modal from 'react-bootstrap/Modal'
import { Link } from 'react-router-dom'

type DownloadFormViewProps = {
    answers: Answer[],
    resumeData?: string,
    survey: Survey
}


/** renders the form in a fullscreen modal, and pops up the print dialog */
const PrintFormModal = ({ survey, answers, resumeData }: DownloadFormViewProps) => {
  const surveyJsData = makeSurveyJsData(resumeData, answers, undefined)
  const surveyJsModel = surveyJSModelFromForm(survey)
  surveyJsModel.data = surveyJsData.data
  configureModelForPrint(surveyJsModel)
  useEffect(() => {
    waitForImages().then(() => { window.print() })
  }, [surveyJsModel])


  return <Modal show={true} fullscreen={true}>
    <Modal.Body className="m-0 p-0">
      <div className="d-print-none d-flex justify-content-center py-2">
        {/* @ts-ignore Link to type also supports numbers for back operations */}
        <Link className="btn btn-secondary" to={-1}>Done</Link>
        <hr/>
      </div>
      <SurveyComponent model={surveyJsModel} />
    </Modal.Body>
  </Modal>
}

export default PrintFormModal
