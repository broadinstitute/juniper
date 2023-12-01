import React from 'react'
import { emptyStudyEnvChange } from './PortalEnvDiffView'
import StudyEnvDiff from './StudyEnvDiff'
import { render, screen } from '@testing-library/react'

test('StudyEnvDiff renders the name of the study', () => {
  render(<StudyEnvDiff studyName="Test Study"
    studyEnvChange={emptyStudyEnvChange}
    selectedChanges={emptyStudyEnvChange}
    setSelectedChanges={jest.fn()}
  />)
  expect(screen.getByText('Test Study')).toBeInTheDocument()
})
