import React from 'react'
import {
  ElementFactory,
  Question,
  Serializer,
  SurveyModel
} from 'survey-core'
import { SurveyQuestionElementBase } from 'survey-react-ui'
import { StudyEnvParams } from 'src/types/study'
import { isEmpty } from 'lodash'
import Modal from 'react-bootstrap/Modal'
import { ModalProps } from 'react-bootstrap'
import { DocumentRequestUploader } from '../components/documents/DocumentRequestUploader'
import { FileAnswer, ParticipantFile } from '../types/participantFile'

const DOCUMENT_REQUEST_TYPE = 'documentrequest'

export class DocumentRequestModel extends Question {
  getType() {
    return DOCUMENT_REQUEST_TYPE
  }
}

// Add question type metadata for further serialization into JSON
Serializer.addClass(
  DOCUMENT_REQUEST_TYPE,
  [],
  () => {
    return new DocumentRequestModel('')
  },
  'question'
)

ElementFactory.Instance.registerElement(DOCUMENT_REQUEST_TYPE, name => {
  return new DocumentRequestModel(name)
})

// A class that renders questions of the new type in the UI
export class SurveyQuestionDocumentRequest extends SurveyQuestionElementBase {
  get question() {
    return this.questionBase
  }

  get selectedFileIds(): string[] {
    const value = this.question.value
    if (!value || !Array.isArray(value)) { return [] }
    return (value as FileAnswer[]).map(f => f.participantFileId)
  }

  get baseModal(): React.ElementType<ModalProps> {
    return Modal
  }

  renderElement() {
    const survey = this.question.survey as SurveyModel

    const studyEnvParams: StudyEnvParams = survey.getVariable('studyEnvParams') as StudyEnvParams
    const enrolleeShortcode = survey.getVariable('enrolleeShortcode') as string

    if (isEmpty(enrolleeShortcode)) {
      return <></>
    }

    return <DocumentRequestUploader
      studyEnvParams={studyEnvParams}
      enrolleeShortcode={enrolleeShortcode}
      selectedFileIds={this.selectedFileIds}
      onSelectedFilesChanged={files => {
        this.question.value = files.map((f: ParticipantFile): FileAnswer => ({
          participantFileId: f.id!
        }))
      }}
      ModalComponent={this.baseModal}
    />
  }
}
