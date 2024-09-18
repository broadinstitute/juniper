import {
  FormContent,
  PortalEnvironmentLanguage
} from '@juniper/ui-core'
import React, { useEffect, useState } from 'react'
import { Button } from 'components/forms/Button'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faArrowRightFromBracket, faCaretUp } from '@fortawesome/free-solid-svg-icons'
import { PageNavigationControls } from './controls/PageNavigationControls'
import classNames from 'classnames'
import { InsertElementControls } from './controls/InsertElementControls'
import { useSearchParams } from 'react-router-dom'
import { FormTableOfContents } from 'forms/FormTableOfContents'
import { handleScrollToTop, scrollToElement } from '../utils/formDesignerUtils'
import { PageEditControls } from './controls/PageEditControls'
import { FormPageContent } from './FormPageContent'

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

  const setSelectedElementPath = (path: string) => {
    searchParams.set('selectedElementPath', path)
    setSearchParams(searchParams)
    scrollToElement(path, setCurrentPageNo)
  }

  useEffect(() => {
    if (selectedElementPath && selectedElementPath !== 'pages') {
      scrollToElement(selectedElementPath, setCurrentPageNo)
    }
  }, [])

  return <div className="container-fluid overflow-scroll">
    <div className="row w-100 mx-0">
      <div style={{ overflowY: 'scroll' }}
        className={classNames('px-0 bg-white', hideTableOfContents ? 'd-none' : 'col-3')}>
        { !hideTableOfContents &&
            <FormTableOfContents
              formContent={content}
              selectedElementPath={selectedElementPath}
              onSelectElement={setSelectedElementPath}
            />
        }
      </div>
      <div className={classNames('col', hideTableOfContents ? 'col-12' : 'col-9')}>
        <div className="d-flex justify-content-between border rounded-3 mt-2 mx-1 p-2 bg-light">
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
            <PageEditControls content={content} onChange={onChange}
              currentPageNo={currentPageNo} setCurrentPageNo={setCurrentPageNo}/>
          </div>
          <PageNavigationControls
            currentPageNo={currentPageNo}
            content={content}
            setCurrentPageNo={setCurrentPageNo}/>
        </div>
        <InsertElementControls
          formContent={content} onChange={onChange}
          elementIndex={-1} pageIndex={currentPageNo}/>
        <FormPageContent
          content={content}
          currentPageNo={currentPageNo}
          currentLanguage={currentLanguage}
          supportedLanguages={supportedLanguages}
          onChange={onChange}/>
        <div className="d-flex justify-content-between m-1 mb-3 border rounded-3 p-2 bg-light">
          <Button variant="light" className="border m-1"
            onClick={() => handleScrollToTop()}>
            <FontAwesomeIcon icon={faCaretUp}/> Scroll to top
          </Button>
          <PageNavigationControls
            currentPageNo={currentPageNo}
            content={content}
            setCurrentPageNo={setCurrentPageNo}/>
        </div>
      </div>
    </div>
  </div>
}
