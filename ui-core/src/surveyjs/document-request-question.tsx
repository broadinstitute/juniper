import React from 'react'
import {
  ElementFactory,
  Question,
  Serializer,
  SurveyModel
} from 'survey-core'
import { SurveyQuestionElementBase } from 'survey-react-ui'
import { StudyEnvParams } from 'src/types/study'
import {
  isEmpty,
  isNil,
  join
} from 'lodash'
import Modal from 'react-bootstrap/Modal'
import { ModalProps } from 'react-bootstrap'
import { DocumentRequestUploader } from '../components/documents/DocumentRequestUploader'

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

  get fileNames() {
    const fileNames = []

    let index = 0
    const survey = this.question.survey as SurveyModel
    while (!isNil(survey.getValue(formatFileIndex(this.question.name, index)))) {
      const value = survey.getValue(formatFileIndex(this.question.name, index))
      fileNames.push(value)
      index++
    }

    return fileNames
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
      selectedFileNames={this.fileNames}
      setSelectedFileNames={fileNames => {
        // clear files
        const numOldFiles = this.fileNames.length
        const numNewFiles = fileNames.length

        const numFiles = Math.max(numOldFiles, numNewFiles)

        for (let i = 0; i < numFiles; i++) {
          if (i < fileNames.length) {
            survey.setValue(formatFileIndex(this.question.name, i), fileNames[i])
          } else {
            survey.setValue(formatFileIndex(this.question.name, i), undefined)
          }
        }

        this.question.value = join(fileNames, ',')
      }}
    />
  }
}

const formatFileIndex = (stableId: string, index: number) => {
  return `${stableId}[${index}]`
}
