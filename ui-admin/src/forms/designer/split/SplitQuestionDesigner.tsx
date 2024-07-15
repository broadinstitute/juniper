import { Textarea } from 'components/forms/Textarea'
import {
  FormContent,
  FormElement,
  FormPanel,
  PortalEnvironmentLanguage,
  Question,
  surveyJSModelFromFormContent
} from '@juniper/ui-core'
import React, { memo, useState } from 'react'
import { IconButton } from 'components/forms/Button'
import { faCode, faLock, faUnlock } from '@fortawesome/free-solid-svg-icons'
import { ListElementController } from 'portal/siteContent/designer/components/ListElementController'
import { QuestionDesigner } from '../QuestionDesigner'
import { PanelDesigner } from './PanelDesigner'
import { Survey as SurveyComponent } from 'survey-react-ui'
import { isEqual } from 'lodash'
import QuestionTypeSelector from './QuestionTypeSelector'
import { TextInput } from 'components/forms/TextInput'

/* Note that this component is memoized using React.memo
 * Since survey pages can contain many questions, we need to be mindful of
 * how many times we re-render these components. Since the parent component state
 * is updated with every keystroke, we memoize this to minimize the number
 * of re-renders that take place. SurveyComponent from SurveyJS in particular
 * is sluggish when undergoing many simultaneous re-renders.
 */

export const SplitQuestionDesigner = memo(({
  elementIndex, element, currentPageNo, editedContent, onChange, currentLanguage, supportedLanguages
}: {
    elementIndex: number, element: FormElement, currentPageNo: number,
    currentLanguage: PortalEnvironmentLanguage, supportedLanguages: PortalEnvironmentLanguage[],
    editedContent: FormContent, onChange: (newContent: FormContent) => void
}) => {
  const [showJsonEditor, setShowJsonEditor] = useState(false)
  const [isStableIdLocked, setIsStableIdLocked] = useState(true)

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
        <>
          {elementType === 'question' ? <>
            <label className={'form-label fw-semibold'}>Stable ID</label>
            <div className="d-flex align-items-center">
              <div className="w-100">
                <TextInput className={'mb-2'} value={(element as Question).name}
                  disabled={isStableIdLocked}
                  onChange={name => {
                    const newContent = { ...editedContent }
                    newContent.pages[currentPageNo].elements[elementIndex] = {
                      ...newContent.pages[currentPageNo].elements[elementIndex],
                      name
                    } as Question
                    onChange(newContent)
                  }}
                />
              </div>
              <IconButton className="mb-2" icon={isStableIdLocked ? faLock : faUnlock}
                aria-label={isStableIdLocked ? 'Unlock stable ID' : 'Lock stable ID'}
                onClick={() => setIsStableIdLocked(!isStableIdLocked)}
              />
            </div>
            {/* @ts-ignore */}
            <QuestionTypeSelector questionType={(element as Question).type}
              onChange={newType => {
                const newContent = { ...editedContent }
                newContent.pages[currentPageNo].elements[elementIndex] = {
                  ...newContent.pages[currentPageNo].elements[elementIndex],
                  type: newType
                } as Question
                onChange(newContent)
              }}
            />
            <QuestionDesigner
              question={element as Question}
              isNewQuestion={false}
              showName={false}
              showQuestionTypeHeader={false}
              readOnly={false}
              onChange={newQuestion => {
                const newContent = { ...editedContent }
                newContent.pages[currentPageNo].elements[elementIndex] = newQuestion
                onChange(newContent)
              }}
              currentLanguage={currentLanguage}
              supportedLanguages={supportedLanguages}/>
          </> :
            <PanelDesigner
              panel={element as FormPanel}
              currentPageNo={currentPageNo}
              onChange={newPanel => {
                const newContent = { ...editedContent }
                newContent.pages[currentPageNo].elements[elementIndex] = newPanel
                onChange(newContent)
              }}
              currentLanguage={currentLanguage}
              supportedLanguages={supportedLanguages}
            />
          }
        </> :
        <QuestionJsonEditor
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

SplitQuestionDesigner.displayName = 'SplitQuestionDesigner'

const QuestionJsonEditor = ({ question, onChange }: {
    question: Question, onChange: (newQuestion: Question) => void
}) => {
  const [editedContent, setEditedContent] = useState(JSON.stringify(question, null, 2))
  return <Textarea
    className="form-control"
    value={editedContent}
    rows={15}
    onChange={updatedContent => {
      try {
        onChange(JSON.parse(updatedContent))
        setEditedContent(updatedContent)
      } catch (e) {
        setEditedContent(updatedContent)
      }
    }}
    label={'Question JSON'}
    infoContent={'Edit the question JSON directly. Learn more about SurveyJS JSON here.'}
  />
}
