import {
  FormContent,
  FormElement,
  PortalEnvironmentLanguage,
  Question,
  surveyJSModelFromFormContent
} from '@juniper/ui-core'
import React, {
  memo,
  useState
} from 'react'
import { Survey as SurveyComponent } from 'survey-react-ui'
import { isEqual } from 'lodash'
import { FormElementEditor } from './FormElementEditor'
import { FormElementJsonEditor } from './FormElementJsonEditor'
import { FormElementOptions } from './controls/FormElementOptions'

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

  surveyModel.progressBarType = 'off'
  surveyModel.showInvisibleElements = true
  surveyModel.showQuestionNumbers = false
  surveyModel.locale = currentLanguage.languageCode

  return <div key={elementIndex} className="row">
    <div className="col-md-6 px-3 rounded-start-3 border border-end-0 bg-light-subtle">
      <FormElementOptions
        showJsonEditor={showJsonEditor}
        setShowJsonEditor={setShowJsonEditor}
        elementIndex={elementIndex}
        element={element}
        currentPageNo={currentPageNo}
        editedContent={editedContent}
        onChange={onChange}
      />
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
    <div className="col-md-6 rounded-end-3 border survey-hide-complete" style={{ backgroundColor: '#f3f3f3' }}>
      <SurveyComponent model={surveyModel} readOnly={false}/>
    </div>
  </div>
}, (prevProps, nextProps) => {
  // Only re-render if the question has changed. Note that React.memo only does a shallow object comparison
  // by default, which is why we have this custom propsAreEqual that uses lodash isEqual, which does a deep comparison.
  return isEqual(prevProps.element, nextProps.element) && isEqual(prevProps.currentLanguage, nextProps.currentLanguage)
})

SplitFormElementDesigner.displayName = 'SplitFormElementDesigner'
