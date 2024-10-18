import React from 'react'
import { render, screen } from '@testing-library/react'
import { withStateResetOnEnvChange } from './withStateResetOnEnvChange'
import { mockStudyEnvContext } from 'test-utils/mocking-utils'

const MockComponent = ({ text }: { text: string }) => {
  return <div data-testid="mock-component">{text}</div>
}

const WrappedComponent = withStateResetOnEnvChange(MockComponent)

test('resets component state when studyEnvContext changes', () => {
  const { rerender } = render(
    <WrappedComponent studyEnvContext={{ ...mockStudyEnvContext(), currentEnvPath: 'mystudy/sandbox' }} text="sandbox"/>
  )

  expect(screen.getByTestId('mock-component')).toHaveTextContent('sandbox')

  rerender(
    <WrappedComponent studyEnvContext={{ ...mockStudyEnvContext(), currentEnvPath: 'mystudy/live' }} text="live"/>
  )

  expect(screen.getByTestId('mock-component')).toHaveTextContent('live')
})
