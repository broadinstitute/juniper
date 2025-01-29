import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import EnrolleeDocuments from './EnrolleeDocuments'
import { asMockedFn, mockEnrollee } from '@juniper/ui-core'
import { mockStudyEnvContext } from 'test-utils/mocking-utils'
import Api from 'api/api'

jest.mock('api/api', () => ({
  listParticipantFiles: jest.fn()
}))

describe('EnrolleeDocuments', () => {
  it('displays participant documents', async () => {
    asMockedFn(Api.listParticipantFiles).mockResolvedValue([
      { id: 'file1', fileName: 'file1.pdf', fileType: 'application/pdf', createdAt: 0, lastUpdatedAt: 0 },
      { id: 'file2', fileName: 'file2.png', fileType: 'image/png', createdAt: 0, lastUpdatedAt: 0 }
    ])

    render(<EnrolleeDocuments enrollee={mockEnrollee()} studyEnvContext={mockStudyEnvContext()} />)

    await waitFor(() => {
      expect(screen.getByText('file1.pdf')).toBeInTheDocument()
      expect(screen.getByText('file2.png')).toBeInTheDocument()
    })
  })
})
