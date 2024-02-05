/**
 * A SurveyJS question that renders a multiple selection combobox.
 * This provides similar functionality as the "tagbox" in https://github.com/surveyjs/custom-widgets.
 * However, this virtualizes the options list to support mahy more
 * options while remaining performant.
 */
import React from 'react'
import { ElementFactory, Question, Serializer } from 'survey-core'
import { ReactQuestionFactory, SurveyQuestionElementBase } from 'survey-react-ui'

import { MultipleComboBox } from '../components/MultipleCombobox'

const MultipleComboboxType = 'multiplecombobox'

export class QuestionMultipleComboboxModel extends Question {
  getType() {
    return MultipleComboboxType
  }

  get options() {
    return this.getPropertyValue('options')
  }

  set options(value) {
    this.setPropertyValue('options', value)
  }

  get placeholder() {
    return this.getPropertyValue('placeholder')
  }

  set placeholder(value) {
    this.setPropertyValue('placeholder', value)
  }

  get choicesByUrl() {
    return this.getPropertyValue('choicesByUrl')
  }

  set choicesByUrl(value) {
    this.setPropertyValue('choicesByUrl', value)
  }
}

ElementFactory.Instance.registerElement(MultipleComboboxType, name => {
  return new QuestionMultipleComboboxModel(name)
})

Serializer.addClass(
  MultipleComboboxType,
  [{
    name: 'options',
    category: 'general',
    type: 'itemValues'
  }, {
    name: 'placeholder',
    category: 'general'
  }, {
    name: 'choicesByUrl',
    category: 'general'
  }],
  () => new QuestionMultipleComboboxModel(''),
  'question'
)

export type ItemValue = {
  value: string
  text: string
}

const itemToString = (item: ItemValue): string => item.text

export class SurveyQuestionMultipleCombobox extends SurveyQuestionElementBase {
  get question() {
    return this.questionBase
  }

  get value() {
    return this.question.value
  }

  get placeholder() {
    return this.question.placeholder
  }

  get choicesByUrl() {
    return this.question.choicesByUrl
  }

  renderElement() {
    const options: ItemValue[] = this.question.options || []
    const value: string[] = this.question.value || []
    const initialSelectedItems = options.filter(opt => value.includes(opt.value))

    return (
      <MultipleComboBox<ItemValue>
        id={this.question.inputId}
        initialValue={initialSelectedItems}
        itemToString={itemToString}
        onChange={selectedItems => {
          this.question.value = selectedItems.map(item => item.value)
        }}
        choices={options}
        choicesByUrl={this.choicesByUrl?.url}
        placeholder={this.placeholder}
      />
    )
  }
}

ReactQuestionFactory.Instance.registerQuestion(MultipleComboboxType, props => {
  return React.createElement(SurveyQuestionMultipleCombobox, props)
})
