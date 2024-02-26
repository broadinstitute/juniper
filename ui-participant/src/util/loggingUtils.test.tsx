import React, { useEffect } from 'react'
import { render, screen } from '@testing-library/react'
import setupErrorLogger, { logError, logVitals } from './loggingUtils'
import Api from 'api/api'

const TestComponent = () => {
  useEffect(() => {
    setupErrorLogger()
  }, [])
  useEffect(() => {
    window.setTimeout(() => { throw { message: 'foo' } }, 100)
  }, [])
  return <span>
        Will throw error
  </span>
}


test('logs JS exceptions', async () => {
  const logSpy = jest.spyOn(Api, 'log').mockImplementation(jest.fn())
  // avoid cluttering the console with the error message from the expected error
  jest.spyOn(console, 'error').mockImplementation(jest.fn())
  render(<TestComponent/>)
  expect(screen.getByText('Will throw error')).toBeInTheDocument()
  await new Promise(r => setTimeout(r, 200))
  expect(logSpy).toHaveBeenCalledTimes(1)
  expect(logSpy).toHaveBeenCalledWith({
    environmentName: 'live',
    portalShortcode: 'localhost',
    eventDetail: '{"message":"foo"}\nhttp://localhost/',
    eventName: 'jserror', eventType: 'ERROR', stackTrace: undefined
  })
})

// See JN-840
test('handles metrics with circular reference', async () => {
  const logSpy = jest.spyOn(Api, 'log').mockImplementation(jest.fn())
  const metricsObj: Record<string, unknown> = { stuff: 1, things: 'blah' }
  metricsObj.circular = metricsObj

  logVitals(metricsObj)
  expect(logSpy).toHaveBeenCalledWith({
    environmentName: 'live',
    eventDetail: '{"stuff":1,"things":"blah","circular":"[Circular ~]"}',
    eventType: 'STATS',
    eventName: 'webvitals',
    portalShortcode: 'localhost'
  })
})

test('does not log errors if browser incompatible', async () => {
  const logSpy = jest.spyOn(Api, 'log').mockImplementation(jest.fn())
  jest.spyOn(Object, 'hasOwn').mockImplementation(() => {
    throw new Error('Test error')
  })
  const windowSpy = jest.spyOn(window, 'alert').mockImplementation(jest.fn())

  logError({ message: 'Object.hasOwn is not a function' },  'trace')
  expect(logSpy).toHaveBeenCalledWith({
    environmentName: 'live',
    eventDetail: 'Object.hasOwn is not a function',
    eventType: 'INFO',
    eventName: 'js-compatibility',
    portalShortcode: 'localhost'
  })
  expect(windowSpy).toHaveBeenCalledTimes(1)
})
