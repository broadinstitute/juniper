import React, { ReactElement } from 'react'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { Router as RemixRouter } from '@remix-run/router/dist/router'

/**
 * return both the component wrapped in a router, and the router object itself,
 * the latter can be used for testing url paths
 */
type RoutableTestStruct = {
  RoutedComponent: ReactElement,
  router: RemixRouter
}

/** adapted from
 https://stackoverflow.com/questions/70313688/how-i-could-test-location-with-memoryrouter-on-react-router-dom-v6 */
export function setupRouterTest(ComponentToRender: ReactElement, initialEntries=['/']): RoutableTestStruct {
  const router = createMemoryRouter(
    [{
      path: '*',
      element: ComponentToRender
    }],
    {
      initialEntries
    }
  )
  const RoutedComponent = <RouterProvider router={router} />

  return { RoutedComponent, router }
}
