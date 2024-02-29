/**
 * A SurveyJS question that renders a multiple selection combobox.
 * This provides similar functionality as the "tagbox" in https://github.com/surveyjs/custom-widgets.
 * However, this virtualizes the options list to support mahy more
 * options while remaining performant.
 */
import React from 'react'
import { ElementFactory, Question, Serializer } from 'survey-core'
import { ReactQuestionFactory, SurveyQuestionElementBase } from 'survey-react-ui'
import { AddressValidationResult, MailingAddress } from '../types/address'
import SuggestBetterAddressModal from '../components/SuggestBetterAddressModal'

export type AddressValidationQuestionValue = {
  inputAddress: MailingAddress,
  canceledSuggestedAddress: boolean,
  acceptedSuggestedAddress: boolean,
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


export class SurveyQuestionAddressValidation extends SurveyQuestionElementBase {
  get question() {
    return this.questionBase
  }

  get value(): AddressValidationQuestionValue {
    return this.question.value
  }

  getPrefix(): string {
    const name = this.question.name
    return name.slice(
      0,
      name.length - 'addressValidation'.length)
  }

  accept(addr: MailingAddress) {
    const prefix = this.getPrefix()
    const survey = this.questionBase.survey

    survey.getQuestionByName(`${prefix}street1`)?.updateValueFromSurvey(addr.street1)
    survey.getQuestionByName(`${prefix}street2`)?.updateValueFromSurvey(addr.street2)
    survey.getQuestionByName(`${prefix}city`)?.updateValueFromSurvey(addr.city)
    survey.getQuestionByName(`${prefix}state`)?.updateValueFromSurvey(addr.state)
    survey.getQuestionByName(`${prefix}postalCode`)?.updateValueFromSurvey(addr.postalCode)
    survey.getQuestionByName(`${prefix}country`)?.updateValueFromSurvey(addr.country)
  }

  renderElement() {
    return (
      <>
        {(this.value.addressValidationResult?.suggestedAddress
            && !this.value.canceledSuggestedAddress
            && !this.value.acceptedSuggestedAddress) &&
            <SuggestBetterAddressModal
              inputtedAddress={this.value.inputAddress}
              improvedAddress={this.value.addressValidationResult.suggestedAddress}
              hasInferredComponents={this.value.addressValidationResult.hasInferredComponents || false}
              accept={() => {
                this.question.value = {
                  ...this.value,
                  acceptedSuggestedAddress: true
                }
                if (this.value.addressValidationResult.suggestedAddress) {
                  this.accept(this.value.addressValidationResult.suggestedAddress)
                }
              }}
              deny={() => {
                this.question.value = {
                  ...this.value,
                  canceledSuggestedAddress: true
                }
              }}
              onDismiss={() => {
                this.question.value = {
                  ...this.value,
                  canceledSuggestedAddress: true
                }
              }}
            />}
      </>
    )
  }
}

ReactQuestionFactory.Instance.registerQuestion(AddressValidationType, props => {
  return React.createElement(SurveyQuestionAddressValidation, props)
})
