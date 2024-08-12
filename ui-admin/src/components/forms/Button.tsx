import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { IconDefinition } from '@fortawesome/fontawesome-svg-core'
import classNames from 'classnames'
import React, { useRef, useState } from 'react'
import { Overlay, Tooltip } from 'react-bootstrap'
import { Placement } from 'react-bootstrap/types'
import { faEllipsisV } from '@fortawesome/free-solid-svg-icons'

type ButtonVariant =
  | 'primary'
  | 'secondary'
  | 'success'
  | 'danger'
  | 'warning'
  | 'info'
  | 'light'
  | 'dark'
  | 'link'

export type ButtonProps = JSX.IntrinsicElements['button'] & {
  outline?: boolean
  tooltip?: string
  tooltipPlacement?: Placement
  variant?: ButtonVariant
}

/** A button.  Among other improvements, this handles the 'disabled' prop in a much more robust way
 * than <button>, enabling tooltips to be shown for disabled buttons. */
export const Button = (props: ButtonProps) => {
  const { outline = false, tooltip, tooltipPlacement, variant = 'secondary', ...buttonProps } = props
  const { className, disabled, onBlur, onClick, onFocus, onMouseEnter, onMouseLeave } = buttonProps

  const buttonRef = useRef<HTMLButtonElement>(null)
  const [isFocused, setIsFocused] = useState(false)
  const [isHovered, setIsHovered] = useState(false)

  return (
    <>
      <button
        {...buttonProps}
        ref={buttonRef}
        aria-disabled={disabled}
        style={{ pointerEvents: 'auto' }}
        className={
          classNames(
            'btn',
            outline ? `btn-outline-${variant}` : `btn-${variant}`,
            { disabled },
            className
          )
        }
        disabled={undefined}
        type="button"
        onClick={disabled ? undefined : onClick}
        onFocus={e => {
          onFocus?.(e)
          setIsFocused(true)
        }}
        onBlur={e => {
          onBlur?.(e)
          setIsFocused(false)
        }}
        onMouseEnter={e => {
          onMouseEnter?.(e)
          setIsHovered(true)
        }}
        onMouseLeave={e => {
          onMouseLeave?.(e)
          setIsHovered(false)
        }}
      />
      <Overlay
        placement={tooltipPlacement ? tooltipPlacement : 'top'}
        // Show the tooltip if the button is hovered or if the button is focused via the keyboard.
        show={
          !!tooltip && (isHovered || isFocused)
        }
        target={buttonRef.current}
      >
        {props => (
          <Tooltip {...props}>
            {tooltip}
          </Tooltip>
        )}
      </Overlay>
    </>
  )
}

export type IconButtonProps = ButtonProps & {
  // Make aria-label required because this button has an icon in place of text content.
  'aria-label': string
  icon: IconDefinition
}

/** A button with an icon. */
export const IconButton = (props: IconButtonProps) => {
  const { icon, ...buttonProps } = props
  const { 'aria-label': ariaLabel } = buttonProps
  return (
    <Button tooltip={ariaLabel} {...buttonProps}>
      <FontAwesomeIcon icon={icon} />
    </Button>
  )
}

/**
 * button for an ellipsis "More actions" menu
 */
export const EllipsisDropdownButton = (props: ButtonProps & {'aria-label': string, text?: string}) => {
  return <Button tooltip={props['aria-label']} data-bs-toggle="dropdown"
    aria-expanded="false"
    {...props}>
    { props.text ? <span className="me-2">{props.text}</span> : '' }
    <FontAwesomeIcon icon={faEllipsisV} className="fa-lg"/>
  </Button>
}
