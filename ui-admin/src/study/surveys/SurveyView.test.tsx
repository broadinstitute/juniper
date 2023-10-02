import { mockStudyEnvContext } from 'test-utils/mocking-utils'
import { render, screen } from '@testing-library/react'
import React from 'react'
import SurveyView from './SurveyView'


describe('SurveyView', () => {
  test('displays a message if loading a survey for an uninitialized study env', async () => {
    //Arrange
    const studyEnvContext = {
      ...mockStudyEnvContext(),
      currentEnv: {
        ...mockStudyEnvContext().currentEnv,
        studyEnvironmentConfig: {
          ...mockStudyEnvContext().currentEnv.studyEnvironmentConfig,
          initialized: false
        }
      }
    }
    render(<SurveyView studyEnvContext={studyEnvContext}/>)

    //Assert
    expect(screen.getByText('Study environment not initialized')).toBeInTheDocument()
  })
})
