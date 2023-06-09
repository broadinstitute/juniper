import { render, screen } from '@testing-library/react'
import React from 'react'

import { FormContent } from '@juniper/ui-core'

import { FormPreview } from './FormPreview'

const formContent: FormContent = {
  title: 'Test survey',
  pages: [
    {
      elements: [
        {
          name: 'test_firstName',
          type: 'text',
          title: 'First name',
          isRequired: true
        },
        {
          name: 'test_lastName',
          type: 'text',
          title: 'Last name',
          isRequired: true
        }
      ]
    }
  ]
}

describe('FormPreview', () => {
  // eslint-disable-next-line jest/expect-expect
  it('renders form', () => {
    // Act
    render(<FormPreview formContent={formContent} />)

    // Assert
    screen.getAllByLabelText('First name')
    screen.getAllByLabelText('Last name')
  })
})
