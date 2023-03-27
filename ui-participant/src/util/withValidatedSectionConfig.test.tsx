import { render } from '@testing-library/react'
import React from 'react'

import { SectionConfig } from 'api/api'
import { requireString } from 'util/validationUtils'

import { withValidatedSectionConfig } from './withValidatedSectionConfig'

describe('withValidatedSectionConfig', () => {
  // Arrange
  type TestSectionTemplateConfig = {
    value: string
  }

  const validateTestSectionTemplateConfig = (config: SectionConfig): TestSectionTemplateConfig => {
    const value = requireString(config, 'value', 'Invalid TestSectionTemplateConfig')
    return { value }
  }

  const TestSectionTemplate = withValidatedSectionConfig(
    validateTestSectionTemplateConfig,
    ({ config }: { config: TestSectionTemplateConfig }) => {
      return <div>{config.value}</div>
    }
  )

  describe('with a valid configuration', () => {
    // Arrange
    const validConfig: SectionConfig = {
      value: 'Hello world'
    }

    it('renders wrapped template component', () => {
      // Act
      const { container } = render(<TestSectionTemplate config={validConfig}/>)

      // Assert
      expect(container).toHaveTextContent('Hello world')
    })
  })

  describe('with an invalid configuration', () => {
    // Arrange
    const invalidConfig: SectionConfig = {}

    beforeEach(() => {
      jest.spyOn(console, 'warn').mockImplementation(() => undefined)
    })

    it('renders nothing', () => {
      // Act
      const { container } = render(<TestSectionTemplate config={invalidConfig}/>)

      // Assert
      expect(container).toBeEmptyDOMElement()
    })

    it('logs a warning', () => {
      // Act
      render(<TestSectionTemplate config={invalidConfig}/>)

      // Assert
      expect(console.warn).toHaveBeenCalled()
    })
  })
})
