import {
  FormContent,
  PortalEnvironmentLanguage
} from '@juniper/ui-core'
import React, { useEffect, useState } from 'react'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faCaretUp, faPlus } from '@fortawesome/free-solid-svg-icons'
import { SplitFormElementDesigner } from './SplitFormElementDesigner'
import { baseQuestions } from '../questions/questionTypes'
import { SplitFormTableOfContents } from './SplitFormTableOfContents'
import { PageControls } from './navigation/PageControls'

/**
 * A split-view form designer that allows editing content on the left and previewing it on the right.
 */
export const SplitFormDesigner = ({ content, onChange, currentLanguage, supportedLanguages }: {
    content: FormContent, onChange: (newContent: FormContent) => void,
    currentLanguage: PortalEnvironmentLanguage, supportedLanguages: PortalEnvironmentLanguage[]
}) => {
  const [currentPageNo, setCurrentPageNo] = useState(0)
  // const [currentPageNo2, setCurrentPageNo2] = useState(0)
  const [currentPage, setCurrentPage] = useState(content.pages[0])

  useEffect(() => {
    setCurrentPage(content.pages[currentPageNo])
  }, [currentPageNo])

  console.log(currentPageNo)

  return <div className="container-fluid">
    <div className="row w-100">
      <div className="col-3 border-end" style={{ overflowY: 'scroll' }}>
        <SplitFormTableOfContents
          formContent={content}
          selectedElementPath={'selectedElementPath'}
          onSelectElement={() => {}}
        />
      </div>
      <div className="col-9">
        <div className="d-flex justify-content-between">
          <AddElementControls
            formContent={content} onChange={onChange}
            elementIndex={-1} pageIndex={0}/>
          <PageControls
            currentPageNo={currentPageNo}
            content={content}
            setCurrentPageNo={setCurrentPageNo}/>
        </div>
        {currentPage && currentPage.elements && currentPage.elements.map((element, elementIndex) => (
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

const AddElementControls = ({ formContent, onChange, elementIndex, pageIndex }: {
    formContent: FormContent, onChange: (newContent: FormContent) => void, elementIndex: number, pageIndex: number
    }) => {
  return <div className="d-flex">
    {renderNewElementButton(formContent, onChange, elementIndex, pageIndex, 'question')}
    {renderNewElementButton(formContent, onChange, elementIndex, pageIndex, 'panel')}
  </div>
}
