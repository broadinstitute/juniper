import React from 'react'
import Api from 'api/api'
import { render, screen } from '@testing-library/react'
import PopulateSiteContent from './PopulateSiteContent'
import userEvent from '@testing-library/user-event'
import { Store } from 'react-notifications-component'

test('populate control can set params', async () => {
  jest.spyOn(Api, 'populateSiteContent').mockImplementation(() => Promise.resolve())
  jest.spyOn(Store, 'addNotification').mockImplementation(() => '')

  render(<PopulateSiteContent initialPortalShortcode={''}/>)
  expect(screen.getByLabelText('No')).toBeChecked()
  expect(screen.getByLabelText('Yes')).not.toBeChecked()
  await userEvent.click(screen.getByLabelText('Yes'))
  expect(screen.getByLabelText('Yes')).toBeChecked()

  await userEvent.type(screen.getByLabelText('Portal shortcode'), 'ourhealth')
  await userEvent.type(screen.getByLabelText('File path (from /populate/src/main/resources/seed)'), 'foo.json')

  await userEvent.click(screen.getByText('Populate'))
  expect(Api.populateSiteContent).toHaveBeenCalledTimes(1)
  expect(Api.populateSiteContent).toHaveBeenCalledWith('foo.json', true, 'ourhealth')
})
