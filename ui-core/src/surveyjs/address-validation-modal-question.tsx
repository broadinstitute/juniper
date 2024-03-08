/**
 * A SurveyJS question that enables address validation.
 */
import React from 'react'
import { ElementFactory, Question, Serializer } from 'survey-core'
import { SurveyQuestionElementBase } from 'survey-react-ui'
import { AddressValidationResult, MailingAddress } from '../types/address'
import { SuggestBetterAddressModal } from '../components/SuggestBetterAddressModal'
import { isEmpty, isNil } from 'lodash'
import Modal from 'react-bootstrap/Modal'
import { ModalProps } from 'react-bootstrap'

export type AddressValidationQuestionValue = {
  inputAddress: MailingAddress,
  canceledSuggestedAddress: boolean,
  acceptedSuggestedAddress: boolean,
  modalDismissed: boolean,
  addressValidationResult: AddressValidationResult
}

const AddressValidationType = 'addressvalidation'
export class QuestionAddressValidationModel extends Question {
  getType() {
    return AddressValidationType
  }

  get street1() {
    return this.getPropertyValue('street1')
  }

  set street1(val: string) {
    this.setPropertyValue('street1', val)
  }

  get street2() {
    return this.getPropertyValue('street2')
  }

  set street2(val: string) {
    this.setPropertyValue('street2', val)
  }

  get city() {
    return this.getPropertyValue('city')
  }

  set city(val: string) {
    this.setPropertyValue('city', val)
  }

  // 'state' is already used on questions :(
  get stateProvince() {
    return this.getPropertyValue('state')
  }

  set stateProvince(val: string) {
    this.setPropertyValue('state', val)
  }

  get country() {
    return this.getPropertyValue('country')
  }

  set country(val: string) {
    this.setPropertyValue('country', val)
  }

  get postalCode() {
    return this.getPropertyValue('postalCode')
  }

  set postalCode(val: string) {
    this.setPropertyValue('postalCode', val)
  }
}

ElementFactory.Instance.registerElement(AddressValidationType, name => {
  return new QuestionAddressValidationModel(name)
})

Serializer.addClass(
  AddressValidationType,
  [{
    name: 'street1',
    category: 'general'
  }, {
    name: 'street2',
    category: 'general'
  }, {
    name: 'city',
    category: 'general'
  }, {
    name: 'stateProvince',
    category: 'general'
  }, {
    name: 'postalCode',
    category: 'general'
  }, {
    name: 'country',
    category: 'general'
  }],
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

  get baseModal(): React.ElementType<ModalProps> {
    return Modal
  }

  accept(addr: MailingAddress) {
    this.tryUpdateOtherValue(this.question.street1, addr.street1)
    this.tryUpdateOtherValue(this.question.street2, addr.street2)
    this.tryUpdateOtherValue(this.question.city, addr.city)
    this.tryUpdateOtherValue(this.question.stateProvince, addr.state)
    this.tryUpdateOtherValue(this.question.postalCode, addr.postalCode)
    this.tryUpdateOtherValue(this.question.country, addr.country)
  }

  tryUpdateOtherValue(questionName: string, value: string) {
    if (isEmpty(questionName)) {
      return
    }

    const question = this.questionBase.survey.getQuestionByName(questionName)

    if (question) {
      question.value = value
    }
  }


  renderElement() {
    if (isNil(this.value?.addressValidationResult)) {
      return null
    }

    return (
      <>
        {(this.value.addressValidationResult?.suggestedAddress
            && !this.value.canceledSuggestedAddress
            && !this.value.acceptedSuggestedAddress) &&
            <SuggestBetterAddressModal
              inputtedAddress={this.value.inputAddress}
              improvedAddress={this.value.addressValidationResult.suggestedAddress}
              accept={() => {
                this.question.value = {
                  ...this.value,
                  modalDismissed: true,
                  acceptedSuggestedAddress: true
                }
                if (this.value.addressValidationResult.suggestedAddress) {
                  this.accept(this.value.addressValidationResult.suggestedAddress)
                }
              }}
              reject={() => {
                this.question.value = {
                  ...this.value,
                  modalDismissed: true,
                  canceledSuggestedAddress: true
                }
              }}
              onDismiss={() => {
                this.question.value = {
                  ...this.value,
                  modalDismissed: true,
                  canceledSuggestedAddress: true
                }
              }}
              ModalComponent={this.baseModal}
            />}
      </>
    )
  }
}
