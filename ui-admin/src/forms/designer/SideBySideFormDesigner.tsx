import {
  FormContent, FormElement, FormPanel,
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
import { Textarea } from '../../components/forms/Textarea'
import { isEqual } from 'lodash'

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
            element={content.pages[currentPageNo].elements[elementIndex]}
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

/* Note that this component is memoized using React.memo
 * Since survey pages can contain many questions, we need to be mindful of
 * how many times we re-render these components. Since the parent component state
 * is updated with every keystroke, we memoize this to minimize the number
 * of re-renders that take place. SurveyComponent from SurveyJS in particular
 * is sluggish when undergoing many simultaneous re-renders.
 */
const SideBySideQuestionDesigner = memo(({
  elementIndex, element, currentPageNo, editedContent, onChange, currentLanguage, supportedLanguages
}: {
    elementIndex: number, element: FormElement, currentPageNo: number,
    currentLanguage: PortalEnvironmentLanguage, supportedLanguages: PortalEnvironmentLanguage[],
    editedContent: FormContent, onChange: (newContent: FormContent) => void
}) => {
  const [showJsonEditor, setShowJsonEditor] = useState(false)

  // Cut down the survey to just the specific question that we're editing, so we can display
  // a preview using the SurveyJS survey component
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

  // @ts-ignore
  const elementType = element.type === 'panel' ? 'panel' : 'question'

  return <div key={elementIndex} className="row">
    <div className="col-md-6 p-3 rounded-start-3"
      style={{ backgroundColor: '#f3f3f3', borderRight: '1px solid #fff' }}>
      <div className="d-flex justify-content-between">
        <span className="h5">Edit {elementType}</span>
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
      { !showJsonEditor ?
        <>
          {elementType === 'question' ? <>
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
            </select>
            <QuestionDesigner
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
              supportedLanguages={supportedLanguages}/>
          </> :
            <PanelDesigner
              panel={element as FormPanel}
              currentPageNo={currentPageNo}
              onChange={newPanel => {
                const newContent = { ...editedContent }
                newContent.pages[currentPageNo].elements[elementIndex] = newPanel
                onChange(newContent)
              }}
              currentLanguage={currentLanguage}
              supportedLanguages={supportedLanguages}
            />
          }
        </> :
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
      <SurveyComponent model={surveyModel} readOnly={false}/>
    </div>
  </div>
}, (prevProps, nextProps) => {
  // Only re-render if the question has changed. Note that React.memo only does a shallow object comparison
  // by default, which is why we have custom prop-comparison here.
  return isEqual(prevProps.element, nextProps.element)
})

SideBySideQuestionDesigner.displayName = 'SideBySideQuestionDesigner'

const QuestionJsonEditor = ({ question, onChange }: {
  question: Question, onChange: (newQuestion: Question) => void
}) => {
  const [editedContent, setEditedContent] = useState(JSON.stringify(question, null, 2))
  return <Textarea
    className="form-control"
    value={editedContent}
    rows={15}
    onChange={updatedContent => {
      try {
        onChange(JSON.parse(updatedContent))
        setEditedContent(updatedContent)
      } catch (e) {
        setEditedContent(updatedContent)
      }
    }}
    label={'Question JSON'}
    infoContent={'Edit the question JSON directly. Learn more about SurveyJS JSON here.'}
  />
}

const PanelDesigner = ({ currentPageNo, panel, onChange, currentLanguage, supportedLanguages }: {
    panel: FormPanel, onChange: (newPanel: FormElement) => void, currentPageNo: number,
  currentLanguage: PortalEnvironmentLanguage, supportedLanguages: PortalEnvironmentLanguage[]
    }) => {
  return (
    <>
      {panel.elements.map((element, elementIndex) => (
        <QuestionDesigner
          key={elementIndex}
          question={element as Question}
          isNewQuestion={false}
          showName={false}
          showQuestionTypeHeader={false}
          readOnly={false}
          onChange={(newQuestion: Question) => {
            const updatedElements = [...panel.elements]
            updatedElements[elementIndex] = newQuestion
            onChange({
              ...panel,
              elements: updatedElements
            })
          }}
          currentLanguage={currentLanguage}
          supportedLanguages={supportedLanguages}
        />
      ))}
    </>
  )
}
