import {
  FormContent,
  PortalEnvironmentLanguage,
  Question, surveyJSModelFromFormContent
} from '@juniper/ui-core'
import React, { memo, useEffect, useState } from 'react'
import { Button } from '../../components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowLeft, faArrowRight, faArrowUp, faPlus } from '@fortawesome/free-solid-svg-icons'
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
          <QuestionModelComponent currentPageNo={currentPageNo}
            elementIndex={elementIndex} editedContent={content}
            currentLanguage={currentLanguage} supportedLanguages={supportedLanguages}
            onChange={onChange}/>
          {renderAddQuestionButton(content, onChange, elementIndex, currentPageNo)}
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

const renderAddQuestionButton = (formContent: FormContent, onChange: (newContent: FormContent) => void,
  elementIndex: number, pageIndex: number) => {
  return <div className="my-2">
    <Button variant="secondary"
      aria-label={'Insert a new question'}
      tooltip={'Insert a new question'}
      disabled={false}
      onClick={() => {
        const newContent = { ...formContent }
        newContent.pages[pageIndex].elements.splice(elementIndex + 1, 0, {
          type: 'text',
          name: '',
          title: '',
          isRequired: false
        })
        onChange(newContent)
      }}>
      <FontAwesomeIcon icon={faPlus}/> Insert question
    </Button>
  </div>
}

const QuestionModelComponent = ({
  elementIndex, currentPageNo, editedContent, onChange, currentLanguage, supportedLanguages
}: {
    elementIndex: number, currentPageNo: number,
    currentLanguage: PortalEnvironmentLanguage, supportedLanguages: PortalEnvironmentLanguage[],
    editedContent: FormContent, onChange: (newContent: FormContent) => void
}) => {
  const element = editedContent.pages[currentPageNo].elements[elementIndex]

  const [oneQuestionSurvey, setOneQuestionSurvey] = useState<FormContent>({
    title: 'Question Preview',
    pages: [
      {
        elements: [element]
      }
    ],
    questionTemplates: editedContent.questionTemplates
  })

  const [surveyModel, setSurveyModel] = useState(surveyJSModelFromFormContent(oneQuestionSurvey))


  useEffect(() => {
    setOneQuestionSurvey({
      ...oneQuestionSurvey,
      pages: [
        {
          elements: [element]
        }
      ],
      questionTemplates: editedContent.questionTemplates
    })
  }, [element])

  useEffect(() => {
    setSurveyModel(surveyJSModelFromFormContent(oneQuestionSurvey))
  }, [oneQuestionSurvey])


  surveyModel.showInvisibleElements = true
  surveyModel.showQuestionNumbers = false

  return <div key={elementIndex} className="row">
    <div className="col-md-6 p-3 rounded-start-3"
      style={{ backgroundColor: '#f3f3f3', borderRight: '1px solid #fff' }}>
      <div className="d-flex justify-content-between">
        <span className="h5">Edit question</span>
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
      <QuestionDesigner
        question={element as Question}
        isNewQuestion={false}
        showName={false}
        readOnly={false}
        onChange={newQuestion => {
          const newContent = { ...editedContent }
          newContent.pages[currentPageNo].elements[elementIndex] = newQuestion
          onChange(newContent)
        }}
        currentLanguage={currentLanguage}
        supportedLanguages={supportedLanguages}/>
    </div>
    <div className="col-md-6 p-3 rounded-end-3 survey-hide-complete"
      style={{ backgroundColor: '#f3f3f3', borderLeft: '1px solid #fff' }}>
      <QuestionPreview model={surveyModel}/>
    </div>
  </div>
}

const QuestionPreview = memo(({ model }: { model: SurveyModel }) => {
  return (
    <SurveyComponent model={model} readOnly={false} />
  )
})

QuestionPreview.displayName = 'QuestionPreview'
