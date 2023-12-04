import React, { useEffect } from 'react'
import { render, screen } from '@testing-library/react'
import setupErrorLogger from './loggingUtils'
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
    eventDetail: '{"message":"foo"}\nhttp://localhost/',
    eventName: 'jserror', eventType: 'ERROR', stackTrace: undefined
  })
})
