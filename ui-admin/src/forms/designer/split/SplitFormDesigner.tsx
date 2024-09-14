import {
  FormContent,
  PortalEnvironmentLanguage
} from '@juniper/ui-core'
import React, { useState } from 'react'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowRightFromBracket, faCaretUp } from '@fortawesome/free-solid-svg-icons'
import { SplitFormElementDesigner } from './SplitFormElementDesigner'
import { SplitFormTableOfContents } from './SplitFormTableOfContents'
import { PageControls } from './controls/PageControls'
import classNames from 'classnames'
import { NewElementControls } from './controls/NewElementControls'
import { useSearchParams } from 'react-router-dom'

/**
 * A split-view form designer that allows editing content on the left and previewing it on the right.
 */
export const SplitFormDesigner = ({ content, onChange, currentLanguage, supportedLanguages }: {
    content: FormContent, onChange: (newContent: FormContent) => void,
    currentLanguage: PortalEnvironmentLanguage, supportedLanguages: PortalEnvironmentLanguage[]
}) => {
  const [currentPageNo, setCurrentPageNo] = useState(0)
  const [showTableOfContents, setShowTableOfContents] = useState(true)
  const [searchParams, setSearchParams] = useSearchParams()
  const selectedElementPath = searchParams.get('selectedElementPath') ?? 'pages'

  //TODO this is kinda wonky across page changes, but it works for now
  const setSelectedElementPath = (path: string) => {
    searchParams.set('selectedElementPath', path)
    setSearchParams(searchParams)

    //parses path, i.e.: selectedElementPath=pages%5B0%5D.elements%5B0%5D
    const pathParts = path.split('.')
    const pageElement = pathParts[0]
    const pageElementIndex = parseInt(pageElement.replace('pages[', '').replace(']', ''))
    setCurrentPageNo(pageElementIndex)

    if (pathParts.length === 1) {
      window.scrollTo(0, 0)
    }

    if (pathParts.length > 1) {
      const elementIndex = parseInt(pathParts[1].replace('elements[', '').replace(']', ''))
      scrollToElement(elementIndex)
    }
  }

  //scrolls to the element with id `element[${elementIndex}]`
  const scrollToElement = (elementIndex: number) => {
    const element = document.getElementById(`element[${elementIndex}]`)
    if (element) {
      element.scrollIntoView({ behavior: 'auto' })
    }
  }

  return <div className="container-fluid">
    <div className="row w-100">
      <div className={classNames('border-end', showTableOfContents ? 'col-3' : 'd-none')}
        style={{ overflowY: 'scroll' }}>
        { showTableOfContents && <SplitFormTableOfContents
          formContent={content}
          selectedElementPath={selectedElementPath}
          onSelectElement={setSelectedElementPath}
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
        <NewElementControls
          formContent={content} onChange={onChange}
          elementIndex={-1} pageIndex={0}/>
        {content.pages[currentPageNo] && content.pages[currentPageNo].elements &&
            content.pages[currentPageNo].elements.map((element, elementIndex) => (
              <div id={`element[${elementIndex}]`} key={elementIndex} className="container">
                <SplitFormElementDesigner currentPageNo={currentPageNo}
                  elementIndex={elementIndex} editedContent={content}
                  element={content.pages[currentPageNo].elements[elementIndex]}
                  currentLanguage={currentLanguage} supportedLanguages={supportedLanguages}
                  onChange={onChange}/>
                <NewElementControls
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
