import React from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import { DocumentRequestUploader } from './DocumentRequestUploader'
import { useApiContext } from 'src/participant/ApiProvider'
import { StudyEnvParams } from 'src/types/study'
import { ParticipantFile } from 'src/types/participantFile'
import { asMockedFn } from 'src/test-utils/asMockedFn'
import { userEvent } from '@testing-library/user-event'
import { mockParticipantFile } from 'src/test-utils/mocking-utils'
import { MockI18nProvider } from 'src/participant/i18n-testing-utils'

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
    <MockI18nProvider>
      <DocumentRequestUploader
        studyEnvParams={studyEnvParams}
        enrolleeShortcode={'HDSALK'}
        selectedFileNames={[]}
        onSelectedFilesChanged={jest.fn()}
      />
    </MockI18nProvider>
  )
}

describe('DocumentRequestUpload', () => {
  it('should render document list', async () => {
    const files: ParticipantFile[] = [
      mockParticipantFile('file1.pdf'),
      mockParticipantFile('file2.jpg')
    ]
    asMockedFn(mockApi.listParticipantFiles).mockResolvedValue(files)

    renderDocumentRequestUpload()

    await waitFor(() => {
      expect(screen.getByText('file1.pdf')).toBeInTheDocument()
      expect(screen.getByText('file2.jpg')).toBeInTheDocument()
    })
  })

  test('should upload a new file', async () => {
    const newFile: ParticipantFile = mockParticipantFile('file3.png')
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
    const file: ParticipantFile = mockParticipantFile('file1.pdf')
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
