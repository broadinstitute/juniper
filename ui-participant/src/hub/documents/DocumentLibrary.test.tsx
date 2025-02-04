import { asMockedFn, MockI18nProvider, setupRouterTest } from '@juniper/ui-core'
import { usePortalEnv } from 'providers/PortalProvider'
import { mockParticipantFile, mockUsePortalEnv } from 'test-utils/test-portal-factory'
import { render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import DocumentLibrary from './DocumentLibrary'
import { useActiveUser } from 'providers/ActiveUserProvider'
import { mockUseActiveUser } from 'test-utils/user-mocking-utils'
import Api from 'api/api'
import { mockParticipantTask } from 'test-utils/test-participant-factory'

jest.mock('providers/PortalProvider', () => ({ usePortalEnv: jest.fn() }))

jest.mock('providers/ActiveUserProvider', () => ({
  useActiveUser: jest.fn()
}))

jest.mock('api/api', () => ({
  listParticipantFiles: jest.fn()
}))

beforeEach(() => {
  asMockedFn(usePortalEnv).mockReturnValue(mockUsePortalEnv())
  asMockedFn(useActiveUser).mockReturnValue(mockUseActiveUser())
})

describe('DocumentLibrary', () => {
  it('renders no documents message', () => {
    const { RoutedComponent } = setupRouterTest(<MockI18nProvider><DocumentLibrary/></MockI18nProvider>)
    render(RoutedComponent)
    expect(screen.getByText('{documentsPageTitle}')).toBeInTheDocument()
    expect(screen.getByText('{documentsListNone}')).toBeInTheDocument()
  })

  it('renders documents', async () => {
    asMockedFn(Api.listParticipantFiles).mockResolvedValue([
      mockParticipantFile('file1.pdf'),
      mockParticipantFile('file2.png')
    ])

    const { RoutedComponent } = setupRouterTest(<MockI18nProvider><DocumentLibrary/></MockI18nProvider>)
    render(RoutedComponent)
    expect(screen.getByText('{documentsPageTitle}')).toBeInTheDocument()
    await waitFor(() => {
      expect(screen.getByText('file1.pdf')).toBeInTheDocument()
      expect(screen.getByText('file2.png')).toBeInTheDocument()
    })
  })

  it('renders document options', async () => {
    asMockedFn(Api.listParticipantFiles).mockResolvedValue([
      mockParticipantFile('file1.pdf')
    ])

    const { RoutedComponent } = setupRouterTest(<MockI18nProvider><DocumentLibrary/></MockI18nProvider>)
    render(RoutedComponent)
    await waitFor(() => {
      expect(screen.getByText('file1.pdf')).toBeInTheDocument()
    })

    expect(screen.getByText('{documentDownloadButton}')).toBeInTheDocument()
  })

  it('renders no associated tasks message', async () => {
    asMockedFn(Api.listParticipantFiles).mockResolvedValue([
      mockParticipantFile('file1.pdf', [])
    ])

    const { RoutedComponent } = setupRouterTest(<MockI18nProvider><DocumentLibrary/></MockI18nProvider>)
    render(RoutedComponent)
    await waitFor(() => {
      expect(screen.getByText('file1.pdf')).toBeInTheDocument()
    })

    expect(screen.getByText('This document is not associated with any tasks')).toBeInTheDocument()
  })

  it('renders associated tasks', async () => {
    asMockedFn(Api.listParticipantFiles).mockResolvedValue([
      mockParticipantFile('file1.pdf', ['taskId1'])
    ])

    asMockedFn(useActiveUser).mockReturnValue({
      ...mockUseActiveUser(),
      enrollees: [{
        ...mockUseActiveUser().enrollees[0],
        participantTasks: [{
          ...mockParticipantTask('SURVEY', 'IN_PROGRESS'),
          surveyResponseId: 'taskId1'
        }]
      }]
    })

    const { RoutedComponent } = setupRouterTest(<MockI18nProvider><DocumentLibrary/></MockI18nProvider>)
    render(RoutedComponent)
    await waitFor(() => {
      expect(screen.getByText('file1.pdf')).toBeInTheDocument()
    })

    //note: this is looking at the i18n key for the task name
    expect(screen.getByText('{researchSurvey1:1}')).toBeInTheDocument()
  })
})
