import React from 'react'
import { screen } from '@testing-library/react'
import KitEnrolleeSelection from './KitEnrolleeSelection'
import { asMockedFn, KitType } from '@juniper/ui-core'
import { mockKitType, mockStudyEnvContext, renderWithRouterAndStore } from 'test-utils/mocking-utils'
import Api from 'api/api'

jest.mock('api/api', () => ({
  fetchKitTypes: jest.fn(),
  executeSearchExpression: jest.fn().mockResolvedValue([]),
  getExpressionSearchFacets: jest.fn().mockResolvedValue({})
}))

describe('KitEnrolleeSelection', () => {
  it('should show a column for each kit type', async () => {
    const mockKitTypes: KitType[] = [
      mockKitType(),
      {
        ...mockKitType(),
        name: 'another',
        displayName: 'Another'
      }
    ]

    asMockedFn(Api.fetchKitTypes).mockResolvedValue(mockKitTypes)

    renderWithRouterAndStore(<KitEnrolleeSelection studyEnvContext={mockStudyEnvContext()}/>)

    for (const kitType of mockKitTypes) {
      expect(await screen.findByText(`${kitType.displayName} kit requested`)).toBeInTheDocument()
    }
  })
})
