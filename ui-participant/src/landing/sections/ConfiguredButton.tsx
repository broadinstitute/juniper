import classNames from 'classnames'
import React from 'react'
import { ButtonConfig } from 'api/api'
import { Link } from 'react-router-dom'

type ConfiguredButtonProps = {
  config: ButtonConfig;
  className?: string;
}

/** renders a button from a ButtonConfig */
export default function ConfiguredButton({ config, className }: ConfiguredButtonProps) {
  if (config.type === 'join') {
    const to = config.studyShortcode ? `/studies/${config.studyShortcode}/join` : '/join'
    return <Link to={to} className={classNames(className, 'btn', 'btn-primary')}>{config.text}</Link>
  } else if (config.type === 'internalLink') {
    return <Link to={config.href} className={classNames(className, 'btn', 'btn-outline-primary')}>{config.text}</Link>
  }
  return <a href={config.href}className={classNames(className, 'btn', 'btn-outline-primary')}>{config.text}</a>
}
