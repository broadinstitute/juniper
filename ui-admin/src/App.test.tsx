import React from 'react'
import { render } from '@testing-library/react'
import App from './App'

test('renders at all', () => {
  render(<App />)
  expect(true).toBe(true)
})
