import { render, screen } from '@testing-library/react'
import React from 'react'
import CreateNotificationConfigModal from './CreateNotificationConfigModal'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { select } from 'react-select-event'

describe('CreateNotificationConfigModal', () => {
  test('renders type and event options', async () => {
    const { RoutedComponent } = setupRouterTest(<CreateNotificationConfigModal
      studyEnvParams={{ studyShortcode: 'testStudy', envName: 'irb', portalShortcode: 'testPortal' }}
      onDismiss={jest.fn()} onCreate={jest.fn()}/>)
    render(RoutedComponent)
    // confirm options are branched based on the config type
    await select(screen.getByLabelText('Notification type'), 'Event')
    expect(screen.queryByLabelText('Task type')).not.toBeInTheDocument()
    expect(screen.getByLabelText('Event name')).toBeInTheDocument()

    await select(screen.getByLabelText('Notification type'), 'Task reminder')
    expect(screen.getByLabelText('Task type')).toBeInTheDocument()
    expect(screen.queryByLabelText('Event name')).not.toBeInTheDocument()
  })
})
