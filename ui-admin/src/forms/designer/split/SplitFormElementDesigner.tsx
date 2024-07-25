import {
  FormContent,
  FormElement,
  PortalEnvironmentLanguage,
  Question,
  surveyJSModelFromFormContent
} from '@juniper/ui-core'
import React, { memo, useState } from 'react'
import { IconButton } from 'components/forms/Button'
import { faCode } from '@fortawesome/free-solid-svg-icons'
import { ListElementController } from 'portal/siteContent/designer/components/ListElementController'
import { Survey as SurveyComponent } from 'survey-react-ui'
import { isEqual } from 'lodash'
import { FormElementEditor } from './FormElementEditor'
import { FormElementJsonEditor } from './FormElementJsonEditor'

/* Note that this component is memoized using React.memo
 * Since survey pages can contain many elements, we need to be mindful of
 * how many times we re-render these components. Since the parent component state
 * is updated with every keystroke, we memoize this to minimize the number
 * of re-renders that take place. The SurveyComponent from SurveyJS in particular
 * is sluggish when undergoing many simultaneous re-renders.
 */

export const SplitFormElementDesigner = memo(({
  elementIndex, element, currentPageNo, editedContent, onChange, currentLanguage, supportedLanguages
}: {
    elementIndex: number, element: FormElement, currentPageNo: number,
    currentLanguage: PortalEnvironmentLanguage, supportedLanguages: PortalEnvironmentLanguage[],
    editedContent: FormContent, onChange: (newContent: FormContent) => void
}) => {
  const [showJsonEditor, setShowJsonEditor] = useState(false)

  // Chop the survey down to just the specific question that we're editing, so we can display
  // a preview using the SurveyJS survey component.
  const surveyFromQuestion = {
    title: 'Question Preview',
    pages: [{ elements: [element] }],
    questionTemplates: editedContent.questionTemplates
  }
  const surveyModel = surveyJSModelFromFormContent(surveyFromQuestion)

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
        <FormElementEditor
          element={element}
          elementIndex={elementIndex}
          currentPageNo={currentPageNo}
          editedContent={editedContent}
          onChange={onChange}
          currentLanguage={currentLanguage}
          supportedLanguages={supportedLanguages}
        /> :
        <FormElementJsonEditor
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
  // by default, which is why we have this custom propsAreEqual that uses lodash isEqual, which does a deep comparison.
  return isEqual(prevProps.element, nextProps.element)
})

SplitFormElementDesigner.displayName = 'SplitFormElementDesigner'
