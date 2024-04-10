import React, { ReactElement } from 'react'
import { createMemoryRouter, RouterProvider } from 'react-router-dom'
import { Router as RemixRouter } from '@remix-run/router/dist/router'
import { waitFor, render } from '@testing-library/react'

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
export function setupRouterTest(ComponentToRender: ReactElement,
  initialEntries=['/'], componentPath = '*'): RoutableTestStruct {
  const routes = [{
    path: componentPath,
    element: ComponentToRender
  }]
  if (componentPath !== '*') {
    routes.push({
      path: '*',
      element: <div>catch-all route</div>
    })
  }
  const router = createMemoryRouter(routes, { initialEntries })
  const RoutedComponent = <RouterProvider router={router} />

  return { RoutedComponent, router }
}

/**
 * confirm even after useEffects, the callback will still be true
 * from https://stackoverflow.com/questions/68400489/how-to-wait-to-assert-an-element-never-appears-in-the-document
  */
export async function expectNever(callable: () => unknown): Promise<void> {
  await expect(() => waitFor(callable)).rejects.toEqual(expect.anything())
}

/** render a component wrapped in a router, use 'setupRouterTest' if you need to access the router
 * directly.
 * paths in the initialEntries array should always start with a '/'.
 * componentPath is the route to mount the component at.  Use this argument if you need to pass a routing param
 * to the component
 * */
export function renderWithRouter(ComponentToRender: ReactElement, initialEntries=['/'], componentPath= '*') {
  const { RoutedComponent } = setupRouterTest(ComponentToRender, initialEntries, componentPath)
  return render(RoutedComponent)
}
