import React from 'react'
import { ButtonConfig } from 'api/api'
import { Link } from 'react-router-dom'

/** renders a button from a ButtonConfig */
export default function ConfiguredButton({ config, className }: {config: ButtonConfig, className: string}) {
  if (config.type === 'join') {
    return <Link to={`study/${config.studyShortcode}/join`}
      className={className}>{config.text}</Link>
  }
  return <a href={config.href} role={'button'} className={className}>{config.text}</a>
}
