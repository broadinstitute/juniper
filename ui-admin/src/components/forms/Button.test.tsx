import { act, render, screen } from '@testing-library/react'
import { userEvent } from '@testing-library/user-event'
import React from 'react'

import { Button } from './Button'

describe('Button', () => {
  it('renders a button', () => {
    // Act
    render(<Button>Test button</Button>)

    // Assert
    const button = screen.getByRole('button')
    expect(button).toHaveTextContent('Test button')
  })

  it('calls onClick', async () => {
    // Arrange
    const user = userEvent.setup()

    const onClick = jest.fn()
    render(<Button onClick={onClick}>Test button</Button>)

    // Act
    const button = screen.getByText('Test button')
    await act(() => user.click(button))

    // Assert
    expect(onClick).toHaveBeenCalled()
  })

  it('is styled based on variant', () => {
    // Act
    render(
      <>
        <Button>Default button</Button>
        <Button variant="primary">Primary button</Button>
        <Button outline variant="light">Light outline button</Button>
      </>
    )

    // Assert
    const defaultButton = screen.getByText('Default button')
    const primaryButton = screen.getByText('Primary button')
    const lightOutlineButton = screen.getByText('Light outline button')

    expect(Array.from(defaultButton.classList)).toEqual(['btn', 'btn-secondary'])
    expect(Array.from(primaryButton.classList)).toEqual(['btn', 'btn-primary'])
    expect(Array.from(lightOutlineButton.classList)).toEqual(['btn', 'btn-outline-light'])
  })

  it('disables button using aria-disabled', async () => {
    // Arrange
    const user = userEvent.setup()
    const onClick = jest.fn()

    // Act
    render(<Button disabled onClick={onClick}>Test button</Button>)

    // Assert
    const button = screen.getByRole('button')

    expect(button).not.toHaveAttribute('disabled')
    expect(button).toHaveAttribute('aria-disabled', 'true')
    expect(button).toHaveClass('disabled')

    await act(() => user.click(button))
    expect(onClick).not.toHaveBeenCalled()
  })

  it('shows tooltip when hovered', async () => {
    // Arrange
    const user = userEvent.setup()
    render(<Button tooltip="This is a tooltip">Test button</Button>)

    // Act
    const button = screen.getByRole('button')
    await act(() => user.hover(button))

    // Assert
    screen.getByText('This is a tooltip')

    // Act
    await act(() => user.unhover(button))

    // Assert
    expect(() => screen.getByText('This is a tooltip')).toThrow()
  })

  it('shows tooltip when focused', async () => {
    const user = userEvent.setup()
    render(<Button tooltip="This is a tooltip">Test button</Button>)
    const button = screen.getByRole('button')

    expect(button).not.toHaveFocus()
    expect(screen.queryByText('This is a tooltip')).not.toBeInTheDocument()


    await user.tab()

    expect(button).toHaveFocus()
    screen.getByText('This is a tooltip')
  })
})
