import React, {
  useEffect,
  useState
} from 'react'
import UserProvider, { useUser } from './UserProvider'
import {
  mockEnrollee,
  mockParticipantUser
} from '../test-utils/test-participant-factory'
import {
  act,
  render,
  screen,
  waitFor
} from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import { AuthProvider } from 'react-oidc-context'
import { setupRouterTest } from '@juniper/ui-core'

/** component for tracing how enrollee update state is propagated */
const UpdateEnrolleeTestComponent = () => {
  const { loginUserInternal, enrollees, updateEnrollee } = useUser()
  const [updated, setUpdated] = useState(false)
  useEffect(() => {
    loginUserInternal({
      user: mockParticipantUser(),
      ppUsers: [],
      proxyRelations: [],
      profile: {},
      enrollees: [mockEnrollee()]
    })
  }, [])

  const addEnrollee = () => {
    const newEnrollee = mockEnrollee()
    newEnrollee.profile.givenName = 'New'
    updateEnrollee(newEnrollee).then(() => {
      setUpdated(true)
    })
  }

  return <div>
    <button onClick={addEnrollee}>Add Enrollee</button>
    {(enrollees[0]?.profile.givenName === 'New' && updated) && <span>Updated enrollee and state</span>}
  </div>
}

jest.mock('react-oidc-context', () => {
  return {
    ...jest.requireActual('react-oidc-context'),
    useAuth: () => ({ events: { addUserLoaded: () => 1 } }),
    AuthProvider: ({ children }: React.PropsWithChildren) => { return children }
  }
})

jest.mock('mixpanel-browser')

describe('UserProvider', () => {
  it('updates an enrollee and calls the afterFn', async () => {
    const { RoutedComponent } = setupRouterTest(<AuthProvider>
      <UserProvider><UpdateEnrolleeTestComponent/></UserProvider>
    </AuthProvider>)
    render(RoutedComponent)
    expect(screen.queryByText('Updated enrollee and state')).not.toBeInTheDocument()
    act(() => {
      userEvent.click(screen.getByText('Add Enrollee'))
    })
    await waitFor(() => expect(screen.getByText('Updated enrollee and state')).toBeInTheDocument())
  })
})
