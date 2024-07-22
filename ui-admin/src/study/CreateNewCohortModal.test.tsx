import React from 'react'
import { userEvent } from '@testing-library/user-event'
import { render, screen } from '@testing-library/react'
import CreateNewCohortModal from './CreateNewCohortModal'
import { makeMockPortal, makeMockPortalStudy } from '../test-utils/mocking-utils'
import { useNavContext } from '../navbar/NavContextProvider'
import { Store } from 'react-notifications-component'

jest.mock('../navbar/NavContextProvider')

jest.mock('../api/api', () => ({
  ...jest.requireActual('../api/api'),
  exportEnrollees: jest.fn().mockResolvedValue({ json: () => ({ columnKeys: [] }) })
}))

describe('CreateNewCohortModal', () => {
  test('enables Create button when cohort name, study, and portal are filled out', async () => {
    const user = userEvent.setup()
    jest.spyOn(Store, 'addNotification').mockImplementation(() => '')

    const portalList = [
      makeMockPortal('Test portal', [
        makeMockPortalStudy('Test study', 'testStudy')
      ], 'testStudy')
    ]

    const mockContextValue = {
      breadCrumbs: [],
      setBreadCrumbs: jest.fn(),
      portalList,
      setPortalList: jest.fn()
    }

    ;(useNavContext as jest.Mock).mockReturnValue(mockContextValue)

    render(<CreateNewCohortModal onDismiss={jest.fn()}/>)

    expect(screen.getByText('Create')).toHaveAttribute('aria-disabled', 'true')

    const nameInput = screen.getByLabelText('Cohort Name')
    await user.type(nameInput, 'Test cohort')

    const portalSelect = screen.getByLabelText('Portal')
    await user.click(portalSelect)
    await user.click(screen.queryAllByText('Test portal')[0])

    const studySelect = screen.getByLabelText('Study')
    await user.click(studySelect)
    await user.click(screen.queryAllByText('Test study')[0])

    expect(screen.getByText('Create')).toHaveAttribute('aria-disabled', 'false')
  })
})
