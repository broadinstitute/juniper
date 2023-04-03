import classNames from 'classnames'
import React from 'react'
import { Link } from 'react-router-dom'

import { requireOptionalString, requirePlainObject, requireString } from 'util/validationUtils'

import { MailingListButton } from './MailingListButton'

type JoinButtonConfig = {
  type: 'join'
  studyShortcode: string
  text: string
}

type MailingListButtonConfig = {
  type: 'mailingList'
  text: string
}

type InternalLinkButtonConfig = {
  type: 'internalLink'
  href: string
  text: string
}

type ExternalLinkButtonConfig = {
  type: undefined
  href: string
  text: string
}

export type ButtonConfig =
  | JoinButtonConfig
  | MailingListButtonConfig
  | InternalLinkButtonConfig
  | ExternalLinkButtonConfig

export const validateButtonConfig = (buttonConfig: unknown): ButtonConfig => {
  const message = 'Invalid button config'
  const config = requirePlainObject(buttonConfig, message)
  const type = requireOptionalString(config, 'type', message)
  const text = requireString(config, 'text', message)

  if (!(type === 'join' || type === 'mailingList' || type === 'internalLink' || type === undefined)) {
    throw new Error(`Invalid button type: "${type}"`)
  }

  if (type === 'join') {
    const studyShortcode = requireString(config, 'studyShortcode', 'Invalid join button config')
    return { type, studyShortcode, text }
  } else if (type === 'mailingList') {
    return { type, text }
  } else {
    const href = requireString(config, 'href', 'Invalid link button config')
    return { type, href, text }
  }
}

type ConfiguredButtonProps = {
  config: ButtonConfig;
  className?: string;
}

/** renders a button from a ButtonConfig */
export default function ConfiguredButton({ config, className }: ConfiguredButtonProps) {
  if (config.type === 'join') {
    const to = config.studyShortcode ? `/studies/${config.studyShortcode}/join` : '/join'
    return <Link to={to} className={classNames(className, 'btn', 'btn-primary')}>{config.text}</Link>
  } else if (config.type === 'mailingList') {
    return (
      <MailingListButton className={classNames(className, 'btn', 'btn-outline-primary')}>
        {config.text}
      </MailingListButton>
    )
  } else if (config.type === 'internalLink') {
    return <Link to={config.href} className={classNames(className, 'btn', 'btn-outline-primary')}>{config.text}</Link>
  }
  return <a href={config.href}className={classNames(className, 'btn', 'btn-outline-primary')}>{config.text}</a>
}
