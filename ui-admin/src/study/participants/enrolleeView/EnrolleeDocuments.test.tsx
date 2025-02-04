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
      expect(screen.getByText('file1.pdf')).toBeInTheDocument()
      expect(screen.getByText('file2.png')).toBeInTheDocument()
    })
  })

  it('displays an empty message when there aren\t any documents', async () => {
    asMockedFn(Api.listParticipantFiles).mockResolvedValue([])

    render(<EnrolleeDocuments enrollee={mockEnrollee()} studyEnvContext={mockStudyEnvContext()} />)

    await waitFor(() => {
      expect(screen.getByText('This participant has not uploaded any documents')).toBeInTheDocument()
    })
  })
})
