import React from 'react'

import { render, screen, waitFor } from '@testing-library/react'
import { mockPortalContext, mockSiteMedia } from 'test-utils/mocking-utils'
import Api from 'api/api'
import { userEvent } from '@testing-library/user-event'
import SiteMediaUploadModal, { cleanFileName } from './SiteMediaUploadModal'
import { Store } from 'react-notifications-component'
import { setupRouterTest } from '@juniper/ui-core'


test('upload is disabled until file chosen', async () => {
  const file = new File(['hello'], 'hello.png', { type: 'image/png' })
  const { RoutedComponent } = setupRouterTest(
    <SiteMediaUploadModal portalContext={mockPortalContext()} onDismiss={jest.fn()} onSubmit={jest.fn()}/>)
  render(RoutedComponent)
  expect(screen.getByText('Upload')).toHaveAttribute('aria-disabled', 'true')
  const fileInput = screen.getByTestId('fileInput') as HTMLInputElement
  await userEvent.upload(fileInput, file)
  expect(screen.getByText('Upload')).toHaveAttribute('aria-disabled', 'false')
})

test('file name shown to user is cleaned', async () => {
  const file = new File(['hello'], 'Hello ^&stuff.png', { type: 'image/png' })
  const { RoutedComponent } = setupRouterTest(
    <SiteMediaUploadModal portalContext={mockPortalContext()} onDismiss={jest.fn()} onSubmit={jest.fn()}/>)
  render(RoutedComponent)
  const fileInput = screen.getByTestId('fileInput') as HTMLInputElement
  await userEvent.upload(fileInput, file)
  expect(screen.getByLabelText('Name:')).toHaveValue('hello_stuff.png')
})

test('upload api is called on submit', async () => {
  const uploadSpy = jest.spyOn(Api, 'uploadPortalMedia').mockResolvedValue(mockSiteMedia())
  jest.spyOn(Store, 'addNotification').mockImplementation(jest.fn())
  const file = new File(['databits'], 'hello.png', { type: 'image/png' })
  const portalContext = mockPortalContext()
  const { RoutedComponent } = setupRouterTest(
    <SiteMediaUploadModal portalContext={portalContext} onDismiss={jest.fn()} onSubmit={jest.fn()}/>)
  render(RoutedComponent)
  const fileInput = screen.getByTestId('fileInput') as HTMLInputElement
  await userEvent.upload(fileInput, file)
  userEvent.click(screen.getByText('Upload'))
  await waitFor(() => expect(uploadSpy)
    .toHaveBeenCalledWith(portalContext.portal.shortcode, file.name, 1, file))
})

test('uploading an invalid file type displays a validation error message', async () => {
  const { RoutedComponent } = setupRouterTest(
    <SiteMediaUploadModal portalContext={mockPortalContext()} onDismiss={jest.fn()} onSubmit={jest.fn()}/>)
  render(RoutedComponent)
  const file = new File(['hello'], 'hello.notallowed')
  const fileInput = screen.getByTestId('fileInput') as HTMLInputElement
  await userEvent.upload(fileInput, file)
  expect(screen.getByText('This file extension is not supported.')).toBeInTheDocument()
})

test('cleanFileName handles names', async () => {
  expect(cleanFileName('hello.png')).toBe('hello.png')
})

test('cleanFileName downcases uppercase', async () => {
  expect(cleanFileName('Hello.png')).toBe('hello.png')
})

test('cleanFileName replaces spaces with underscore', async () => {
  expect(cleanFileName('He llo.png')).toBe('he_llo.png')
})

test('cleanFileName removes special chars', async () => {
  expect(cleanFileName('He$$$#@$%llo.png')).toBe('hello.png')
})


