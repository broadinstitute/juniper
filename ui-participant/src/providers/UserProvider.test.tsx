import React, { useEffect, useState } from 'react'
import UserProvider, { useUser } from './UserProvider'
import { mockEnrollee, mockParticipantUser } from '../test-utils/test-participant-factory'
import { setupRouterTest } from '../test-utils/router-testing-utils'
import { act, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AuthProvider } from 'react-oidc-context'

/** component for tracing how enrollee update state is propagated */
const UpdateEnrolleeTestComponent = () => {
  const { loginUserInternal, enrollees, updateEnrollee } = useUser()
  const [updated, setUpdated] = useState(false)
  const mockEnrolleeObject = mockEnrollee()
  useEffect(() => {
    loginUserInternal({
      user: mockParticipantUser(),
      ppUser: { profile: {}, profileId: '', id: '' },
      profile: {},
      enrollees: [mockEnrolleeObject],
      relations: []
    })
  }, [])

  const addEnrollee = () => {
    updateEnrollee(mockEnrolleeObject).then(() => { setUpdated(true) })
  }

  return <div>
    <button onClick={addEnrollee}>Update Enrollee</button>
    { (enrollees.length === 1 && !updated) && <span>No updates yet</span>}
    { (enrollees.length > 1 && !updated) && <span>Updated enrollee but not state</span>}
    { (enrollees.length === 1 && updated) && <span>Updated state but not enrollee</span>}
    { (enrollees.length > 1 && updated) && <span>Updated enrollee and state</span>}
  </div>
}

jest.mock('react-oidc-context', () => {
  return {
    ...jest.requireActual('react-oidc-context'),
    useAuth: () => ({ events: { addUserLoaded: () => 1 } }),
    AuthProvider: ({ children }: React.PropsWithChildren) => { return children }
  }
})

describe('UserProvider', () => {
  it('updates an enrollee and calls the afterFn', async () => {
    const { RoutedComponent } = setupRouterTest(<AuthProvider>
      <UserProvider><UpdateEnrolleeTestComponent/></UserProvider>
    </AuthProvider>)
    render(RoutedComponent)
    expect(screen.getByText('No updates yet')).toBeInTheDocument()
    act(() => {
      userEvent.click(screen.getByText('Update Enrollee'))
    })
    expect(screen.queryByText('Updated state but not enrollee')).toBeNull()
    await waitFor(() => expect(screen.getByText('Updated state but not enrollee')).toBeInTheDocument())
  })
})
