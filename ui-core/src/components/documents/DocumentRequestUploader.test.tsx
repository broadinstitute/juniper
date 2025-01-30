import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import { DocumentRequestUploader } from './DocumentRequestUploader'
import { useApiContext } from 'src/participant/ApiProvider'
import { StudyEnvParams } from 'src/types/study'
import { ParticipantFile } from 'src/types/participantFile'
import { asMockedFn } from 'src/test-utils/asMockedFn'
import { userEvent } from '@testing-library/user-event'

jest.mock('src/participant/ApiProvider')

const mockApi = {
  listParticipantFiles: jest.fn(),
  uploadParticipantFile: jest.fn(),
  downloadParticipantFile: jest.fn()
};

(useApiContext as jest.Mock).mockReturnValue(mockApi)

const studyEnvParams: StudyEnvParams = {
  envName: 'sandbox',
  studyShortcode: 'test-study',
  portalShortcode: 'test-portal'
}

const renderDocumentRequestUpload = () => {
  render(
    <DocumentRequestUploader
      studyEnvParams={studyEnvParams}
      enrolleeShortcode={'HDSALK'}
      selectedFileNames={[]}
      setSelectedFileNames={jest.fn()}
    />
  )
}

describe('DocumentRequestUpload', () => {
  it('should render document list', async () => {
    const files: ParticipantFile[] = [
      {
        id: '1', fileName: 'file1.pdf', fileType: 'application/pdf',
        createdAt: 0, lastUpdatedAt: 0, surveyResponseIds: []
      },
      { id: '2', fileName: 'file2.jpg', fileType: 'image/jpeg', createdAt: 0, lastUpdatedAt: 0, surveyResponseIds: [] }
    ]
    asMockedFn(mockApi.listParticipantFiles).mockResolvedValue(files)

    renderDocumentRequestUpload()

    await waitFor(() => {
      expect(screen.getByText('file1.pdf')).toBeInTheDocument()
      expect(screen.getByText('file2.jpg')).toBeInTheDocument()
    })
  })

  test('should upload a new file', async () => {
    const newFile: ParticipantFile = {
      id: '3', fileName: 'file3.png', fileType: 'image/png', createdAt: 0, lastUpdatedAt: 0, surveyResponseIds: []
    }
    asMockedFn(mockApi.listParticipantFiles).mockResolvedValue([])
    asMockedFn(mockApi.uploadParticipantFile).mockResolvedValue(newFile)

    renderDocumentRequestUpload()

    const file = new File(['dummy content'], 'file3.png', { type: 'image/png' })
    const fileInput = screen.getByTestId('fileInput') as HTMLInputElement
    await userEvent.upload(fileInput, file)

    await waitFor(() => {
      expect(mockApi.uploadParticipantFile).toHaveBeenCalledWith({
        studyEnvParams,
        enrolleeShortcode: 'HDSALK',
        file
      })
      expect(screen.getByText('file3.png')).toBeInTheDocument()
    })
  })

  test('should download a file when clicked', async () => {
    const file: ParticipantFile = {
      id: '1', fileName: 'file1.pdf', fileType: 'application/pdf', createdAt: 0, lastUpdatedAt: 0, surveyResponseIds: []
    }
    asMockedFn(mockApi.listParticipantFiles).mockResolvedValue([file])
    asMockedFn(mockApi.downloadParticipantFile).mockResolvedValue(new Response())

    renderDocumentRequestUpload()

    await waitFor(() => {
      expect(screen.getByText('file1.pdf')).toBeInTheDocument()
    })

    const downloadButton = screen.getByText('file1.pdf')
    await userEvent.click(downloadButton!)

    await waitFor(() => {
      expect(mockApi.downloadParticipantFile).toHaveBeenCalledWith({
        studyEnvParams,
        enrolleeShortcode: 'HDSALK',
        fileName: 'file1.pdf'
      })
    })
  })
})
