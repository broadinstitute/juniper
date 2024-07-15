import {
  FormContent,
  PortalEnvironmentLanguage,
  Question, surveyJSModelFromFormContent
} from '@juniper/ui-core'
import React, { memo, useEffect, useState } from 'react'
import { Button, IconButton } from '../../components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowLeft, faArrowRight, faArrowUp, faCode, faPlus } from '@fortawesome/free-solid-svg-icons'
import { ListElementController } from '../../portal/siteContent/designer/components/ListElementController'
import { QuestionDesigner } from './QuestionDesigner'
import { Survey as SurveyComponent } from 'survey-react-ui'
import { SurveyModel } from 'survey-core'

/**
 *
 */
export const SideBySideFormDesigner = ({ content, onChange, currentLanguage, supportedLanguages }: {
    content: FormContent, onChange: (newContent: FormContent) => void,
    currentLanguage: PortalEnvironmentLanguage, supportedLanguages: PortalEnvironmentLanguage[]
}) => {
  const [currentPageNo, setCurrentPageNo] = useState(0)
  const [currentPage, setCurrentPage] = useState(content.pages[currentPageNo])


  const handlePageChange = (direction: 'next' | 'previous') => {
    if (direction === 'next') {
      setCurrentPageNo(currentPageNo + 1)
      setCurrentPage(content.pages[currentPageNo + 1])
    } else {
      setCurrentPageNo(currentPageNo - 1)
      setCurrentPage(content.pages[currentPageNo - 1])
    }
    handleScrollToTop()
  }

  const handleScrollToTop = () => {
    window.scrollTo(0, 0)
  }

  return <div className="mt-3 container w-100">
    <>
      {currentPage.elements.map((element, elementIndex) => (
        // @ts-ignore
        <div key={element.name || elementIndex} className="container">
          <SideBySideQuestionDesigner currentPageNo={currentPageNo}
            elementIndex={elementIndex} editedContent={content}
            currentLanguage={currentLanguage} supportedLanguages={supportedLanguages}
            onChange={onChange}/>
          <div className="d-flex">
            {renderNewElementButton(content, onChange, elementIndex, currentPageNo, 'question')}
            {renderNewElementButton(content, onChange, elementIndex, currentPageNo, 'panel')}
          </div>
        </div>
      ))}
    </>
    <div className="d-flex justify-content-between mt-3 mb-5">
      <Button variant="light" className="border m-1"
        disabled={currentPageNo === 0}
        onClick={() => handlePageChange('previous')}>
        <FontAwesomeIcon icon={faArrowLeft}/> Previous page
      </Button>
      <Button className="border m-1" variant="light" onClick={() => window.scrollTo(0, 0)}>
        <FontAwesomeIcon icon={faArrowUp}/> Scroll to top
      </Button>
      <Button variant="light" className="border m-1"
        disabled={currentPageNo === content.pages.length - 1}
        onClick={() => handlePageChange('next')}>
                Next page <FontAwesomeIcon icon={faArrowRight}/>
      </Button>
    </div>
  </div>
}

const renderNewElementButton = (formContent: FormContent, onChange: (newContent: FormContent) => void,
  elementIndex: number, pageIndex: number, elementType: 'question' | 'panel') => {
  return <div className="my-2">
    <Button variant="secondary"
      aria-label={`Insert a new ${elementType}`}
      tooltip={`Insert a new ${elementType}`}
      disabled={false}
      onClick={() => {
        const newContent = { ...formContent }
        newContent.pages[pageIndex].elements.splice(elementIndex + 1, 0, (elementType == 'panel') ? {
          type: 'panel',
          elements: []
        } :
          {
            type: 'text',
            name: '',
            title: '',
            isRequired: false
          }

        )
        onChange(newContent)
      }}>
      <FontAwesomeIcon icon={faPlus}/> Insert {elementType}
    </Button>
  </div>
}


