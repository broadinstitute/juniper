import { IconDefinition } from '@fortawesome/fontawesome-svg-core'
import { faCheck, faCircleInfo, faTriangleExclamation, faXmark } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import classNames from 'classnames'
import React from 'react'

export type AlertLevel =
  | 'primary'
  | 'info'
  | 'success'
  | 'warning'
  | 'danger'

/**
 * Returns the default icon for the given alert level.
 */
export const getDefaultIcon = (alertLevel: AlertLevel): IconDefinition | undefined => {
  return {
    primary: undefined,
    info: faCircleInfo,
    success: faCheck,
    warning: faTriangleExclamation,
    danger: faXmark
  }[alertLevel]
}

export type AlertProps = {
  icon?: IconDefinition
  level?: AlertLevel
  title: string
  onDismiss?: () => void
} & JSX.IntrinsicElements['div']

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const Alert = (props: AlertProps) => {
  const { children, className, icon, level = 'info', title, onDismiss, ...otherProps } = props
  const renderedIcon = icon ? icon : getDefaultIcon(level)

  return (
    <div
      {...otherProps}
      className={classNames('alert', `alert-${level}`, 'd-flex', 'flex-row', 'align-items-center', className)}
    >
      {renderedIcon && <FontAwesomeIcon className="me-3 fa-xl" icon={renderedIcon} />}
      <div className="flex-grow-1">
        <div className="alert-heading fw-bold">{title}</div>
        <div style={{ whiteSpace: 'pre-wrap' }}>{children}</div>
      </div>
      {!!onDismiss && <button aria-label="Close" className="btn-close ms-3" onClick={onDismiss} />}
    </div>
  )
}
