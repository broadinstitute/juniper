import {
  FormContent,
  PortalEnvironmentLanguage
} from '@juniper/ui-core'
import React, { useState } from 'react'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowRightFromBracket, faCaretUp, faPlus } from '@fortawesome/free-solid-svg-icons'
import { SplitFormElementDesigner } from './SplitFormElementDesigner'
import { baseQuestions } from '../questions/questionTypes'
import { SplitFormTableOfContents } from './SplitFormTableOfContents'
import { PageControls } from './navigation/PageControls'
import classNames from 'classnames'

/**
 * A split-view form designer that allows editing content on the left and previewing it on the right.
 */
export const SplitFormDesigner = ({ content, onChange, currentLanguage, supportedLanguages }: {
    content: FormContent, onChange: (newContent: FormContent) => void,
    currentLanguage: PortalEnvironmentLanguage, supportedLanguages: PortalEnvironmentLanguage[]
}) => {
  const [currentPageNo, setCurrentPageNo] = useState(0)
  const [showTableOfContents, setShowTableOfContents] = useState(true)

  return <div className="container-fluid">
    <div className="row w-100">
      <div className={classNames('border-end', showTableOfContents ? 'col-3' : 'd-none')}
        style={{ overflowY: 'scroll' }}>
        { showTableOfContents && <SplitFormTableOfContents
          formContent={content}
          selectedElementPath={'selectedElementPath'}
          onSelectElement={() => {}}
        />}
      </div>
      <div className={classNames('col', showTableOfContents ? 'col-9' : 'col-12')}>
        <div className="d-flex justify-content-between">
          <Button variant="light" className="border m-1"
            onClick={() => setShowTableOfContents(!showTableOfContents)}
            tooltip={showTableOfContents ? 'Hide table of contents' : 'Show table of contents'}>
            <FontAwesomeIcon icon={faArrowRightFromBracket}
              className={classNames(showTableOfContents ? 'fa-rotate-180' : '')}/>
          </Button>
          {/*<Button variant="light" className="border m-1"*/}
          {/*  onClick={() => {*/}
          {/*    const newContent = { ...content }*/}
          {/*    newContent.pages.splice(currentPageNo + 1, 0, { elements: [] })*/}
          {/*    onChange(newContent)*/}
          {/*    setCurrentPageNo(currentPageNo + 1)*/}
          {/*  }}>*/}
          {/*  <FontAwesomeIcon icon={faPlus}/> Insert page*/}
          {/*</Button>*/}
          <PageControls
            currentPageNo={currentPageNo}
            content={content}
            setCurrentPageNo={setCurrentPageNo}/>
        </div>
        <AddElementControls
          formContent={content} onChange={onChange}
          elementIndex={-1} pageIndex={0}/>
        {content.pages[currentPageNo] && content.pages[currentPageNo].elements &&
            content.pages[currentPageNo].elements.map((element, elementIndex) => (
              <div key={elementIndex} className="container">
                <SplitFormElementDesigner currentPageNo={currentPageNo}
                  elementIndex={elementIndex} editedContent={content}
                  element={content.pages[currentPageNo].elements[elementIndex]}
                  currentLanguage={currentLanguage} supportedLanguages={supportedLanguages}
                  onChange={onChange}/>
                <AddElementControls
                  formContent={content} onChange={onChange}
                  elementIndex={elementIndex} pageIndex={currentPageNo}/>
              </div>
            ))}
        {content.pages[currentPageNo].elements.length === 0 &&
          <div className="alert alert-secondary">
            This page is empty. Insert a new question or panel to get started.
          </div>}
        <div className="d-flex justify-content-between mb-3">
          <Button variant="light" className="border m-1"
            onClick={() => window.scrollTo(0, 0)}>
            <FontAwesomeIcon icon={faCaretUp}/> Scroll to top
          </Button>
          <PageControls
            currentPageNo={currentPageNo}
            content={content}
            setCurrentPageNo={setCurrentPageNo}/>
        </div>
      </div>
    </div>
  </div>
}

type ElementType = 'question' | 'panel'

const renderNewElementButton = (formContent: FormContent, onChange: (newContent: FormContent) => void,
  elementIndex: number, pageIndex: number, elementType: ElementType) => {
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

const AddElementControls = ({ formContent, onChange, elementIndex, pageIndex }: {
  formContent: FormContent, onChange: (newContent: FormContent) => void, elementIndex: number, pageIndex: number
}) => {
  return (
    <div className="d-flex">
      {renderNewElementButton(formContent, onChange, elementIndex, pageIndex, 'question')}
      {renderNewElementButton(formContent, onChange, elementIndex, pageIndex, 'panel')}
    </div>
  )
}
