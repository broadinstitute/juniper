import classNames from 'classnames'
import React from 'react'
import { Link } from 'react-router-dom'

import { requireOptionalString, requirePlainObject, requireString } from '../util/validationUtils'
import { SectionProp, textProp } from './sections/SectionProp'

type JoinButtonConfig = {
  type: 'join'
  studyShortcode?: string
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

export const buttonConfigProps: SectionProp[] = [
  textProp
]

export const validateButtonConfig = (buttonConfig: unknown): ButtonConfig => {
  const message = 'Invalid button config'
  const config = requirePlainObject(buttonConfig, message)
  const type = requireOptionalString(config, 'type', message)
  const text = requireString(config, 'text', message)

  if (!(type === 'join' || type === 'mailingList' || type === 'internalLink' || type === undefined)) {
    throw new Error(`Invalid button type: "${type}"`)
  }
  const studyShortcode = requireOptionalString(config, 'studyShortcode', 'Invalid join button config')
  if (type === 'join') {
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

/** link typically used for navbar items */
export const ConfiguredLink = (props: ConfiguredButtonProps) => {
  const { className, config } = props
  if (config.type === 'join') {
    const to = config.studyShortcode ? `/studies/${config.studyShortcode}/join` : '/join'
    return <Link to={to} className={className}>{config.text}</Link>
  } else if (config.type === 'mailingList') {
    return <a href="#mailing-list" className={className}>{config.text}</a>
  } else if (config.type === 'internalLink') {
    return <Link to={config.href} className={className}>{config.text}</Link>
  } else { // external link
    return <a href={config.href} className={className} rel="noreferrer" target="_blank">{config.text}</a>
  }
}

/** renders a button from a ButtonConfig */
export default function ConfiguredButton(props: ConfiguredButtonProps) {
  const { className, config, ...otherProps } = props
  const buttonStyle = config.type === 'join' ? 'btn-primary' : 'btn-outline-primary'
  return (
    <ConfiguredLink
      {...otherProps}
      config={config}
      className={classNames('btn', buttonStyle, className)}
    />
  )
}
