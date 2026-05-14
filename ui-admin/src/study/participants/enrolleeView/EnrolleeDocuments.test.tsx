import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import EnrolleeDocuments from './EnrolleeDocuments'
import { asMockedFn, mockEnrollee, mockParticipantFile } from '@juniper/ui-core'
import { mockStudyEnvContext } from 'test-utils/mocking-utils'
import Api from 'api/api'

jest.mock('api/api', () => ({
  listParticipantFiles: jest.fn()
}))

describe('EnrolleeDocuments', () => {
  it('displays participant documents', async () => {
    asMockedFn(Api.listParticipantFiles).mockResolvedValue([
      mockParticipantFile('file1.pdf'),
      mockParticipantFile('file2.png')
    ])

    render(<EnrolleeDocuments enrollee={mockEnrollee()} studyEnvContext={mockStudyEnvContext()} />)

    await waitFor(() => {
      expect(screen.getAllByText('file1.pdf').length).toBeGreaterThan(0)
      expect(screen.getAllByText('file2.png').length).toBeGreaterThan(0)
    })
  })

  it('displays an empty message when there aren\t any documents', async () => {
    asMockedFn(Api.listParticipantFiles).mockResolvedValue([])

    render(<EnrolleeDocuments enrollee={mockEnrollee()} studyEnvContext={mockStudyEnvContext()} />)

    await waitFor(() => {
      expect(screen.getByText('No uploaded documents')).toBeInTheDocument()
    })
  })
})
