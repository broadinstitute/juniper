import { FormElement, FormPanel, PortalEnvironmentLanguage, Question } from '@juniper/ui-core'
import { QuestionDesigner } from '../QuestionDesigner'
import React, { useId } from 'react'
import { CollapsibleSectionButton } from '../../../portal/siteContent/designer/components/CollapsibleSectionButton'
import { TextInput } from '../../../components/forms/TextInput'

/**
 *
 */
export const PanelDesigner = ({ currentPageNo, panel, onChange, currentLanguage, supportedLanguages }: {
    panel: FormPanel, onChange: (newPanel: FormElement) => void, currentPageNo: number,
    currentLanguage: PortalEnvironmentLanguage, supportedLanguages: PortalEnvironmentLanguage[]
}) => {
  return (
    <>
      <TextInput className={'mb-2'} label={'Title'} value={panel.title}
        onChange={title => onChange({ ...panel, title })}/>
      <div>
        {panel.elements.map((element, elementIndex) => {
          const questionLabelId = useId()

          return <div key={elementIndex}>
            <CollapsibleSectionButton
              targetSelector={`#${questionLabelId}`} sectionLabel={element.name}/>
            <div className="collapse hide" id={questionLabelId}>
              <QuestionDesigner
                key={elementIndex}
                question={element as Question}
                isNewQuestion={false}
                showName={false}
                showQuestionTypeHeader={false}
                readOnly={false}
                onChange={(newQuestion: Question) => {
                  const updatedElements = [...panel.elements]
                  updatedElements[elementIndex] = newQuestion
                  onChange({
                    ...panel,
                    elements: updatedElements
                  })
                }}
                currentLanguage={currentLanguage}
                supportedLanguages={supportedLanguages}
              />
            </div>
          </div>
        })}
      </div>
    </>
  )
}
