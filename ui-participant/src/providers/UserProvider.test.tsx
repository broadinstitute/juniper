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
  useEffect(() => {
    loginUserInternal({
      user: mockParticipantUser(),
      enrollees: []
    })
  }, [])

  const addEnrollee = () => {
    updateEnrollee(mockEnrollee()).then(() => { setUpdated(true) })
  }

  return <div>
    <button onClick={addEnrollee}>Add Enrollee</button>
    { (enrollees.length === 0 && !updated) && <span>No updates yet</span>}
    { (enrollees.length > 0 && !updated) && <span>Updated enrollee but not state</span>}
    { (enrollees.length === 0 && updated) && <span>Updated state but not enrollee</span>}
    { (enrollees.length > 0 && updated) && <span>Updated enrollee and state</span>}
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
      userEvent.click(screen.getByText('Add Enrollee'))
    })
    expect(screen.queryByText('Updated state but not enrollee')).toBeNull()
    await waitFor(() => expect(screen.getByText('Updated enrollee and state')).toBeInTheDocument())
  })
})
