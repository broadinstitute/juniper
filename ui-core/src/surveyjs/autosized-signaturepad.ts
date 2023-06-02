import { throttle } from 'lodash'

import {
  CustomWidgetCollection,
  IQuestion,
  QuestionCustomWidget,
  QuestionSignaturePadModel
} from 'survey-core'


// eslint-disable-next-line max-len
// https://surveyjs.io/survey-creator/documentation/customize-question-types/create-custom-widgets#add-functionality-into-existing-question
const autosizedSignaturePadWidget: Partial<QuestionCustomWidget> = {
  name: 'autosized_signaturepad',
  // SurveyJS calls this for every question to check if this widget should apply.
  isFit: (question: IQuestion) => question.getType() === 'signaturepad',
  // Extend default render, do not replace.
  isDefaultRender: true,
  afterRender: (question: QuestionSignaturePadModel, el: HTMLElement) => {
    const resizeSignaturePad = throttle(() => {
      const { width } = el.getBoundingClientRect()
      question.signatureWidth = width
    }, 150)

    window.addEventListener('resize', resizeSignaturePad)
    question.autosizedSignaturePadRemoveResizeListener = () => {
      window.removeEventListener('resize', resizeSignaturePad)
    }

    const { width } = el.getBoundingClientRect()
    question.signatureWidth = width

    // If no signature has been entered, re-center "Sign here" placeholder.
    if (!question.value) {
      setTimeout(() => {
        question.value = ''
        question.clearValue()
      }, 0)
    }
  },
  willUnmount: (question: QuestionSignaturePadModel) => {
    question.autosizedSignaturePadRemoveResizeListener?.()
  }
}

CustomWidgetCollection.Instance.add(autosizedSignaturePadWidget)
