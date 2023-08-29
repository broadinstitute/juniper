import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faChevronDown, faChevronUp, faPlus, faTimes } from '@fortawesome/free-solid-svg-icons'
import React, { useId } from 'react'

import { CheckboxQuestion, DropdownQuestion, RadiogroupQuestion } from '@juniper/ui-core'

import { Button, IconButton } from 'components/forms/Button'
import { TextInput } from 'components/forms/TextInput'
import { getValueForChoice } from 'util/pearlSurveyUtils'

type QuestionWithChoices = CheckboxQuestion | DropdownQuestion | RadiogroupQuestion

type ChoicesListProps = {
  question: QuestionWithChoices
  readOnly: boolean
  onChange: (newValue: QuestionWithChoices) => void
}

/** UI for editing the list of choices for a question. */
export const ChoicesList = (props: ChoicesListProps) => {
  const { question, readOnly, onChange } = props

  const labelId = useId()

  return (
    <div className="mb-3">
      <p className="mb-2" id={labelId}>Choices</p>
      <ol aria-labelledby={labelId} className="list-group mb-1">
        {question.choices.map((choice, i) => {
          return (
            <li
              key={i}
              className="list-group-item d-flex"
            >
              <div
                className="flex-grow-1"
                style={{
                  display: 'grid',
                  grid: 'auto-flow / max-content auto',
                  gridColumnGap: '0.5rem',
                  gridRowGap: '1rem',
                  alignItems: 'center',
                  height: 'fit-content'
                }}
              >
                <TextInput
                  className="form-control"
                  disabled={readOnly}
                  label="Text"
                  labelClassname="mb-0"
                  value={choice.text}
                  onChange={value => {
                    onChange({
                      ...question,
                      choices: [
                        ...question.choices.slice(0, i),
                        { text: value, value: getValueForChoice(value) },
                        ...question.choices.slice(i + 1)
                      ]
                    })
                  }}
                />
                <TextInput
                  disabled={readOnly}
                  label="Value"
                  labelClassname="mb-0"
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
              </div>

              <div className="d-flex flex-column justify-content-between ms-2">
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
                  className="btn btn-light"
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
              </div>

              <div className="d-flex flex-column justify-content-between flex-shrink-0 ms-2">
                <IconButton
                  aria-label="Delete this choice"
                  disabled={readOnly}
                  icon={faTimes}
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
              </div>
            </li>
          )
        })}
      </ol>

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
