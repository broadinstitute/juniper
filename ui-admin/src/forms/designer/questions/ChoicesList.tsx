import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronDown, faChevronUp, faPlus, faTimes } from '@fortawesome/free-solid-svg-icons'
import React, { useId } from 'react'

import { CheckboxQuestion, DropdownQuestion, RadiogroupQuestion } from '@juniper/ui-core'

import { Button, IconButton } from 'components/forms/Button'
import { TextInput } from 'components/forms/TextInput'
import { getValueForChoice, getI18nSurveyElement } from 'util/juniperSurveyUtils'

type QuestionWithChoices = CheckboxQuestion | DropdownQuestion | RadiogroupQuestion

type ChoicesListProps = {
  question: QuestionWithChoices
  isNewQuestion: boolean
  readOnly: boolean
  onChange: (newValue: QuestionWithChoices) => void
}

/** UI for editing the list of choices for a question. */
export const ChoicesList = (props: ChoicesListProps) => {
  const { question, isNewQuestion, readOnly, onChange } = props

  const labelId = useId()
  if (!question.choices) {
    return null
  }
  return (
    <div className="mb-3 mt-4">
      <p className="mb-2 fw-semibold" id={labelId}>Choices</p>
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
                isNewQuestion && choice.value == getValueForChoice(getI18nSurveyElement(choice.text))

            return (
              <tr key={i}>
                <td>
                  <TextInput
                    className="form-control pe-3"
                    disabled={readOnly}
                    required={true}
                    aria-label="text"
                    value={getI18nSurveyElement(choice.text)}
                    onChange={value => {
                      onChange({
                        ...question,
                        choices: [
                          ...question.choices.slice(0, i), {
                            text: value, value: enableAutoFillValue ?
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
        <FontAwesomeIcon icon={faPlus} /> Add a choice
      </Button>
    </div>
  )
}
