import React from 'react'
import { setupRouterTest } from 'test-utils/router-testing-utils'
import { render, screen } from '@testing-library/react'
import { mockEnrollee, mockParticipantTask, mockSurvey } from 'test-utils/test-participant-factory'
import StudyResearchTasks from './StudyResearchTasks'
import { MockI18nProvider, mockTextsDefault } from '@juniper/ui-core'
import { TasksWithSurveys } from '../api/api'

describe('HubPage', () => {
  it('renders tasks with consent and required surveys first', () => {
    const enrollee = mockEnrollee()

    const tasksWithForms: TasksWithSurveys = {
      surveyTasks: [
        {
          task: {
            ...mockParticipantTask('SURVEY', 'NEW'),
            blocksHub: false,
            taskOrder: 5
          },
          form: {
            ...mockSurvey('test_survey1'),
            name: 'Optional Survey'
          }
        },
        {
          task: {
            ...mockParticipantTask('SURVEY', 'NEW'),
            taskOrder: 1
          },
          form: {
            ...mockSurvey('test_survey2'),
            name: 'Required Survey'
          }
        }
      ],
      consentTasks: [
        {
          task: mockParticipantTask('CONSENT', 'NEW'),
          form: {
            ...mockSurvey('test_consent'),
            name: 'Our consent form'
          }
        }
      ],
      outreachTasks: [
        {
          task: mockParticipantTask('OUTREACH', 'COMPLETE'),
          form: {
            ...mockSurvey('test_outreach'),
            name: 'Outreach Survey'
          }
        }
      ]
    }

    const { RoutedComponent } = setupRouterTest(
      <MockI18nProvider mockTexts={mockTextsDefault}>
        <StudyResearchTasks
          enrollee={enrollee}
          participantTasks={[]}
          studyShortcode={'study1'}
          consentTasks={tasksWithForms.consentTasks}
          surveyTasks={tasksWithForms.surveyTasks}
        />
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
