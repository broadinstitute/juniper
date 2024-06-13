import { render, screen } from '@testing-library/react'
import React from 'react'

import { getDisplayValue, ItemDisplay } from './SurveyFullDataView'
import { Question } from 'survey-core'
import { Answer } from '@juniper/ui-core/build/types/forms'


describe('getDisplayValue', () => {
  it('renders a plaintext value', async () => {
    const question: Question = { isVisible: true, getType: () => 'text' } as Question
    const answer: Answer = { stringValue: 'test123', questionStableId: 'testQ' } as Answer
    render(<span>{getDisplayValue(answer, question)}</span>)
    expect(screen.getByText('test123')).toBeTruthy()
  })

  it('renders a choice value', async () => {
    const question: Question = {
      isVisible: true,
      getType: () => 'radiogroup',
      choices: [{
        text: 'option 1', value: 'option1Val'
      }, {
        text: 'option 2', value: 'option2Val'
      }]
    } as unknown as Question
    const answer: Answer = { stringValue: 'option2Val', questionStableId: 'testQ' } as Answer
    render(<span>{getDisplayValue(answer, question)}</span>)
    expect(screen.getByText('option 2')).toBeTruthy()
  })

  it('renders a free text other description', async () => {
    const question: Question = {
      isVisible: true,
      getType: () => 'radiogroup',
      choices: [{
        text: 'option 1', value: 'option1Val'
      }, {
        text: 'option 2', value: 'option2Val'
      }]
    } as unknown as Question
    const answer: Answer = {
      stringValue: 'option2Val', questionStableId: 'testQ',
      otherDescription: 'more detail'
    } as Answer
    render(<span>{getDisplayValue(answer, question)}</span>)
    expect(screen.getByText('option 2 - more detail')).toBeTruthy()
  })

  it('renders a choice array value', async () => {
    const question: Question = {
      isVisible: true,
      getType: () => 'checkbox',
      choices: [{
        text: 'option 1', value: 'option1Val'
      }, {
        text: 'option 2', value: 'option2Val'
      }, {
        text: 'option 3', value: 'option3Val'
      }, {
        text: 'option 4', value: 'option4Val'
      }]
    } as unknown as Question
    const answer: Answer = {
      objectValue: JSON.stringify(['option2Val', 'option4Val']),
      questionStableId: 'testQ'
    } as Answer
    render(<span>{getDisplayValue(answer, question)}</span>)
    expect(screen.getByText('["option 2","option 4"]')).toBeTruthy()
  })

  it('renders a signaturepad as an image', async () => {
    const question = {
      name: 'testQ', text: 'test question',
      isVisible: true, type: 'signaturepad',
      getType: () => 'signaturepad'
    }
    const answer: Answer = { stringValue: 'data:image/png;base64, test123', questionStableId: 'testQ' } as Answer
    render(<span>{getDisplayValue(answer, question as unknown as Question)}</span>)
    expect(screen.getByRole('img')).toHaveAttribute('src', 'data:image/png;base64, test123')
    expect(screen.queryByText('data:image/png;base64, test123')).not.toBeInTheDocument()
  })
})

describe('ItemDisplay', () => {
  it('renders the language used to answer a question', async () => {
    const question = { name: 'testQ', text: 'test question', isVisible: true, getType: () => 'text' }
    const answer: Answer = {
      stringValue: 'test123',
      questionStableId: 'testQ',
      surveyVersion: 1,
      viewedLanguage: 'es'
    } as Answer
    const answerMap: Record<string, Answer> = {}
    answerMap[answer.questionStableId] = answer
    render(<ItemDisplay
      question={question as unknown as Question}
      answerMap={answerMap}
      surveyVersion={1}
      supportedLanguages={[{ languageCode: 'es', languageName: 'Spanish' }]}
      showFullQuestions={true}/>)

    expect(screen.getByText('(testQ) (Answered in Spanish)')).toBeInTheDocument()
  })

  it('renders correctly if a viewedLanguage is not specified', async () => {
    const question = { name: 'testQ', text: 'test question', isVisible: true, getType: () => 'text' }
    const answer: Answer = {
      stringValue: 'test123',
      questionStableId: 'testQ',
      surveyVersion: 1
    } as Answer
    const answerMap: Record<string, Answer> = {}
    answerMap[answer.questionStableId] = answer
    render(<ItemDisplay
      question={question as unknown as Question}
      answerMap={answerMap}
      surveyVersion={1}
      supportedLanguages={[{ languageCode: 'en', languageName: 'English' }]}
      showFullQuestions={true}/>)

    expect(screen.getByText('(testQ)')).toBeInTheDocument()
  })

  it('does not render a language name if it doesnt match a supported language', async () => {
    const question = { name: 'testQ', text: 'test question', isVisible: true, getType: () => 'text' }
    const answer: Answer = {
      stringValue: 'test123',
      questionStableId: 'testQ',
      surveyVersion: 1,
      viewedLanguage: 'fake'
    } as Answer
    const answerMap: Record<string, Answer> = {}
    answerMap[answer.questionStableId] = answer
    render(<ItemDisplay
      question={question as unknown as Question}
      answerMap={answerMap}
      surveyVersion={1}
      supportedLanguages={[{ languageCode: 'es', languageName: 'Spanish' }]}
      showFullQuestions={true}/>)

    expect(screen.getByText('(testQ)')).toBeInTheDocument()
  })
})
