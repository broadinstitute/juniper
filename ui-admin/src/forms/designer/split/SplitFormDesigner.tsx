import {
  FormContent,
  PortalEnvironmentLanguage
} from '@juniper/ui-core'
import React, { useState } from 'react'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowLeft, faArrowRight, faArrowUp, faPlus } from '@fortawesome/free-solid-svg-icons'
import { SplitFormElementDesigner } from './SplitFormElementDesigner'
import { baseQuestions } from '../questions/questionTypes'

/**
 * A split-view form designer that allows editing content on the left and previewing it on the right.
 */
export const SplitFormDesigner = ({ content, onChange, currentLanguage, supportedLanguages }: {
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
    <div className="d-flex">
      {renderNewElementButton(content, onChange, -1, currentPageNo, 'question')}
      {renderNewElementButton(content, onChange, -1, currentPageNo, 'panel')}
    </div>
    <>
      {currentPage && currentPage.elements && currentPage.elements.map((element, elementIndex) => (
        <div key={elementIndex} className="container">
          <SplitFormElementDesigner currentPageNo={currentPageNo}
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
      <Button className="border m-1" variant="light" onClick={() => handleScrollToTop()}>
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
          title: '',
          type: 'panel',
          elements: []
        } :
          baseQuestions['text']
        )
        onChange(newContent)
      }}>
      <FontAwesomeIcon icon={faPlus}/> Insert {elementType}
    </Button>
  </div>
}
