/**
 * A SurveyJS question that renders a multiple selection combobox.
 * This provides similar functionality as the "tagbox" in https://github.com/surveyjs/custom-widgets.
 * However, this virtualizes the options list to support mahy more
 * options while remaining performant.
 */
import React from 'react'
import { ElementFactory, Question, Serializer } from 'survey-core'
import { ReactQuestionFactory, SurveyQuestionElementBase } from 'survey-react-ui'
import { AddressValidationResult, MailingAddress } from 'src/types/address'

export type AddressValidationQuestionValue = {
  inputAddress: MailingAddress,
  canceledSuggestedAddress: boolean,
  addressValidationResult: AddressValidationResult
}

const AddressValidationType = 'addressvalidation'
export class QuestionAddressValidationModel extends Question {
  getType() {
    return AddressValidationType
  }
}

ElementFactory.Instance.registerElement(AddressValidationType, name => {
  return new QuestionAddressValidationModel(name)
})

Serializer.addClass(
  AddressValidationType,
  [],
  () => new QuestionAddressValidationModel(''),
  'question'
)


export class SurveyQuestionMultipleCombobox extends SurveyQuestionElementBase {
  get question() {
    return this.questionBase
  }

  get value(): AddressValidationQuestionValue | undefined {
    return this.question.value
  }

  renderElement() {
    return (
      <>
        {this.value && <p>show MODAL</p>}
      </>
    )
  }
}

ReactQuestionFactory.Instance.registerQuestion(AddressValidationType, props => {
  return React.createElement(SurveyQuestionMultipleCombobox, props)
})
