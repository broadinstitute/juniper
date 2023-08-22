import { act, render, screen, waitFor } from '@testing-library/react'
import React from 'react'
import { mockStudyEnvContext, mockSurvey, mockSurveyVersionsList } from 'test-utils/mocking-utils'
import userEvent from '@testing-library/user-event'
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

  test('renders the list of form versions', async () => {
    //Arrange
    const user = userEvent.setup()
    const studyEnvContext = mockStudyEnvContext()
    render(<VersionSelector
      portalShortcode={studyEnvContext.portal.shortcode}
      stableId={mockSurvey().stableId}
      show={true}
      setShow={jest.fn()}
      previewedVersions={[]}
      setPreviewedVersions={jest.fn()}
    />)

    //Act
    await waitFor(() => expect(screen.queryByTestId('loading-spinner')).not.toBeInTheDocument())
    const versionSelect = screen.getByLabelText('Select version to preview') as HTMLSelectElement
    await select(screen.getByLabelText('Select version to preview'), ['1'])

    //Assert
    expect(versionSelect).toHaveValue(1)
  })

  // test('does not let the user preview the same form version twice', () => {
  //   //Arrange
  //   jest.mock('api/api', () => ({
  //     getSurveyVersions: () => {
  //       return Promise.resolve(tail(mockSurveyVersionsList()))
  //     }
  //   }))
  //
  //   const studyEnvContext = mockStudyEnvContext()
  //   render(<VersionSelector
  //     portalShortcode={studyEnvContext.portal.shortcode}
  //     stableId={mockSurvey().stableId}
  //     show={true}
  //     setShow={jest.fn()}
  //     previewedVersions={[head(mockSurveyVersionsList())!]}
  //     setPreviewedVersions={jest.fn()}
  //   />)
  //
  //   //Assert
  //   const versionDropdown = screen.getByText('Select version to preview') as HTMLSelectElement
  //
  //   console.log(versionDropdown.options)
  //
  //   mockSurveyVersionsList().forEach((survey) => {
  //     expect(versionDropdown.options.namedItem(survey.version.toString())).toBeInTheDocument()
  //   })
  // })
})
