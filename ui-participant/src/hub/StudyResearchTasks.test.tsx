import React from 'react'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { render, screen } from '@testing-library/react'
import { mockEnrollee, mockParticipantTask } from 'test-utils/test-participant-factory'
import StudyResearchTasks from './StudyResearchTasks'
import { MockI18nProvider, mockTextsDefault } from '@juniper/ui-core'

describe('HubPage', () => {
  it('renders tasks with consent and required surveys first', () => {
    const enrollee = mockEnrollee()
    const participantTasks = [
      {
        ...mockParticipantTask('SURVEY', 'NEW'),
        targetName: 'Optional Survey',
        targetStableId: 'optionalSurvey',
        blocksHub: false,
        taskOrder: 5
      },
      {
        ...mockParticipantTask('CONSENT', 'NEW'),
        targetStableId: 'consentForm',
        targetName: 'Our consent form'
      },
      {
        ...mockParticipantTask('SURVEY', 'NEW'),
        targetStableId: 'requiredSurvey',
        targetName: 'Required Survey',
        taskOrder: 1
      },
      {
        ...mockParticipantTask('OUTREACH', 'COMPLETE'),
        targetName: 'Outreach Survey',
        targetStableId: 'outreachSurvey'
      }
    ]

    const { RoutedComponent } = setupRouterTest(
      <MockI18nProvider mockTexts={{
        ...mockTextsDefault,
        'requiredSurvey:1': 'Required Survey',
        'optionalSurvey:1': 'Optional Survey',
        'outreachSurvey:1': 'Outreach Survey',
        'consentForm:1': 'Our consent form'
      }}>
        <StudyResearchTasks enrollee={enrollee} participantTasks={participantTasks} studyShortcode={'study1'}/>
      </MockI18nProvider>
    )
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
