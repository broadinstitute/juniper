import { render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import { mockStudyEnvContext, mockSurvey, mockSurveyVersionsList } from 'test-utils/mocking-utils'
import VersionSelector from './VersionSelector'
import { select } from 'react-select-event'

jest.mock('api/api', () => ({
  getSurveyVersions: () => {
    return Promise.resolve(mockSurveyVersionsList())
  },
  getSurvey: () => {
    return Promise.resolve(mockSurvey())
  }
}))

describe('VersionSelector', () => {
  test('renders a list of form versions that can be selected', async () => {
    //Arrange
    const studyEnvContext = mockStudyEnvContext()
    render(<VersionSelector
      portalShortcode={studyEnvContext.portal.shortcode}
      stableId={mockSurvey().stableId}
      show={true}
      setShow={jest.fn()}
      visibleVersionPreviews={[]}
      setVisibleVersionPreviews={jest.fn()}
    />)

    //Act
    await waitFor(() => expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument())
    await select(screen.getByLabelText('Select version to preview'), ['1'])
    const openPreviewButton = screen.getByText('Open version')

    //Assert
    expect(openPreviewButton).toBeEnabled()
  })
})
