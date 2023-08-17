import { render, screen, fireEvent } from '@testing-library/react'
import React from 'react'
import { mockPortalContext } from 'test-utils/mocking-utils'
import PortalEnvDiffView, { emptyChangeSet } from './PortalEnvDiffView'
import { setupRouterTest } from 'test-utils/router-testing-utils'

import { PortalEnvironmentChange } from 'api/api'


describe('PortalEnvDiff', () => {
  it('handles an empty changeset', () => {
    const { portal } = mockPortalContext()

    const { RoutedComponent } = setupRouterTest(<PortalEnvDiffView
      portal={portal}
      destEnvName={portal.portalEnvironments[0].environmentName}
      applyChanges={() => 1}
      sourceEnvName="sourceEnv"
      changeSet={emptyChangeSet}/>)
    render(RoutedComponent)
    expect(screen.queryAllByText('no changes')).toHaveLength(4)
    expect(screen.queryAllByRole('input')).toHaveLength(0)
  })

  it('handles a changeset with one item', () => {
    const { portal } = mockPortalContext()
    const changeSet: PortalEnvironmentChange = {
      ...emptyChangeSet,
      configChanges: [
        { propertyName: 'password', oldValue: 'secret', newValue: 'moreSecret' }
      ]
    }
    const spyApplyChanges = jest.fn(() => 1)
    const { RoutedComponent } = setupRouterTest(<PortalEnvDiffView
      portal={portal}
      destEnvName={portal.portalEnvironments[0].environmentName}
      applyChanges={spyApplyChanges}
      sourceEnvName="sourceEnv"
      changeSet={changeSet}/>)
    render(RoutedComponent)
    expect(screen.queryAllByText('no changes')).toHaveLength(3)
    expect(screen.queryAllByRole('checkbox')).toHaveLength(1)

    // if we save without making any changes, the result should be an empty changeset
    fireEvent.click(screen.getByText('Copy changes'))
    expect(spyApplyChanges).toHaveBeenCalledTimes(1)
    expect(spyApplyChanges).toHaveBeenCalledWith(emptyChangeSet)

    // if we save after clicking the password field, we should save with a config change
    fireEvent.click(screen.getByText('password:'))
    fireEvent.click(screen.getByText('Copy changes'))
    expect(spyApplyChanges).toHaveBeenCalledTimes(2)
    expect(spyApplyChanges).toHaveBeenCalledWith(changeSet)
  })
})
