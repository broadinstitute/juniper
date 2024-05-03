import { IconDefinition } from '@fortawesome/fontawesome-svg-core'
import { faCheck, faCircleInfo, faTriangleExclamation, faXmark } from '@fortawesome/free-solid-svg-icons'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import classNames from 'classnames'
import React from 'react'
import { InlineMarkdown } from '../landing/Markdown'

export type AlertLevel =
  | 'PRIMARY'
  | 'INFO'
  | 'SUCCESS'
  | 'WARNING'
  | 'DANGER'

export type AlertTrigger =
  'NO_ACTIVITIES_REMAIN' |
  'WELCOME' |
  'STUDY_ALREADY_ENROLLED'

export type ParticipantDashboardAlert = {
  id?: string
  title: string
  detail?: string
  alertType: AlertLevel
  trigger: AlertTrigger
}

export const alertDefaults: Record<AlertTrigger, ParticipantDashboardAlert> = {
  'NO_ACTIVITIES_REMAIN': {
    title: 'All activities complete',
    detail: 'You have completed all activities for this study. We will notify you ' +
      'as soon as new activities are available. Thank you for your participation!',
    alertType: 'PRIMARY',
    trigger: 'NO_ACTIVITIES_REMAIN'
  },
  'WELCOME': {
    title: 'Welcome to the study.',
    detail: 'Please read and sign the consent form below to continue.',
    alertType: 'INFO',
    trigger: 'WELCOME'
  },
  'STUDY_ALREADY_ENROLLED': {
    title: 'You are already enrolled in this study.',
    alertType: 'INFO',
    trigger: 'STUDY_ALREADY_ENROLLED'
  }
}

/**
 * Returns the default icon for the given alert level.
 */
export const getDefaultIcon = (alertLevel: AlertLevel): IconDefinition | undefined => {
  return {
    PRIMARY: undefined,
    INFO: faCircleInfo,
    SUCCESS: faCheck,
    WARNING: faTriangleExclamation,
    DANGER: faXmark
  }[alertLevel]
}

export type AlertProps = {
  icon?: IconDefinition
  level?: AlertLevel
  title: string
  detail?: string
  onDismiss?: () => void
} & JSX.IntrinsicElements['div']

// TODO: Add JSDoc
// eslint-disable-next-line jsdoc/require-jsdoc
export const Alert = (props: AlertProps) => {
  const { className, icon, level = 'INFO', title, detail, onDismiss, ...otherProps } = props
  const renderedIcon = icon ? icon : getDefaultIcon(level)

  return (
    <div
      {...otherProps}
      className={classNames('alert', `alert-${level.toLowerCase()}`,
        'd-flex', 'flex-row', 'align-items-center', className)}
    >
      {renderedIcon && <FontAwesomeIcon className="me-3 fa-xl" icon={renderedIcon} />}
      <div className="flex-grow-1">
        <div className="alert-heading fw-bold"><InlineMarkdown>{title}</InlineMarkdown></div>
        { detail && <div style={{ whiteSpace: 'pre-wrap' }}><InlineMarkdown>{detail}</InlineMarkdown></div> }
      </div>
      {!!onDismiss && <button aria-label="Close" className="btn-close ms-3" onClick={onDismiss} />}
    </div>
  )
}
