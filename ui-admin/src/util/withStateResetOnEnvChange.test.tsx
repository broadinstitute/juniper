import React, { useState } from 'react'
import { render, screen } from '@testing-library/react'
import { withStateResetOnEnvChange } from './withStateResetOnEnvChange'
import { mockStudyEnvContext } from 'test-utils/mocking-utils'
import { userEvent } from '@testing-library/user-event'

const MockComponent = () => {
  const [input, setInput] = useState<string>('')
  return <div data-testid="mock-component">
    <input data-testid="mock-input" value={input} onChange={e => setInput(e.target.value)} />
  </div>
}

const WrappedComponent = withStateResetOnEnvChange(MockComponent)

test('resets component state when studyEnvContext changes', async () => {
  const { rerender } = render(
    <WrappedComponent studyEnvContext={{ ...mockStudyEnvContext(), currentEnvPath: 'mystudy/sandbox' }}/>
  )

  await userEvent.type(screen.getByTestId('mock-input'), 'hello sandbox')
  expect(screen.getByTestId('mock-input')).toHaveValue('hello sandbox')

  rerender(
    <WrappedComponent studyEnvContext={{ ...mockStudyEnvContext(), currentEnvPath: 'mystudy/live' }}/>
  )

  expect(screen.getByTestId('mock-input')).toHaveValue('')
})
