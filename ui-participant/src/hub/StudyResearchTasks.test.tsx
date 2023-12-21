import React from 'react'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { render, screen } from '@testing-library/react'
import { mockEnrollee, mockParticipantTask } from 'test-utils/test-participant-factory'
import StudyResearchTasks from './StudyResearchTasks'

describe('HubPage', () => {
  it('renders tasks with consent and required surveys first', () => {
    const enrollee = mockEnrollee()
    const participantTasks = [
      {
        ...mockParticipantTask('SURVEY', 'NEW'),
        targetName: 'Optional Survey',
        blocksHub: false,
        taskOrder: 5
      },
      {
        ...mockParticipantTask('CONSENT', 'NEW'),
        targetName: 'Our consent form'
      },
      {
        ...mockParticipantTask('SURVEY', 'NEW'),
        targetName: 'Required Survey',
        taskOrder: 1
      },
      {
        ...mockParticipantTask('OUTREACH', 'COMPLETE'),
        targetName: 'Outreach Survey'
      }
    ]
    const { RoutedComponent } = setupRouterTest(<StudyResearchTasks
      enrollee={enrollee} participantTasks={participantTasks} studyShortcode={'study1'}
    />)
    render(RoutedComponent)
    expect(screen.getByText('Start Consent')).toBeInTheDocument()
    // compareDocumentPosition returns the relationship of the argument with respect to the element (which is the
    // opposite of how I always read it). So below we are asserting the consent form is preceding the survey
    expect(screen.getByText('Required Survey')
      .compareDocumentPosition(screen.getByText('Our consent form'))).toBe(Node.DOCUMENT_POSITION_PRECEDING)
    expect(screen.getByText('Optional Survey')
      .compareDocumentPosition(screen.getByText('Required Survey'))).toBe(Node.DOCUMENT_POSITION_PRECEDING)

    // confirm it does not show the outreach survey
    expect(screen.queryByText('Outreach Survey')).not.toBeInTheDocument()
  })
})
