import React from 'react'
import {ButtonConfig} from 'api/api'
import {Link} from 'react-router-dom'

/** renders a button from a ButtonConfig */
export default function ConfiguredButton({config, className}: { config: ButtonConfig, className: string }) {
  if (config.type === 'join') {
    if (config.studyShortcode) {
      return <Link to={`/studies/${config.studyShortcode}/join`}
                   className={className}>{config.text}</Link>
    } else {
      return <Link to={`/join`}
                   className={className}>{config.text}</Link>
    }
  } else if (config.type === 'internalLink') {
    return <Link to={config.href}
                 className={className}>{config.text}</Link>
  }
  return <a href={config.href} role={'button'} className={className}>{config.text}</a>
}
