import { FormElement, FormPanel, PortalEnvironmentLanguage, Question } from '@juniper/ui-core'
import React from 'react'
import { TextInput } from 'components/forms/TextInput'
import { FullQuestionDesigner } from './FullQuestionDesigner'
import { Button } from 'components/forms/Button'
import { baseQuestions } from '../questions/questionTypes'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlus } from '@fortawesome/free-solid-svg-icons'
import { ListElementController } from 'portal/siteContent/designer/components/ListElementController'

/**
 *
 */
export const PanelEditor = ({ panel, onChange, currentLanguage, supportedLanguages }: {
    panel: FormPanel, onChange: (newPanel: FormElement) => void,
    currentLanguage: PortalEnvironmentLanguage, supportedLanguages: PortalEnvironmentLanguage[]
}) => {
  return (
    <>
      <TextInput className={'mb-2'} label={'Panel Title'} labelClassname={'mb-0'} value={panel.title}
        placeholder={'Enter an optional panel title'}
        onChange={title => onChange({ ...panel, title })}/>
      <hr className="mb-0"/>
      <div>
        {panel.elements.map((element, elementIndex) => {
          return <div key={elementIndex}>
            <div className="d-flex justify-content-end">
              <div className="d-flex border rounded-3 rounded-top-0 border-top-0 bg-light">
                <ListElementController
                  items={panel.elements}
                  index={elementIndex}
                  updateItems={newItems => {
                    onChange({
                      ...panel,
                      elements: newItems
                    })
                  }}/>
              </div>
            </div>
            <FullQuestionDesigner
              key={elementIndex}
              question={element as Question}
              isNewQuestion={false}
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
            <hr className="mb-0"/>
          </div>
        })}
        <div className="my-2">
          <Button variant="secondary"
            aria-label={`Insert a new question`}
            tooltip={`Insert a new question`}
            disabled={false}
            onClick={() => {
              const updatedElements = [...panel.elements]
              updatedElements.push(baseQuestions['text'])
              onChange({
                ...panel,
                elements: updatedElements
              })
            }}>
            <FontAwesomeIcon icon={faPlus}/> Add question to panel
          </Button>
        </div>
      </div>
    </>
  )
}
