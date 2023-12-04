import { render, screen } from '@testing-library/react'
import React from 'react'
import { mockPortalContext } from 'test-utils/mocking-utils'
import PortalEnvDiffView, { emptyChangeSet } from './PortalEnvDiffView'
import { setupRouterTest } from 'test-utils/router-testing-utils'

import { PortalEnvironmentChange } from 'api/api'
import userEvent from '@testing-library/user-event';


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
    expect(screen.queryAllByText('no changes')).toHaveLength(5)
    expect(screen.queryAllByRole('input')).toHaveLength(0)
  })

  it('handles a changeset with one item', async () => {
    const {portal} = mockPortalContext()
    const changeSet: PortalEnvironmentChange = {
      ...emptyChangeSet,
      configChanges: [
        {propertyName: 'password', oldValue: 'secret', newValue: 'moreSecret'}
      ]
    }
    const spyApplyChanges = jest.fn(() => 1)
    const {RoutedComponent} = setupRouterTest(<PortalEnvDiffView
      portal={portal}
      destEnvName={portal.portalEnvironments[0].environmentName}
      applyChanges={spyApplyChanges}
      sourceEnvName="sourceEnv"
      changeSet={changeSet}/>)
    render(RoutedComponent)
    expect(screen.queryAllByText('no changes')).toHaveLength(4)
    expect(screen.queryAllByRole('checkbox')).toHaveLength(1)

    // if we save without making any changes, the result should be an empty changeset
    await userEvent.click(screen.getByText('Copy changes'))
    expect(spyApplyChanges).toHaveBeenCalledTimes(1)
    expect(spyApplyChanges).toHaveBeenCalledWith(emptyChangeSet)

    // if we save after clicking the password field, we should save with a config change
    await userEvent.click(screen.getByText('password:'))
    await userEvent.click(screen.getByText('Copy changes'))
    expect(spyApplyChanges).toHaveBeenCalledTimes(2)
    expect(spyApplyChanges).toHaveBeenCalledWith(changeSet)
  })

  it('handles changes with siteContent', async () => {
    const {portal} = mockPortalContext()
    const changeSet: PortalEnvironmentChange = {
      ...emptyChangeSet,
      siteContentChange: {
        changed: true,
        newVersion: 2,
        oldVersion: 1,
        newStableId: 'contentId',
        oldStableId: 'contentId'
      }
    }
    const spyApplyChanges = jest.fn(() => 1)
    const {RoutedComponent} = setupRouterTest(<PortalEnvDiffView
      portal={portal}
      destEnvName={portal.portalEnvironments[0].environmentName}
      applyChanges={spyApplyChanges}
      sourceEnvName="sourceEnv"
      changeSet={changeSet}/>)
    render(RoutedComponent)
    expect(screen.queryAllByText('no changes')).toHaveLength(4)
    expect(screen.queryAllByRole('checkbox')).toHaveLength(1)

    // if we save without making any changes, the result should be an empty changeset
    await userEvent.click(screen.getByText('Copy changes'))
    expect(spyApplyChanges).toHaveBeenCalledTimes(1)
    expect(spyApplyChanges).toHaveBeenCalledWith(emptyChangeSet)

    // if we save after clicking the password field, we should save with a config change
    await userEvent.click(screen.getByText('contentId v1'))
    await userEvent.click(screen.getByText('Copy changes'))
    expect(spyApplyChanges).toHaveBeenCalledTimes(2)
    expect(spyApplyChanges).toHaveBeenCalledWith(changeSet)
  })

  it('handles changes with dashboard alerts', async () => {
    const { portal } = mockPortalContext()
    const changeSet: PortalEnvironmentChange = {
      ...emptyChangeSet,
      participantDashboardAlertChanges: [
        {
          trigger: 'NO_ACTIVITIES_REMAIN',
          changes: [
            {
              propertyName: 'title',
              oldValue: 'Old Title',
              newValue: 'New Title'
            },
            {
              propertyName: 'detail',
              oldValue: 'Old message',
              newValue: 'New message'
            }
          ]
        }
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
    const changeCheckboxes = screen.queryAllByRole('checkbox')
    expect(screen.queryAllByText('no changes')).toHaveLength(4)
    expect(changeCheckboxes).toHaveLength(2)

    for (const checkbox of changeCheckboxes) {
      await userEvent.click(checkbox)
    }

    await userEvent.click(screen.getByText('Copy changes'))

    expect(spyApplyChanges).toHaveBeenCalledTimes(1)
    expect(spyApplyChanges).toHaveBeenCalledWith(changeSet)
  })
})
