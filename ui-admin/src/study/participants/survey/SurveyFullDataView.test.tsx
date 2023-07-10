import { render, screen } from '@testing-library/react'
import React from 'react'

import { getDisplayValue } from './SurveyFullDataView'
import { Question } from 'survey-core'
import { Answer } from '@juniper/ui-core/build/types/forms'


describe('getDisplayValue', () => {
  it('renders a plaintext value', async () => {
    const question: Question = { isVisible: true } as Question
    const answer: Answer = { stringValue: 'test123', questionStableId: 'testQ' } as Answer
    render(<span>{getDisplayValue(answer, question)}</span>)
    expect(screen.getByText('test123')).toBeTruthy()
  })

  it('renders a choice value', async () => {
    const question: Question = {
      isVisible: true,
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

  it('renders a choice array value', async () => {
    const question: Question = {
      isVisible: true,
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
})
