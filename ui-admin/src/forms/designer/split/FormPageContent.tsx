import { FormContent, PortalEnvironmentLanguage } from '@juniper/ui-core'
import React from 'react'
import { SplitFormElementDesigner } from './SplitFormElementDesigner'
import { InsertElementControls } from './controls/InsertElementControls'

type FormPageContentProps = {
  content: FormContent,
  currentPageNo: number,
  currentLanguage: PortalEnvironmentLanguage,
  supportedLanguages: PortalEnvironmentLanguage[],
  onChange: (newContent: FormContent) => void
}

export const FormPageContent = ({
  content,
  currentPageNo,
  currentLanguage,
  supportedLanguages,
  onChange
}: FormPageContentProps) => {
  return (
    <>
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
        ))
      }
      {content.pages[currentPageNo].elements.length === 0 &&
        <div className="text-muted fst-italic my-5 pb-3 text-center">
          This page is empty. Insert a new question to get started.
        </div>
      }
    </>
  )
}
