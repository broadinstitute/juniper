import { renderEmailActivityIcon } from './EnrolleeTimeline'
import React from 'react'
import { faEnvelope, faEnvelopeOpen } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { mockEventDetails, mockNotification } from 'test-utils/mocking-utils'
import { render, screen } from '@testing-library/react'

describe('EnrolleeTimeline', () => {
  it('should render the email opened icon', () => {
    const notification = {
      ...mockNotification(),
      eventDetails: {
        ...mockEventDetails(),
        opensCount: 1
      }
    }
    const result = renderEmailActivityIcon(notification)
    expect(result).toEqual(<FontAwesomeIcon icon={faEnvelopeOpen} aria-label={'Email opened'}/>)
  })

  it('should render the email opened icon with 0 opens', () => {
    const notification = {
      ...mockNotification(),
      eventDetails: {
        ...mockEventDetails(),
        opensCount: 0
      }
    }

    const result = renderEmailActivityIcon(notification)
    expect(result).toEqual(<FontAwesomeIcon icon={faEnvelope} aria-label={'Email not yet opened'}/>)
  })

  it('should render an N/A notice if there are no event details', () => {
    const notification = mockNotification()
    render(renderEmailActivityIcon(notification))
    expect(screen.getByText('N/A')).toBeInTheDocument()
  })
})
