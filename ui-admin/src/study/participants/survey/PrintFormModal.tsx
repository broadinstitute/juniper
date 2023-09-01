import React, { useEffect } from 'react'
import { Survey as SurveyComponent } from 'survey-react-ui'

import {
  surveyJSModelFromForm,
  makeSurveyJsData,
  waitForImages,
  Survey,
  ConsentForm,
  configureModelForPrint
} from '@juniper/ui-core'

import { Answer } from 'api/api'
import Modal from 'react-bootstrap/Modal'
import { Button } from 'components/forms/Button'


type DownloadFormViewProps = {
    answers: Answer[],
    resumeData?: string,
    survey: Survey | ConsentForm,
    onDismiss: () => void
}


/** renders the form in a fullscreen modal, and pops up the print dialog */
const PrintFormModal = ({ survey, answers, resumeData, onDismiss }: DownloadFormViewProps) => {
  const surveyJsData = makeSurveyJsData(resumeData, answers, undefined)
  const surveyJsModel = surveyJSModelFromForm(survey)
  surveyJsModel.data = surveyJsData.data
  configureModelForPrint(surveyJsModel)
  useEffect(() => {
    waitForImages().then(() => { window.print() })
  }, [surveyJsModel])


  return <Modal show={true} onHide={onDismiss} fullscreen={true}>
    <Modal.Body className="m-0 p-0">
      <div className="d-print-none d-flex justify-content-center py-2">
        <Button variant="secondary" className="d-print-none" onClick={onDismiss}>
                    Done
        </Button>
        <hr/>
      </div>
      <SurveyComponent model={surveyJsModel} />
    </Modal.Body>
  </Modal>
}

export default PrintFormModal