const SideBySideQuestionDesigner = ({
  elementIndex, currentPageNo, editedContent, onChange, currentLanguage, supportedLanguages
}: {
    elementIndex: number, currentPageNo: number,
    currentLanguage: PortalEnvironmentLanguage, supportedLanguages: PortalEnvironmentLanguage[],
    editedContent: FormContent, onChange: (newContent: FormContent) => void
}) => {
  const element = editedContent.pages[currentPageNo].elements[elementIndex]
  const [showJsonEditor, setShowJsonEditor] = useState(false)

  const [surveyQuestion, setSurveyQuestion] = useState<FormContent>({
    title: 'Question Preview',
    pages: [
      {
        elements: [element]
      }
    ],
    questionTemplates: editedContent.questionTemplates
  })

  const [surveyModel, setSurveyModel] = useState(surveyJSModelFromFormContent(surveyQuestion))

  useEffect(() => {
    const updatedSurveyQuestion = {
      ...surveyQuestion,
      pages: [
        {
          elements: [element]
        }
      ],
      questionTemplates: editedContent.questionTemplates
    }

    setSurveyQuestion(updatedSurveyQuestion)
    setSurveyModel(surveyJSModelFromFormContent(updatedSurveyQuestion))
  }, [element])


  surveyModel.showInvisibleElements = true
  surveyModel.showQuestionNumbers = false

  return <div key={elementIndex} className="row">
    <div className="col-md-6 p-3 rounded-start-3"
      style={{ backgroundColor: '#f3f3f3', borderRight: '1px solid #fff' }}>
      <div className="d-flex justify-content-between">
        <span className="h5">Edit question</span>
        <div className="d-flex justify-content-end">
          <IconButton icon={faCode}
            aria-label={showJsonEditor ? 'Switch to designer' : 'Switch to JSON editor'}
            className="ms-2"
            onClick={() => setShowJsonEditor(!showJsonEditor)}
          />
          <ListElementController
            index={elementIndex}
            items={editedContent.pages[currentPageNo].elements}
            updateItems={newItems => {
              const newContent = { ...editedContent }
              newContent.pages[currentPageNo].elements = newItems
              onChange(newContent)
            }}
          />
        </div>
      </div>
      { !showJsonEditor ? <>
        <label className="form-label fw-bold" htmlFor="questionType">Question type</label>
        <select id="questionType"
          disabled={false}
          className="form-select mb-2"
          // @ts-ignore
          value={element.type}
          onChange={e => {
            const newContent = { ...editedContent }
            // @ts-ignore
            newContent.pages[currentPageNo].elements[elementIndex].type = e.target.value
            onChange(newContent)
          }}>
          <option hidden>Select a question type</option>
          <option value="text">Text</option>
          <option value="checkbox">Checkbox</option>
          <option value="dropdown">Dropdown</option>
          <option value="medications">Medications</option>
          <option value="radiogroup">Radio group</option>
          <option value="signaturepad">Signature</option>
          <option value="html">Html</option>
        </select><QuestionDesigner
          question={element as Question}
          isNewQuestion={false}
          showName={false}
          showQuestionTypeHeader={false}
          readOnly={false}
          onChange={newQuestion => {
            const newContent = { ...editedContent }
            newContent.pages[currentPageNo].elements[elementIndex] = newQuestion
            onChange(newContent)
          }}
          currentLanguage={currentLanguage}
          supportedLanguages={supportedLanguages}/></> :
        <QuestionJsonEditor
          question={element as Question}
          onChange={newQuestion => {
            const newContent = { ...editedContent }
            newContent.pages[currentPageNo].elements[elementIndex] = newQuestion
            onChange(newContent)
          }}
        />
      }
    </div>
    <div className="col-md-6 p-3 rounded-end-3 survey-hide-complete"
      style={{ backgroundColor: '#f3f3f3', borderLeft: '1px solid #fff' }}>
      <QuestionPreview surveyModel={surveyModel}/>
    </div>
  </div>
}

/* This component uses the 'memo' higher-order component that's built into React.
      * This means that the component will only re-render if the props change, which
      * is very important in this case because the SurveyJS survey component is expensive
      * to re-render and we don't want to re-render every other question preview when the
      * parent state changes.
 */
const QuestionPreview = memo(({ surveyModel }: { surveyModel: SurveyModel }) => {
  return (
    <SurveyComponent model={surveyModel} readOnly={false}/>
  )
})

QuestionPreview.displayName = 'QuestionPreview'

const QuestionJsonEditor = ({ question, onChange }: {
  question: Question, onChange: (newQuestion: Question) => void
}) => {
  const [editedContent, setEditedContent] = useState(JSON.stringify(question, null, 2))
  return <div>
    <textarea
      className="form-control"
      value={editedContent}
      rows={12}
      onChange={updatedContent => {
        try {
          onChange(JSON.parse(updatedContent.target.value))
          setEditedContent(updatedContent.target.value)
        } catch (e) {
          setEditedContent(updatedContent.target.value)
        }
      }}
    />
  </div>
}
