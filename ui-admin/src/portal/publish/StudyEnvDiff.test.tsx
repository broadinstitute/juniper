import React from 'react'
import { emptyStudyEnvChange } from './PortalEnvDiffView'
import StudyEnvDiff from './StudyEnvDiff'
import { render } from '@testing-library/react'

test('StudyEnvDiff renders the name of the study', () => {
  const { getByText } = render(<StudyEnvDiff studyName="Test Study"
    studyEnvChange={emptyStudyEnvChange}
    selectedChanges={emptyStudyEnvChange}
    setSelectedChanges={jest.fn()}
  />)
  expect(getByText('Test Study')).toBeInTheDocument()
})
