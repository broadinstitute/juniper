import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronDown, faChevronUp, faPlus, faTimes } from '@fortawesome/free-solid-svg-icons'
import React, { useId } from 'react'

import { CheckboxQuestion, DropdownQuestion, PortalEnvironmentLanguage, RadiogroupQuestion } from '@juniper/ui-core'

import { Button, IconButton } from 'components/forms/Button'
import { TextInput } from 'components/forms/TextInput'
import { getValueForChoice, i18nSurveyText, updateI18nSurveyText } from 'util/juniperSurveyUtils'
import { CollapsibleSectionButton } from 'portal/siteContent/designer/components/CollapsibleSectionButton'
import { OtherOptionFields } from './OtherOptionFields'
import { CheckboxFields } from './CheckboxFields'

type QuestionWithChoices = CheckboxQuestion | DropdownQuestion | RadiogroupQuestion

type ChoicesListProps = {
  question: QuestionWithChoices
  isNewQuestion: boolean
  readOnly: boolean
  onChange: (newValue: QuestionWithChoices) => void
  currentLanguage: PortalEnvironmentLanguage
  supportedLanguages: PortalEnvironmentLanguage[]
}

/** UI for editing the list of choices for a question. */
export const ChoicesList = (props: ChoicesListProps) => {
  const { question, isNewQuestion, readOnly, onChange, currentLanguage, supportedLanguages } = props

  const labelId = useId()
  if (!question.choices) {
    return null
  }
  return (
    <div className="bg-white rounded-3 p-2 mb-2">
      <CollapsibleSectionButton targetSelector={`#${labelId}`} sectionLabel={`Choices (${question.choices.length})`}/>
      <div className="collapse hide" id={labelId}>
        <table className="ms-2 table">
          <thead>
            <tr>
              <td className="fw-semibold">text</td>
              <td className="fw-semibold">value</td>
              <td></td>
            </tr>
          </thead>
          <tbody>
            {question.choices.map((choice, i) => {
              const enableAutoFillValue: boolean =
                  isNewQuestion && choice.value == getValueForChoice(i18nSurveyText(choice.text,
                    currentLanguage.languageCode))

              return (
                <tr key={i}>
                  <td>
                    <TextInput
                      className="form-control pe-3"
                      disabled={readOnly}
                      required={true}
                      aria-label="text"
                      value={i18nSurveyText(choice.text, currentLanguage.languageCode)}
                      onChange={value => {
                        onChange({
                          ...question,
                          choices: [
                            ...question.choices.slice(0, i), {
                              text: updateI18nSurveyText({
                                valueText: value,
                                oldValue: choice.text,
                                languageCode: currentLanguage.languageCode,
                                supportedLanguages
                              }),
                              value: enableAutoFillValue ?
                                getValueForChoice(value) :
                                question.choices[i].value
                            },
                            ...question.choices.slice(i + 1)
                          ]
                        })
                      }}
                    />
                  </td>
                  <td>
                    <TextInput
                      disabled={readOnly}
                      className="form-control pe-3"
                      required={true}
                      aria-label="value"
                      value={choice.value}
                      onChange={value => {
                        onChange({
                          ...question,
                          choices: [
                            ...question.choices.slice(0, i),
                            { ...question.choices[i], value },
                            ...question.choices.slice(i + 1)
                          ]
                        })
                      }}
                    />
                  </td>

                  <td className="d-flex ms-2">
                    <IconButton
                      aria-label="Move this choice before the previous one"
                      disabled={readOnly || i === 0}
                      icon={faChevronUp}
                      variant="light"
                      onClick={() => {
                        onChange({
                          ...question,
                          choices: [
                            ...question.choices.slice(0, i - 1),
                            question.choices[i],
                            question.choices[i - 1],
                            ...question.choices.slice(i + 1)
                          ]
                        })
                      }}
                    />

                    <IconButton
                      aria-label="Move this choice after the next one"
                      disabled={readOnly || i === question.choices.length - 1}
                      icon={faChevronDown}
                      variant="light"
                      onClick={() => {
                        onChange({
                          ...question,
                          choices: [
                            ...question.choices.slice(0, i),
                            question.choices[i + 1],
                            question.choices[i],
                            ...question.choices.slice(i + 2)
                          ]
                        })
                      }}
                    />
                    <IconButton
                      aria-label="Delete this choice"
                      disabled={readOnly}
                      icon={faTimes}
                      className="ms-2"
                      variant="light"
                      onClick={() => {
                        onChange({
                          ...question,
                          choices: [
                            ...question.choices.slice(0, i),
                            ...question.choices.slice(i + 1)
                          ]
                        })
                      }}
                    />
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
        <Button
          disabled={readOnly}
          variant="secondary"
          onClick={() => {
            onChange({
              ...question,
              choices: [
                ...question.choices,
                { text: '', value: '' }
              ]
            })
          }}
        >
          <FontAwesomeIcon icon={faPlus}/> Add a choice
        </Button>
        <OtherOptionFields
          disabled={readOnly}
          question={question}
          onChange={onChange}
        />
        {
          question.type === 'checkbox' && (
            <CheckboxFields
              disabled={readOnly}
              question={question}
              onChange={onChange}
            />
          )
        }
      </div>
    </div>
  )
}
