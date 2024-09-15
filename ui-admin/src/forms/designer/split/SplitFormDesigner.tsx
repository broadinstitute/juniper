import {
  FormContent,
  PortalEnvironmentLanguage
} from '@juniper/ui-core'
import React, { useState } from 'react'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowRightFromBracket, faCaretUp, faPlus, faTrash } from '@fortawesome/free-solid-svg-icons'
import { SplitFormElementDesigner } from './SplitFormElementDesigner'
import { SplitFormTableOfContents } from './SplitFormTableOfContents'
import { PageControls } from './controls/PageControls'
import classNames from 'classnames'
import { InsertElementControls } from './controls/InsertElementControls'
import { useSearchParams } from 'react-router-dom'

/**
 * A split-view form designer that allows editing content on the left and previewing it on the right.
 */
export const SplitFormDesigner = ({ content, onChange, currentLanguage, supportedLanguages }: {
    content: FormContent, onChange: (newContent: FormContent) => void,
    currentLanguage: PortalEnvironmentLanguage, supportedLanguages: PortalEnvironmentLanguage[]
}) => {
  const HIDE_TABLE_OF_CONTENTS_KEY = 'formDesigner.hideTableOfContents'

  const [currentPageNo, setCurrentPageNo] = useState(0)
  const [hideTableOfContents, setHideTableOfContents] = useState(
    localStorage.getItem(HIDE_TABLE_OF_CONTENTS_KEY) === 'true')
  const [searchParams, setSearchParams] = useSearchParams()
  const selectedElementPath = searchParams.get('selectedElementPath') ?? 'pages'

  //TODO this is kinda wonky across page changes, but it works for now
  const setSelectedElementPath = (path: string) => {
    searchParams.set('selectedElementPath', path)
    setSearchParams(searchParams)

    //parses path, i.e.: selectedElementPath=pages%5B0%5D.elements%5B0%5D
    const pathParts = path.split('.')
    const pageElement = pathParts[0]
    const pageElementIndex = parseInt(pageElement.replace('pages[', '').replace(/\]/g, ''))
    setCurrentPageNo(pageElementIndex)

    if (pathParts.length === 1) {
      window.scrollTo(0, 0)
    }

    if (pathParts.length > 1) {
      const elementIndex = parseInt(pathParts[1].replace('elements[', '').replace(/\]/g, ''))
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

  return <div className="container-fluid overflow-scroll">
    <div className="row w-100 mx-0">
      <div className={classNames('px-0 border-start border-end bg-light', hideTableOfContents ? 'd-none' : 'col-3')}
        style={{ overflowY: 'scroll' }}>
        { !hideTableOfContents && <SplitFormTableOfContents
          formContent={content}
          selectedElementPath={selectedElementPath}
          onSelectElement={setSelectedElementPath}
        />}
      </div>
      <div className={classNames('col', hideTableOfContents ? 'col-12' : 'col-9')}>
        <div className="d-flex justify-content-between border border-top-0 rounded-bottom-3 p-2 bg-light">
          <div>
            <Button variant="light" className="border m-1"
              onClick={() => {
                setHideTableOfContents(!hideTableOfContents)
                localStorage.setItem(HIDE_TABLE_OF_CONTENTS_KEY, (!hideTableOfContents).toString())
              }}
              tooltip={hideTableOfContents ? 'Show table of contents' : 'Hide table of contents'}>
              <FontAwesomeIcon icon={faArrowRightFromBracket}
                className={classNames(hideTableOfContents ? '' : 'fa-rotate-180')}/>
            </Button>
            <Button variant="light" className="border m-1"
              tooltip={'Create a new page'}
              onClick={() => {
                const newContent = { ...content }
                newContent.pages.splice(currentPageNo + 1, 0, { elements: [] })
                onChange(newContent)
                setCurrentPageNo(currentPageNo + 1)
              }}>
              <FontAwesomeIcon icon={faPlus}/> Create page
            </Button>
            <Button variant="light" className="border m-1"
              tooltip={'Delete this page'}
              onClick={() => {
                //TODO: add confirmation dialog as this can be quite destructive
                const newContent = { ...content }
                newContent.pages.splice(currentPageNo, 1)
                onChange(newContent)
                setCurrentPageNo(Math.min(currentPageNo, newContent.pages.length - 1))
              }}>
              <FontAwesomeIcon icon={faTrash}/> Delete page
            </Button>
          </div>
          <PageControls
            currentPageNo={currentPageNo}
            content={content}
            setCurrentPageNo={setCurrentPageNo}/>
        </div>
        <InsertElementControls
          formContent={content} onChange={onChange}
          elementIndex={-1} pageIndex={currentPageNo}/>
        {content.pages[currentPageNo] && content.pages[currentPageNo].elements &&
            content.pages[currentPageNo].elements.map((element, elementIndex) => (
              <React.Fragment key={elementIndex}>
                <div id={`element[${elementIndex}]`} key={elementIndex} className={'mx-3'}>
                  <SplitFormElementDesigner currentPageNo={currentPageNo}
                    elementIndex={elementIndex} editedContent={content}
                    element={content.pages[currentPageNo].elements[elementIndex]}
                    currentLanguage={currentLanguage} supportedLanguages={supportedLanguages}
                    onChange={onChange}/>
                </div>
                <InsertElementControls
                  formContent={content} onChange={onChange}
                  elementIndex={elementIndex} pageIndex={currentPageNo}/>
              </React.Fragment>
            ))}
        {content.pages[currentPageNo].elements.length === 0 &&
          <div className="text-muted fst-italic my-5 pb-3 text-center">
            This page is empty. Insert a new question to get started.
          </div>}
        <div className="d-flex justify-content-between m-1 mb-3 border rounded-3 p-2 bg-light">
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
